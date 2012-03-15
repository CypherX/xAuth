package com.cypherx.xauth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import com.cypherx.xauth.database.Table;
import com.cypherx.xauth.plugins.xPermissions;
import com.cypherx.xauth.xAuthPlayer.Status;

public class PlayerManager {
	private final xAuth plugin;
	private final ConcurrentMap<String, xAuthPlayer> players = new ConcurrentHashMap<String, xAuthPlayer>();

	public PlayerManager(final xAuth plugin) {
		this.plugin = plugin;
	}

	public xAuthPlayer getPlayer(Player player) {
		return getPlayer(player.getName(), false);
	}

	public xAuthPlayer getPlayer(Player player, boolean reload) {
		return getPlayer(player.getName(), reload);
	}

	public xAuthPlayer getPlayer(String playerName) {
		return getPlayer(playerName, false);
	}

	private xAuthPlayer getPlayer(String playerName, boolean reload) {
		String lowPlayerName = playerName.toLowerCase();

		if (players.containsKey(lowPlayerName) && !reload)
			return players.get(lowPlayerName);

		xAuthPlayer player = loadPlayer(playerName);

		//if (!plugin.isSafeMode())
			//player = loadPlayer(playerName);

		if (player == null)
			player = new xAuthPlayer(playerName);

		players.put(lowPlayerName, player);
		return player;
	}

	private xAuthPlayer loadPlayer(String playerName) {
		Connection conn = plugin.getDbCtrl().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = String.format("SELECT `id` FROM `%s` WHERE `playername` = ?",
					plugin.getConfig().getString("mysql.tables.account"));
			ps = conn.prepareStatement(sql);
			ps.setString(1, playerName);
			rs = ps.executeQuery();
			if (!rs.next())
				return null;

			return new xAuthPlayer(playerName, rs.getInt("id"));
		} catch (SQLException e) {
			xAuthLog.severe(String.format("Failed to load player: %s", playerName), e);
			return null;
		} finally {
			plugin.getDbCtrl().close(conn, ps, rs);
		}
	}

	public void handleReload(Player[] players) {
		for (Player p : players) {
			xAuthPlayer xp = getPlayer(p.getName());
			boolean mustLogin = false;

			if (xp.isRegistered()) {
				if (!checkSession(xp)) {
					mustLogin = true;
					plugin.getAuthClass(xp).offline(p.getName());
				} else {
					xp.setStatus(Status.Authenticated);
					plugin.getAuthClass(xp).online(p.getName());
				}
			}
			else if (mustRegister(p)) {
				mustLogin = true;
				plugin.getAuthClass(xp).offline(p.getName());
			}

			if (mustLogin) {
				protect(xp);
				plugin.getMsgHndlr().sendMessage("misc.reloaded", p);
			}
		}
	}

	public boolean mustRegister(Player player) {
		return plugin.getConfig().getBoolean("registration.forced") || xPermissions.has(player, "xauth.register");
	}

	public boolean checkSession(xAuthPlayer player) {
		if (!plugin.getDbCtrl().isTableActive(Table.SESSION))
			return false;

		Connection conn = plugin.getDbCtrl().getConnection();
		PreparedStatement ps = null;
		ResultSet rs= null;

		try {
			String sql = String.format("SELECT `ipaddress`, `logintime` FROM `%s` WHERE `accountid` = ?",
					plugin.getConfig().getString("mysql.tables.session"));
			ps = conn.prepareStatement(sql);
			ps.setInt(1, player.getAccountId());
			rs = ps.executeQuery();
			if (!rs.next())
				return false;

			String ipAddress = rs.getString("ipaddress");
			Timestamp loginTime = rs.getTimestamp("logintime");

			boolean valid = isSessionValid(player, ipAddress, loginTime);
			if (valid)
				return true;

			deleteSession(player.getAccountId());
			return false;
		} catch (SQLException e) {
			xAuthLog.severe(String.format("Failed to load session for account: %d", player.getAccountId()), e);
			return false;
		} finally {
			plugin.getDbCtrl().close(conn, ps, rs);
		}
	}

	private boolean isSessionValid(xAuthPlayer xp, String ipAddress, Timestamp loginTime) {
		if (plugin.getConfig().getBoolean("session.verifyip") && !ipAddress.equals(xp.getIPAddress()))
			return false;

		Timestamp expireTime = new Timestamp(loginTime.getTime() + (plugin.getConfig().getInt("session.length") * 1000));
		return expireTime.compareTo(new Timestamp(System.currentTimeMillis())) > 0;
	}

	public void protect(xAuthPlayer xp) {
		Player p = xp.getPlayer();
		plugin.getPlyrDtHndlr().storeData(xp, p);

		xp.setCreative(p.getGameMode().equals(GameMode.CREATIVE));
		if (xp.isCreativeMode())
			p.setGameMode(GameMode.SURVIVAL);

		xp.setLastNotifyTime(new Timestamp(System.currentTimeMillis()));

		int timeout = plugin.getConfig().getInt("guest.timeout");
		if (timeout > 0 && xp.isRegistered())
			xp.setTimeoutTaskId(plugin.getSchdlr().scheduleTimeoutTask(p, timeout));

		xp.setProtected(true);
	}

	public void unprotect(xAuthPlayer xp) {
		Player p = xp.getPlayer();
		plugin.getPlyrDtHndlr().restoreData(xp, p);

		if (xp.isCreativeMode())
			p.setGameMode(GameMode.CREATIVE);

		int timeoutTaskId = xp.getTimeoutTaskId();
		if (timeoutTaskId > -1) {
			Bukkit.getScheduler().cancelTask(timeoutTaskId);
			xp.setTimeoutTaskId(-1);
		}

		xp.setProtected(false);
	}

	public boolean isRestricted(xAuthPlayer player, Event event) {
		boolean restrict = true;
		String[] split = event.getEventName().split("\\.");
		String eventName = split[split.length - 1];
		split = eventName.split("(?=\\p{Upper})");

		// first element (0) will always be empty for whatever reason
		String type = split[1].toLowerCase(); //player, block, entity
		String action = split[2].toLowerCase(); // move, place, target, etc.
		String restrictNode = String.format("restrict.%s.%s", type, action);
		String allowNode = String.format("allow.%s.%s", type, action);

		if (plugin.getConfig().contains("guest." + restrictNode)) {
			if (!plugin.getConfig().getBoolean("guest." + restrictNode))
				restrict = false;

			if (xPermissions.has(player.getPlayer(), "xauth." + restrictNode))
				restrict = true;
			else if (xPermissions.has(player.getPlayer(), "xauth." + allowNode))
				restrict = false;
		}

		return player.isProtected() && restrict;
	}

	public void sendNotice(xAuthPlayer player) {
		if (canNotify(player)) {
			plugin.getMsgHndlr().sendMessage("misc.illegal", player.getPlayer());
			player.setLastNotifyTime(new Timestamp(System.currentTimeMillis()));
		}
	}

	private boolean canNotify(xAuthPlayer player) {
		Timestamp lastNotifyTime = player.getLastNotifyTime();
		if (lastNotifyTime == null)
			return true;

		Timestamp nextNotifyTime = new Timestamp(lastNotifyTime.getTime() + (plugin.getConfig().getInt("guest.notify-cooldown") * 1000));
		return nextNotifyTime.compareTo(new Timestamp(System.currentTimeMillis())) < 0;
	}

	public boolean hasGodmode(xAuthPlayer player) {
		int godmodeLength = plugin.getConfig().getInt("session.godmode-length");
		Timestamp loginTime = player.getLoginTime();
		if (godmodeLength < 1 || loginTime == null)
			return false;

		Timestamp expireTime = new Timestamp(loginTime.getTime() + (godmodeLength * 1000));
		return expireTime.compareTo(new Timestamp(System.currentTimeMillis())) > 0;
	}

	public void reload() {
		players.clear();
	}

	public boolean deleteAccount(int accountId) {
		Connection conn = plugin.getDbCtrl().getConnection();
		PreparedStatement ps = null;

		try {
			String sql = String.format("DELETE FROM `%s` WHERE `id` = ?",
					plugin.getConfig().getString("mysql.tables.account"));
			ps = conn.prepareStatement(sql);
			ps.setInt(1, accountId);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			xAuthLog.severe("Something went wrong while deleting account: " + accountId, e);
			return false;
		} finally {
			plugin.getDbCtrl().close(conn, ps);
		}
	}

	public boolean createSession(int accountId, String ipAddress) {
		Connection conn = plugin.getDbCtrl().getConnection();
		PreparedStatement ps = null;

		try {
			String sql = String.format("INSERT INTO `%s` VALUES (?, ?, ?)",
					plugin.getConfig().getString("mysql.tables.session"));
			ps = conn.prepareStatement(sql);
			ps.setInt(1, accountId);
			ps.setString(2, ipAddress);
			ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			xAuthLog.severe("Something went wrong while inserting session for account: " + accountId, e);
			return false;
		} finally {
			plugin.getDbCtrl().close(conn, ps);
		}
	}

	public boolean deleteSession(int accountId) {
		Connection conn = plugin.getDbCtrl().getConnection();
		PreparedStatement ps = null;

		try {
			String sql = String.format("DELETE FROM `%s` WHERE `accountid` = ?",
					plugin.getConfig().getString("mysql.tables.session"));
			ps = conn.prepareStatement(sql);
			ps.setInt(1, accountId);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			xAuthLog.severe("something went wrong while deleting session for account: " + accountId, e);
			return false;
		} finally {
			plugin.getDbCtrl().close(conn, ps);
		}
	}
}