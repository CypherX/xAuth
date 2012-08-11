/*
 * xAuth for Bukkit
 * Copyright (C) 2012 Lycano <https://github.com/lycano/xAuth/>
 *
 * Copyright (C) 2011 CypherX <https://github.com/CypherX/xAuth/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cypherx.xauth.database;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.utils.xAuthLog;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseController {
    private final xAuth plugin;
    private ConnectionPool connPool;
    private List<Table> activeTables = new ArrayList<Table>();
    private DBMS dbms = DBMS.H2;

    public DatabaseController(final xAuth plugin) {
        this.plugin = plugin;
        dbInit();
    }

    private void dbInit() {
        // Initialize connection pool
        String driver, url, user, pass;

        if (plugin.getConfig().getBoolean("mysql.enabled")) { // MySQL
            dbms = DBMS.MySQL;
            ConfigurationSection cs = plugin.getConfig().getConfigurationSection("mysql");
            String host = cs.getString("host");
            int port = cs.getInt("port");
            String db = cs.getString("database");

            driver = "com.mysql.jdbc.Driver";
            url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?zeroDateTimeBehavior=convertToNull";
            user = cs.getString("user");
            pass = cs.getString("password");
        } else { // H2
            driver = "org.h2.Driver";
            url = "jdbc:h2:" + plugin.getDataFolder() + File.separator + "xAuth;MODE=MySQL;IGNORECASE=TRUE";
            user = "sa";
            pass = "";
        }

        try {
            connPool = new ConnectionPool(driver, url, user, pass);
        } catch (ClassNotFoundException e) {
            xAuthLog.severe("Failed to create instance of " + getDBMS() + " JDBC Driver!", e);
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
        if (plugin.getConfig().getInt("strikes.lockout-length") > 0)
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
            xAuthLog.severe("Failed to borrow " + getDBMS() + " connection from pool!", e);
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
            } catch (Exception e) {
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
            xAuthLog.severe("Failed to close " + getDBMS() + " connection pool!", e);
        }
    }

    public void runUpdater() {
        DatabaseUpdater dbUpdater = new DatabaseUpdater(plugin, this);
        dbUpdater.runUpdate();
    }

    public boolean isTableActive(Table tbl) {
        return activeTables.contains(tbl);
    }

    public String getTable(Table tbl) {
        if (dbms == DBMS.H2)
            return tbl.getName();

        return plugin.getConfig().getString("mysql.tables." + tbl.toString().toLowerCase());
    }

    public List<Table> getActiveTables() {
        return activeTables;
    }

    public String getDBMS() {
        return dbms.toString();
    }

    public boolean isMySQL() {
        return dbms == DBMS.MySQL;
    }

    private enum DBMS {H2, MySQL}
}