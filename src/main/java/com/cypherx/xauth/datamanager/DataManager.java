package com.cypherx.xauth.datamanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.cypherx.xauth.Account;
import com.cypherx.xauth.Session;
import com.cypherx.xauth.StrikeBan;
import com.cypherx.xauth.TeleLocation;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.xAuthSettings;

public class DataManager {
	private Connection connection = null;
	private Statement stmt = null;
	private PreparedStatement prepStmt = null;
	private ResultSet rs = null;

	private ConcurrentHashMap<String, xAuthPlayer> playerCache = new ConcurrentHashMap<String, xAuthPlayer>();
	private ConcurrentHashMap<String, TeleLocation> teleLocations = new ConcurrentHashMap<String, TeleLocation>();

	public DataManager() {
		if (xAuthSettings.datasource.equals("mysql"))
			connectMySQL();
		else
			connectH2();
	}

	private void connectMySQL() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://" + xAuthSettings.mysqlHost + ":" + xAuthSettings.mysqlPort + "/" +
					xAuthSettings.mysqlDb, xAuthSettings.mysqlUser, xAuthSettings.mysqlPass);
			stmt = connection.createStatement();
			xAuthLog.info("Connection to MySQL server established!");
		} catch (ClassNotFoundException e) {
			xAuthLog.severe("Missing MySQL library!", e);
		} catch (SQLException e) {
			xAuthLog.severe("Could not connect to MySQL server!", e);
		}
	}

	private void connectH2() {
		if (!xAuthSettings.datasource.equals("default"))
			System.out.println("[" + xAuth.desc.getName() + "] Unknown datasource '" + xAuthSettings.datasource + "' - Using default (H2)");

		try {
			Class.forName("org.h2.Driver");
			connection = DriverManager.getConnection("jdbc:h2:" + xAuth.dataFolder.toString() + 
					System.getProperty("file.separator") + "xAuth;IGNORECASE=TRUE", "sa", "");
			stmt = connection.createStatement();
			xAuthLog.info("Connection to H2 database established!");
		} catch (ClassNotFoundException e) {
			xAuthLog.severe("Missing H2 library!", e);
		} catch (SQLException e) {
			xAuthLog.severe("Could not connect to H2 database!", e);
		}
	}

	public void runStartupTasks() {
		createTables();
		loadTeleLocations();

		String sql;

		if (xAuthSettings.datasource.equals("mysql")) {
			sql = "DELETE FROM `" + xAuthSettings.tblSession + "`" +
				"WHERE NOW() > ADDDATE(`logintime`, INTERVAL " + xAuthSettings.sessionLength + " SECOND)";
		} else {
			sql = "DELETE FROM `" + xAuthSettings.tblSession + "`" +
				"WHERE NOW() > DATEADD('SECOND', " + xAuthSettings.sessionLength + ", `logintime`)";
		}

		// delete expired sessions
		try {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			xAuthLog.severe("Could not delete expired settings!", e);
		}
	}

	public void printStats() {
		try {
			rs = stmt.executeQuery(
				"SELECT" +
					" (SELECT COUNT(*) FROM `" + xAuthSettings.tblAccount + "`) AS accounts," +
					" (SELECT COUNT(*) FROM `" + xAuthSettings.tblSession + "`) AS sessions"
			);

			if (rs.next())
				xAuthLog.info("Accounts: " + rs.getInt("accounts") + ", Sessions: " + rs.getInt("sessions"));
		} catch (SQLException e) {
			xAuthLog.severe("Could not fetch xAuth statistics!", e);
		}
	}

	public void createTables() {
		try {
			stmt.execute(
				"CREATE TABLE IF NOT EXISTS `" + xAuthSettings.tblAccount + "` (" +
					"`id` INT UNSIGNED NOT NULL AUTO_INCREMENT," +
					"`playername` VARCHAR(255) NOT NULL," +
					"`password` CHAR(255) NOT NULL," +
					"`email` VARCHAR(100) NULL," +
					"`registerdate` DATETIME NULL," +
					"`registerip` CHAR(15) NULL," +
					"`lastlogindate` DATETIME NULL," +
					"`lastloginip` CHAR(15) NULL," +
					"`active` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0," +
					"PRIMARY KEY(`id`)" +
				")"
			);

			stmt.execute(
				"CREATE TABLE IF NOT EXISTS `" + xAuthSettings.tblSession + "` (" +
					"`accountid` INT UNSIGNED NOT NULL," +
					"`host` CHAR(15) NOT NULL," +
					"`logintime` DATETIME NOT NULL," +
					"PRIMARY KEY(`accountid`)," +
					"FOREIGN KEY (`accountid`) REFERENCES `" + xAuthSettings.tblAccount + "`(`id`) ON DELETE CASCADE" +
				")"
			);

			stmt.execute(
				"CREATE TABLE IF NOT EXISTS `" + xAuthSettings.tblLocation + "` (" +
					"`worldname` VARCHAR(255) NOT NULL," +
					"`x` DOUBLE NOT NULL," +
					"`y` DOUBLE NOT NULL," +
					"`z` DOUBLE NOT NULL," +
					"`yaw` FLOAT NOT NULL," +
					"`pitch` FLOAT NOT NULL," +
					"PRIMARY KEY(`worldname`)" +
				")"
			);

			stmt.execute(
					"CREATE TABLE IF NOT EXISTS `" + xAuthSettings.tblStrike + "` (" +
						"`host` CHAR(15) NOT NULL," +
						"`bantime` DATETIME NOT NULL," +
						"PRIMARY KEY(`host`)" +
					")"
				);
		} catch (SQLException e) {
			xAuthLog.severe("Could not check/create database tables!", e);
		}
	}

	public xAuthPlayer getPlayerByName(String playerName) {
		String lowPlayerName = playerName.toLowerCase();

		if (playerCache.containsKey(lowPlayerName))
			return playerCache.get(lowPlayerName);

		xAuthPlayer xPlayer = getPlayerFromDb(playerName);
		if (xPlayer == null)
			xPlayer = new xAuthPlayer(playerName);

		playerCache.put(lowPlayerName, xPlayer);
		return xPlayer;
	}

	public xAuthPlayer getPlayerFromDb(String playerName) {
		xAuthPlayer xPlayer = null;

		try {
			prepStmt = connection.prepareStatement(
				"SELECT a.*, s.*" +
				" FROM `" + xAuthSettings.tblAccount + "` a" +
				" LEFT JOIN `" + xAuthSettings.tblSession + "` s" +
					" ON a.id = s.accountid" +
				" WHERE `playername` = ?"				
			);
			prepStmt.setString(1, playerName);
			rs = prepStmt.executeQuery();

			if (rs.next())
				xPlayer = new xAuthPlayer(playerName, buildAccount(rs), buildSession(rs));
		} catch (SQLException e) {
			xAuthLog.severe("Could not load player: " + playerName, e);
		}

		return xPlayer;
	}

	public void saveAccount(Account account) {
		if (account.getId() == 0)
			insertAccount(account);
		else
			updateAccount(account);
	}

	protected void insertAccount(Account account) {
		try {
			prepStmt = connection.prepareStatement(
				"INSERT INTO `" + xAuthSettings.tblAccount + "`" +
					" (`playername`, `password`, `email`, `registerdate`, `registerip`, `lastlogindate`, `lastloginip`, `active`)" +
				" VALUES" +
					" (?, ?, ?, ?, ?, ?, ?, ?)",
			Statement.RETURN_GENERATED_KEYS);
			prepStmt.setString(1, account.getPlayerName());
			prepStmt.setString(2, account.getPassword());
			prepStmt.setString(3, account.getEmail());
			prepStmt.setTimestamp(4, account.getRegisterDate());
			prepStmt.setString(5, account.getRegisterHost());
			prepStmt.setTimestamp(6, account.getLastLoginDate());
			prepStmt.setString(7, account.getLastLoginHost());
			prepStmt.setInt(8, account.getActive());
			prepStmt.executeUpdate();

			rs = prepStmt.getGeneratedKeys();
			if (rs.next())
				account.setId(rs.getInt(1));
		} catch (SQLException e) {
			xAuthLog.severe("Could not insert account for player: " + account.getPlayerName(), e);
		}
	}

	public void insertAccounts(List<Account> accounts) {
		StringBuilder sb = new StringBuilder();
		Account account;
		sb.append("INSERT INTO `" + xAuthSettings.tblAccount + "` (`playername`, `password`) VALUES");
		sb.append(" (?, ?)");

		for (int i = 1; i < accounts.size(); i++)
			sb.append(", (?, ?)");
		sb.append(";");

		try {
			prepStmt = connection.prepareStatement(sb.toString());
			for (int i = 0, j = 1; j < accounts.size() * 2; i++, j += 2) {
				account = accounts.get(i);
				prepStmt.setString(j, account.getPlayerName());
				prepStmt.setString(j + 1, account.getPassword().toLowerCase());
			}
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void updateAccount(Account account) {
		try {
			prepStmt = connection.prepareStatement(
				"UPDATE `" + xAuthSettings.tblAccount + "`" +
				" SET" +
					" `playername` = ?," +
					"`password` = ?," +
					"`email` = ?," +
					"`registerdate` = ?," +
					"`registerip` = ?," +
					"`lastlogindate` = ?," +
					"`lastloginip` = ?," +
					"`active` = ?" +
				" WHERE id = ?"
			);
			prepStmt.setString(1, account.getPlayerName());
			prepStmt.setString(2, account.getPassword());
			prepStmt.setString(3, account.getEmail());
			prepStmt.setTimestamp(4, account.getRegisterDate());
			prepStmt.setString(5, account.getRegisterHost());
			prepStmt.setTimestamp(6, account.getLastLoginDate());
			prepStmt.setString(7, account.getLastLoginHost());
			prepStmt.setInt(8, account.getActive());
			prepStmt.setInt(9, account.getId());
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			xAuthLog.severe("Could not update account for player: " + account.getPlayerName(), e);
		}
	}

	public void deleteAccount(xAuthPlayer xPlayer) {
		Account account = xPlayer.getAccount();

		try {
			prepStmt = connection.prepareStatement(
				"DELETE FROM `" + xAuthSettings.tblAccount + "`" +
				" WHERE `id` = ?"
			);
			prepStmt.setInt(1, account.getId());
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			xAuthLog.severe("Could not delete account for player: " + xPlayer.getPlayerName(), e);
		}

		xPlayer.setAccount(null);
		xPlayer.setSession(null);
	}

	public void insertSession(Session session) {
		try {
			prepStmt = connection.prepareStatement(
				"INSERT INTO `" + xAuthSettings.tblSession + "`" +
				" VALUES" +
					" (?, ?, ?)",
			Statement.RETURN_GENERATED_KEYS);
			prepStmt.setInt(1, session.getAccountId());
			prepStmt.setString(2, session.getHost());
			prepStmt.setTimestamp(3, session.getLoginTime());
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			xAuthLog.severe("Could not insert session for account: " + session.getAccountId(), e);
		}
	}

	public void deleteSession(xAuthPlayer xPlayer) {
		Session session = xPlayer.getSession();

		try {
			prepStmt = connection.prepareStatement(
				"DELETE FROM `" + xAuthSettings.tblSession + "`" +
				" WHERE `accountid` = ?"
			);
			prepStmt.setInt(1, session.getAccountId());
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			xAuthLog.severe("Could not delete session for player: " + xPlayer.getPlayerName(), e);
		}

		xPlayer.setSession(null);
	}

	public void loadTeleLocations() {
		//List<TeleLocation> teleLocations = new ArrayList<TeleLocation>();

		try {
			rs = stmt.executeQuery(
				"SELECT * " +
				"FROM `" + xAuthSettings.tblLocation + "`"
			);

			while (rs.next()) {
				TeleLocation teleLocation = new TeleLocation();
				teleLocation.setWorldName(rs.getString("worldname"));
				teleLocation.setX(rs.getDouble("x"));
				teleLocation.setY(rs.getDouble("y"));
				teleLocation.setZ(rs.getDouble("z"));
				teleLocation.setYaw(rs.getFloat("yaw"));
				teleLocation.setPitch(rs.getFloat("pitch"));
				teleLocations.put(teleLocation.getWorldName(), teleLocation);
				//teleLocations.add(teleLocation);
			}
		} catch (SQLException e) {
			xAuthLog.severe("Could not load TeleLocations from database!", e);
		}

		//return teleLocations;
	}

	public void insertTeleLocation(TeleLocation teleLocation) {
		try {
			prepStmt = connection.prepareStatement(
				"INSERT INTO `" + xAuthSettings.tblLocation + "`" +
				" VALUES" +
					" (?, ?, ?, ?, ?, ?)"
			);

			prepStmt.setString(1, teleLocation.getWorldName());
			prepStmt.setDouble(2, teleLocation.getX());
			prepStmt.setDouble(3, teleLocation.getY());
			prepStmt.setDouble(4, teleLocation.getZ());
			prepStmt.setFloat(5, teleLocation.getYaw());
			prepStmt.setFloat(6, teleLocation.getPitch());
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			xAuthLog.severe("Could not insert TeleLocation for world: " + teleLocation.getWorldName(), e);
		}
	}

	public void updateTeleLocation(TeleLocation teleLocation) {
		try {
			prepStmt = connection.prepareStatement(
				"UPDATE `" + xAuthSettings.tblLocation + "`" +
				" SET " +
					"`x` = ?," +
					"`y` = ?," +
					"`z` = ?," +
					"`yaw` = ?," +
					"`pitch` = ?" +
				" WHERE `worldname` = ?"
			);
			prepStmt.setDouble(1, teleLocation.getX());
			prepStmt.setDouble(2, teleLocation.getY());
			prepStmt.setDouble(3, teleLocation.getZ());
			prepStmt.setFloat(4, teleLocation.getYaw());
			prepStmt.setFloat(5, teleLocation.getPitch());
			prepStmt.setString(6, teleLocation.getWorldName());
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			xAuthLog.severe("Could not update TeleLocation for world: " + teleLocation.getWorldName(), e);
		}
	}

	public void deleteTeleLocation(TeleLocation teleLocation) {
		try {
			prepStmt = connection.prepareStatement(
				"DELETE FROM `" + xAuthSettings.tblLocation + "`" +
				" WHERE `worldname` = ?"
			);

			prepStmt.setString(1, teleLocation.getWorldName());
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			xAuthLog.severe("Could not delete TeleLocation for world: " + teleLocation.getWorldName(), e);
		}
	}

	public StrikeBan loadStrikeBan(String host) {
		StrikeBan ban = null;

		try {
			prepStmt = connection.prepareStatement(
				"SELECT * " +
				"FROM `" + xAuthSettings.tblStrike + "`" +
				" WHERE `host` = ?"
			);

			prepStmt.setString(1, host);
			rs = prepStmt.executeQuery();

			if (rs.next()) {
				ban = new StrikeBan();
				ban.setHost(rs.getString("host"));
				ban.setBanTime(rs.getTimestamp("bantime"));
			}
		} catch (SQLException e) {
			xAuthLog.severe("Could not load StrikeBan for host: " + host, e);
		}

		return ban;
	}

	public void insertStrikeBan(StrikeBan ban) {
		try {
			prepStmt = connection.prepareStatement(
				"INSERT INTO `" + xAuthSettings.tblStrike + "`" +
					" (`host`, `bantime`)" +
				" VALUES" +
					" (?, ?)"
			);

			prepStmt.setString(1, ban.getHost());
			prepStmt.setTimestamp(2, ban.getBanTime());
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			xAuthLog.severe("Could not insert StrikeBan for host: " + ban.getHost(), e);
		}
	}

	public void deleteStrikeBan(StrikeBan ban) {
		try {
			stmt = connection.prepareStatement(
				"DELETE FROM `" + xAuthSettings.tblStrike + "`" +
				" WHERE `host` = ?"
			);

			prepStmt.setString(1, ban.getHost());
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			xAuthLog.severe("Could not delete StrikeBan for host: " + ban.getHost(), e);
		}
	}

	public boolean isHostUsed(String host) {
		try {
			prepStmt = connection.prepareStatement(
				"SELECT `id`" +
				" FROM `" + xAuthSettings.tblAccount + "`" +
				" WHERE `registerip` = ?"
			);
			prepStmt.setString(1, host);
			rs = prepStmt.executeQuery();

			if (rs.next())
				return true;
		} catch (SQLException e) {
			xAuthLog.severe("Could not check if IP address has been used!", e);
		}

		return false;
	}

	public void close() {
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {}

		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e) {}

		try {
			if (prepStmt != null)
				prepStmt.close();
		} catch (SQLException e) {}

		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {}
	}

	private Account buildAccount(ResultSet rs) {
		Account account = null;
		try {
			account = new Account();
			account.setId(rs.getInt("id"));
			account.setPlayerName(rs.getString("playername"));
			account.setPassword(rs.getString("password"));
			account.setEmail(rs.getString("email"));
			account.setRegisterDate(rs.getTimestamp("registerdate"));
			account.setRegisterHost(rs.getString("registerip"));
			account.setLastLoginDate(rs.getTimestamp("lastlogindate"));
			account.setLastLoginHost(rs.getString("lastloginip"));
			account.setActive(rs.getInt("active"));
		} catch (SQLException e) {
			xAuthLog.severe("Error while building Account from ResultSet!", e);
		}

		return account;
	}

	private Session buildSession(ResultSet rs) {
		Session session = null;
		try {
			session = new Session();
			session.setAccountId(rs.getInt("accountid"));

			if (rs.wasNull()) // no session data in database
				return null;

			session.setHost(rs.getString("host"));
			session.setLoginTime(rs.getTimestamp("logintime"));
		} catch (SQLException e) {
			xAuthLog.severe("Error while building Session from ResultSet!", e);
		}

		return session;
	}

	public boolean isConnected() {
		try {
			if (connection == null || connection.isClosed())
				return false;
		} catch (SQLException e) {
			return false;
		}

		return true;
	}

	public TeleLocation getTeleLocation(String worldName) {
		return teleLocations.get(worldName);
	}

	public void setTeleLocation(TeleLocation teleLocation) {
		TeleLocation tOld = teleLocations.put(teleLocation.getWorldName(), teleLocation);
		if (tOld == null)
			insertTeleLocation(teleLocation);
		else
			updateTeleLocation(teleLocation);
	}

	public void removeTeleLocation(TeleLocation teleLocation) {
		teleLocations.remove(teleLocation.getWorldName());
		deleteTeleLocation(teleLocation);
	}
}