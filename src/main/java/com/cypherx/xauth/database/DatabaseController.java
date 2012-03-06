package com.cypherx.xauth.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;

public class DatabaseController {
	private final xAuth plugin;
	private ConnectionPool connPool;
	private List<Table> activeTables = new ArrayList<Table>();

	public DatabaseController(final xAuth plugin) {
		this.plugin = plugin;
		dbInit();
	}

	private void dbInit() {
		// Initialize connection pool
		ConfigurationSection cs = plugin.getConfig().getConfigurationSection("mysql");
		String host = cs.getString("host");
		int port = cs.getInt("port");
		String db = cs.getString("database");
		String user = cs.getString("user");
		String pass = cs.getString("password");

		String url = String.format("jdbc:mysql://%s:%d/%s?zeroDateTimeBehavior=convertToNull", host, port, db);

		try {
			connPool = new ConnectionPool(url, user, pass);
		} catch (ClassNotFoundException e) {
			xAuthLog.severe("Failed to create instance of MySQL JDBC Driver!", e);
		}

		// Register tables
		activeTables.add(Table.ACCOUNT);
		activeTables.add(Table.PLAYERDATA);

		// Activate session table only if session length is higher than zero
		if (plugin.getConfig().getInt("session.length") > 0)
			activeTables.add(Table.SESSION);

		// Activate location table only if location protection is enabled
		if (plugin.getConfig().getBoolean("guest.protect-location"))
			activeTables.add(Table.LOCATION);

		// Activate lockout table only if lockouts are enabled
		if (plugin.getConfig().getBoolean("strikes.lockout.enabled"))
			activeTables.add(Table.LOCKOUT);
	}

	public boolean isConnectable() {
		Connection conn = null;

		try {
			conn = getConnection();
			return conn != null && !conn.isClosed();
		} catch (SQLException e) {
			return false;
		} finally {
			close(conn);
		}
	}

	public Connection getConnection() {
		try {
			return connPool.leaseConn();
		} catch (Exception e) {
			xAuthLog.severe("Failed to borrow MySQL connection from pool!", e);
			return null;
		}
	}

	public void close(Connection conn, PreparedStatement ps) {
		close(conn, ps, null);
	}

	public void close(Connection conn, PreparedStatement ps, ResultSet rs) {
		close(rs);
		close(ps);
		close(conn);
	}

	public void close(Connection conn) {
		if (conn != null) {
			try {
				conn.setAutoCommit(true);
				connPool.returnConn(conn);
			}
			catch (Exception e) {
				xAuthLog.warning("Failed to return connection to pool!", e);
			}
		}
	}

	public void close(PreparedStatement ps) {
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				xAuthLog.warning("Failed to close PreparedStatement object!", e);
			}
		}
	}

	private void close(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				xAuthLog.warning("Failed to close ResultSet object!", e);
			}
		}
	}

	public void close() {
		try {
			connPool.close();
		} catch (Exception e) {
			xAuthLog.severe("Failed to close MySQL connection pool!", e);
		}
	}

	public void runUpdater() {
		DatabaseUpdater dbUpdater = new DatabaseUpdater(plugin, this);
		dbUpdater.runUpdate();
	}

	public boolean isTableActive(Table table) {
		return activeTables.contains(table);
	}

	public List<Table> getActiveTables() { return activeTables; }
}