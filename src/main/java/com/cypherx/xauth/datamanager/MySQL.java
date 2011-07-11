package com.cypherx.xauth.datamanager;

import java.sql.DriverManager;
import java.sql.SQLException;

import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.xAuthSettings;

public class MySQL extends DataManager {
	public MySQL() {
		super();
	}

	public void connect() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://" + xAuthSettings.mysqlHost + ":" + xAuthSettings.mysqlPort + "/" +
					xAuthSettings.mysqlDb + "?zeroDateTimeBehavior=convertToNull", xAuthSettings.mysqlUser, xAuthSettings.mysqlPass);
			xAuthLog.info("Connection to MySQL server established!");
		} catch (ClassNotFoundException e) {
			xAuthLog.severe("Missing MySQL library!", e);
		} catch (SQLException e) {
			xAuthLog.severe("Could not connect to MySQL server!", e);
		}
	}

	public void deleteExpiredSessions() {
		if (!isConnected())
			connect();

		try {
			stmt.executeUpdate(
				"DELETE FROM `" + xAuthSettings.tblSession + "`" +
				"WHERE NOW() > ADDDATE(`logintime`, INTERVAL " + xAuthSettings.sessionLength + " SECOND)"
			);
		} catch (SQLException e) {
			xAuthLog.severe("Could not delete expired settings!", e);
		}
	}
}