package com.cypherx.xauth.database;

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

	public ConnectionPool(String url, String user, String password) throws ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
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