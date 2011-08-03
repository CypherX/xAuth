package com.cypherx.xauth.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.cypherx.xauth.Account;
import com.cypherx.xauth.Session;
import com.cypherx.xauth.StrikeBan;
import com.cypherx.xauth.TeleLocation;
import com.cypherx.xauth.Util;
import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.xAuthSettings;
import com.cypherx.xauth.database.Database.DBMS;

public class DbUtil {
	/* START ACCOUNT METHODS */
	public static void saveAccount(Account account) {
		if (account.getId() == 0)
			insertAccount(account);
		else
			updateAccount(account);
	}

	private static void insertAccount(Account account) {
		String sql = "INSERT INTO `" + xAuthSettings.tblAccount + "`" +
						" (`playername`, `password`, `email`, `registerdate`, `registerip`, `lastlogindate`, `lastloginip`, `active`)" +
							" VALUES" +
						" (?, ?, ?, ?, ?, ?, ?, ?)";
		Database.queryWrite(sql, account.getPlayerName(), account.getPassword(), account.getEmail(),
				account.getRegisterDate(), account.getRegisterHost(), account.getLastLoginDate(),
				account.getLastLoginHost(), account.getActive());
		account.setId(Database.lastInsertId());
	}

	private static void updateAccount(Account account) {
		String sql = "UPDATE `" + xAuthSettings.tblAccount + "`" +
					" SET" +
						" `playername` = ?," +
						"`password` = ?," +
						"`email` = ?," +
						"`registerdate` = ?," +
						"`registerip` = ?," +
						"`lastlogindate` = ?," +
						"`lastloginip` = ?," +
						"`active` = ?" +
					" WHERE id = ?";

		Database.queryWrite(sql, account.getPlayerName(), account.getPassword(), account.getEmail(),
				account.getRegisterDate(), account.getRegisterHost(), account.getLastLoginDate(),
				account.getLastLoginHost(), account.getActive(), account.getId());
	}

	public static void insertAccounts(List<Account> accounts) {
		String sql = "INSERT INTO `" + xAuthSettings.tblAccount + "` (`playername`, `password`) VALUES (?, ?)";
		Account account;

		try {
			PreparedStatement stmt = Database.getConnection().prepareStatement(sql);

			for (int i = 0; i < accounts.size(); i++) {
				account = accounts.get(i);
				stmt.setString(1, account.getPlayerName());
				stmt.setString(2, account.getPassword());
				stmt.addBatch();
			}

			Database.queryBatch(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void deleteAccount(xAuthPlayer xPlayer) {
		String sql = "DELETE FROM `" + xAuthSettings.tblAccount + "` WHERE `id` = ?";
		Database.queryWrite(sql, xPlayer.getAccount().getId());
		xPlayer.setAccount(null);
		xPlayer.setSession(null);
	}
	/* END ACCOUNT METHODS */

	/* START SESSION METHODS */
	public static void insertSession(Session session) {
		String sql = "INSERT INTO `" + xAuthSettings.tblSession + "` VALUES (?, ?, ?)";
		Database.queryWrite(sql, session.getAccountId(), session.getHost(), session.getLoginTime());
	}

	public static void deleteSession(xAuthPlayer xPlayer) {
		String sql = "DELETE FROM `" + xAuthSettings.tblSession + "` WHERE `accountid` = ?";
		Database.queryWrite(sql, xPlayer.getSession().getAccountId());
		xPlayer.setSession(null);
	}
	/* END SESSION METHODS */

	/* START LOCATION METHODS */
	public static void insertTeleLocation(TeleLocation teleLocation) {
		String sql = "INSERT INTO `" + xAuthSettings.tblLocation + "` VALUES (?, ?, ?, ?, ?, ?, ?)";
		Database.queryWrite(sql, teleLocation.getUID().toString(), teleLocation.getX(), teleLocation.getY(), teleLocation.getZ(),
				teleLocation.getYaw(), teleLocation.getPitch(), teleLocation.getGlobal());
	}

	public static void updateTeleLocation(TeleLocation teleLocation) {
		String sql = "UPDATE `" + xAuthSettings.tblLocation + "` " +
					"SET " +
						"`x` = ?, " +
						"`y` = ?, " +
						"`z` = ?, " +
						"`yaw` = ?, " +
						"`pitch` = ?, " +
						"`global` = ? " +
					"WHERE `uid` = ?";
		Database.queryWrite(sql, teleLocation.getX(), teleLocation.getY(), teleLocation.getZ(), teleLocation.getYaw(), 
				teleLocation.getPitch(), teleLocation.getGlobal(), teleLocation.getUID().toString());
	}

	public static void deleteTeleLocation(TeleLocation teleLocation) {
		String sql = "DELETE FROM `" + xAuthSettings.tblLocation + "` WHERE `uid` = ?";
		Database.queryWrite(sql, teleLocation.getUID().toString());
	}
	/* END LOCATION METHODS */

	/* START STRIKE METHODS */
	public static StrikeBan loadStrikeBan(String host) {
		String sql = "SELECT * FROM `" + xAuthSettings.tblStrike + "` WHERE `host` = ?";
		ResultSet rs = Database.queryRead(sql, host);
		StrikeBan ban = null;

		try {
			if (rs.next()) {
				ban = new StrikeBan();
				ban.setHost(rs.getString(host));
				ban.setBanTime(rs.getTimestamp("bantime"));
			}
		} catch (SQLException e) {
			xAuthLog.severe("Could not load StrikeBan for host: " + host, e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {}
		}

		return ban;
	}

	public static void insertStrikeBan(StrikeBan ban) {
		String sql = "INSERT INTO `" + xAuthSettings.tblStrike + "` VALUES (?, ?)";
		Database.queryWrite(sql, ban.getHost(), ban.getBanTime());
	}

	public static void deleteStrikeBan(StrikeBan ban) {
		String sql = "DELETE FROM `" + xAuthSettings.tblStrike + "` WHERE `host` = ?";
		Database.queryWrite(sql, ban.getHost());
	}
	/* END STRIKE METHODS */

	/* START INVENTORY METHODS */
	public static ItemStack[] getInventory(xAuthPlayer xPlayer) {
		String sql = "SELECT * FROM `" + xAuthSettings.tblInventory + "` WHERE `playername` = ?";
		ResultSet rs = Database.queryRead(sql, xPlayer.getPlayerName());

		try {
			if (rs.next()) {
				int[] itemid = Util.stringToInt(rs.getString("itemid").split(","));
				int[] amount = Util.stringToInt(rs.getString("amount").split(","));
				int[] durability = Util.stringToInt(rs.getString("durability").split(","));
				ItemStack[] inv = new ItemStack[itemid.length];

				for (int i = 0; i < inv.length; i++)
					inv[i] = new ItemStack(itemid[i], amount[i], (short)durability[i]);

				return inv;
			}
		} catch (SQLException e) {
			xAuthLog.severe("Could not load inventory for player: " + xPlayer.getPlayerName(), e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {}
		}

		return null;
	}

	public static void insertInventory(xAuthPlayer xPlayer) {
		String sql = "SELECT * FROM `" + xAuthSettings.tblInventory + "` WHERE `playername` = ?";
		ResultSet rs = Database.queryRead(sql, xPlayer.getPlayerName());

		try {
			if (rs.next())
				return;
		} catch (SQLException e) {
			xAuthLog.severe("Could not check inventory for player: " + xPlayer.getPlayerName(), e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {}
		}

		PlayerInventory inv = xPlayer.getPlayer().getInventory();
		StringBuilder sbItems = new StringBuilder();
		StringBuilder sbAmount = new StringBuilder();
		StringBuilder sbDurability = new StringBuilder();

		for (ItemStack item : inv.getContents()) {
			int itemid = 0;
			int amount = 0;
			short durability = 0;

			if (item != null) {
				itemid = item.getTypeId();
				amount = item.getAmount();
				durability = item.getDurability();
			}

			sbItems.append(itemid + ",");
			sbAmount.append(amount + ",");
			sbDurability.append(durability + ",");
		}

		for (ItemStack item : inv.getArmorContents()) {
			int itemid = 0;
			int amount = 0;
			short durability = 0;

			if (item != null) {
				itemid = item.getTypeId();
				amount = item.getAmount();
				durability = item.getDurability();
			}

			sbItems.append(itemid + ",");
			sbAmount.append(amount + ",");
			sbDurability.append(durability + ",");
		}

		sbItems.deleteCharAt(sbItems.lastIndexOf(","));
		sbAmount.deleteCharAt(sbAmount.lastIndexOf(","));
		sbDurability.deleteCharAt(sbDurability.lastIndexOf(","));

		sql = "INSERT INTO `" + xAuthSettings.tblInventory + "` VALUES (?, ?, ?, ?)";
		Database.queryWrite(sql, xPlayer.getPlayerName(), sbItems.toString(), sbAmount.toString(), sbDurability.toString());
	}

	public static void deleteInventory(xAuthPlayer xPlayer) {
		String sql = "DELETE FROM `" + xAuthSettings.tblInventory + "` WHERE `playername` = ?";
		Database.queryWrite(sql, xPlayer.getPlayerName());
	}
	/* END INVENTORY FUNCTIONS */
	public static xAuthPlayer getPlayerFromDb(String playerName) {
		xAuthPlayer xPlayer = null;
		String sql = "SELECT a.*, s.*" +
					" FROM `" + xAuthSettings.tblAccount + "` a" +
					" LEFT JOIN `" + xAuthSettings.tblSession + "` s" +
						" ON a.id = s.accountid" +
					" WHERE `playername` = ?";
		ResultSet rs = Database.queryRead(sql, playerName);

		try {
			if (rs.next())
				xPlayer = new xAuthPlayer(playerName, Util.buildAccount(rs), Util.buildSession(rs));
		} catch (SQLException e) {
			xAuthLog.severe("Could not load player: " + playerName, e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {}
		}

		return xPlayer;
	}

	public static xAuthPlayer reloadPlayer(xAuthPlayer xPlayer) {
		String sql = "SELECT a.*, s.*" +
						" FROM `" + xAuthSettings.tblAccount + "` a" +
						" LEFT JOIN `" + xAuthSettings.tblSession + "` s" +
							" ON a.id = s.accountid" +
						" WHERE `playername` = ?";
		ResultSet rs = Database.queryRead(sql, xPlayer.getPlayerName());

		try {
			if (rs.next()) {
				xPlayer.setAccount(Util.buildAccount(rs));
				xPlayer.setSession(Util.buildSession(rs));
			}
		} catch (SQLException e) {
			xAuthLog.severe("Could not reload player: " + xPlayer.getPlayerName(), e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {}
		}

		return xPlayer;
	}

	public static int getActive(String playerName) {
		String sql = "SELECT `active` FROM `" + xAuthSettings.tblAccount + "` WHERE `playername` = ?";
		ResultSet rs = Database.queryRead(sql, playerName);

		try {
			if (rs.next())
				return rs.getInt("active");
		} catch (SQLException e) {
			xAuthLog.severe("Could not check active status of player: " + playerName, e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {}
		}

		return 0;
	}

	public static void deleteExpiredSessions() {
		String sql;

		if (Database.getDBMS() == DBMS.H2)
			sql = "DELETE FROM `" + xAuthSettings.tblSession + "`" +
					" WHERE NOW() > DATEADD('SECOND', " + xAuthSettings.sessionLength + ", `logintime`)";
		else
			sql = "DELETE FROM `" + xAuthSettings.tblSession + "`" +
					" WHERE NOW() > ADDDATE(`logintime`, INTERVAL " + xAuthSettings.sessionLength + " SECOND)";

		Database.queryWrite(sql);
	}

	public static void printStats() {
		String sql = "SELECT" +
				" (SELECT COUNT(*) FROM `" + xAuthSettings.tblAccount + "`) AS accounts," +
				" (SELECT COUNT(*) FROM `" + xAuthSettings.tblSession + "`) AS sessions";
		ResultSet rs = Database.queryRead(sql);

		try {
			if (rs.next())
				xAuthLog.info("Accounts: " + rs.getInt("accounts") + ", Sessions: " + rs.getInt("sessions"));
		} catch (SQLException e) {
			xAuthLog.severe("Could not fetch xAuth statistics!", e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {}
		}
	}

	public static boolean isHostUsed(String host) {
		String sql = "SELECT * FROM `" + xAuthSettings.tblAccount + "` WHERE `registerip` = ?";
		ResultSet rs = Database.queryRead(sql, host);

		try {
			if (rs.next())
				return true;
		} catch (SQLException e) {
			xAuthLog.severe("Could not check if IP address has been used!", e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {}
		}

		return false;
	}
}