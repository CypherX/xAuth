package com.cypherx.xauth.strike;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.database.Table;

public class StrikeManager {
	private final xAuth plugin;
	private Map<String, StrikeRecord> strikeRecords = new HashMap<String, StrikeRecord>();

	public StrikeManager(final xAuth plugin) {
		this.plugin = plugin;
	}

	public StrikeRecord getRecord(String ipAddress) {
		if (strikeRecords.containsKey(ipAddress))
			return strikeRecords.get(ipAddress);

		StrikeRecord record = new StrikeRecord();
		strikeRecords.put(ipAddress, record);
		return record;
	}

	public void strikeout(Player player) {
		player.kickPlayer(plugin.getMsgHndlr().get("misc.strikeout"));
		xAuthLog.info(player.getName() + " kicked for passing the incorrect password threshold");

		if (plugin.getConfig().getInt("strikes.lockout-length") > 0) {
			String ipAddress = player.getAddress().getAddress().getHostAddress();
			String playerName = player.getName();

			Connection conn = plugin.getDbCtrl().getConnection();
			PreparedStatement ps = null;

			try {
				String sql = String.format("INSERT INTO `%s` (`ipaddress`, `playername`, `time`) VALUES (?, ?, ?)",
						plugin.getDbCtrl().getTable(Table.LOCKOUT));
				ps = conn.prepareStatement(sql);
				ps.setString(1, ipAddress);
				ps.setString(2, playerName);
				ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				ps.executeUpdate();
			} catch (SQLException e) {
				xAuthLog.severe(String.format("Failed to insert lockout record for player: %s (%s)", playerName, ipAddress), e);
			} finally {
				plugin.getDbCtrl().close(conn, ps);
			}
		}
	}

	public boolean isLockedOut(String ipAddress, String playerName) {
		if (plugin.getConfig().getInt("strikes.lockout-length") < 1)
			return false;

		Connection conn = plugin.getDbCtrl().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = String.format("SELECT `time` FROM `%s` WHERE `ipaddress` = ? AND `playername` = ?",
					plugin.getDbCtrl().getTable(Table.LOCKOUT));
			ps = conn.prepareStatement(sql);
			ps.setString(1, ipAddress);
			ps.setString(2, playerName);
			rs = ps.executeQuery();
			if (!rs.next())
				return false;

			Timestamp lockoutTime = rs.getTimestamp("time");
			Timestamp expireTime = new Timestamp(lockoutTime.getTime() + (plugin.getConfig().getInt("strikes.lockout-length") * 1000));
			return expireTime.compareTo(new Timestamp(System.currentTimeMillis())) > 0;
		} catch (SQLException e) {
			xAuthLog.severe(String.format("Failed to load lockout time for player: %s (%s)", playerName, ipAddress), e);
			return false;
		} finally {
			plugin.getDbCtrl().close(conn, ps, rs);
		}
	}
}