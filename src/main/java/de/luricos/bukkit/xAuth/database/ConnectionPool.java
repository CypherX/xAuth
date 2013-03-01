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
package de.luricos.bukkit.xAuth.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionPool {
    private static final int maxConnections = 10;

    private String url, user, password;
    private Vector<Connection> idleConnections = new Vector<Connection>();
    private Vector<Connection> busyConnections = new Vector<Connection>();
    private Lock lock = new ReentrantLock();

    public ConnectionPool(String driver, String url, String user, String password) throws ClassNotFoundException {
        Class.forName(driver);
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection leaseConn() throws SQLException {
        lock.lock();
        try {
            if (!idleConnections.isEmpty()) {
                Connection conn = idleConnections.firstElement();
                idleConnections.removeElementAt(0);
                if (conn.isValid(1)) {
                    busyConnections.add(conn);
                    return conn;
                } else {
                    conn.close();
                    return leaseConn();
                }
            }

            if (idleConnections.size() + busyConnections.size() >= maxConnections)
                throw new SQLException("Connection pool is full");

            Connection conn = DriverManager.getConnection(url, user, password);
            if (conn.isValid(1)) {
                busyConnections.add(conn);
                return conn;
            } else {
                conn.close();
                throw new SQLException("Failed to validate new connection");
            }
        } finally {
            lock.unlock();
        }
    }

    public synchronized void returnConn(Connection conn) {
        lock.lock();
        busyConnections.remove(conn);
        idleConnections.add(conn);
        lock.unlock();
    }

    public synchronized void close() throws SQLException {
        lock.lock();
        for (Connection conn : idleConnections)
            conn.close();

        for (Connection conn : busyConnections)
            conn.close();

        busyConnections.clear();
        idleConnections.clear();
        lock.unlock();
    }
}