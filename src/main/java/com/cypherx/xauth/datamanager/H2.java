package com.cypherx.xauth.datamanager;

import java.sql.DriverManager;
import java.sql.SQLException;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.xAuthSettings;

public class H2 extends DataManager {
	public H2() {
		super();
	}

	public void connect() {
		if (!xAuthSettings.datasource.equals("default"))
			System.out.println("[" + xAuth.desc.getName() + "] Unknown datasource '" + xAuthSettings.datasource + "' - Using default (H2)");

		try {
			Class.forName("org.h2.Driver");
			connection = DriverManager.getConnection("jdbc:h2:" + xAuth.dataFolder.toString() + 
					System.getProperty("file.separator") + "xAuth;IGNORECASE=TRUE", "sa", "");
			xAuthLog.info("Connection to H2 database established!");
		} catch (ClassNotFoundException e) {
			xAuthLog.severe("Missing H2 library!", e);
		} catch (SQLException e) {
			xAuthLog.severe("Could not connect to H2 database!", e);
		}
	}

	public void deleteExpiredSessions() {
		if (!isConnected())
			connect();

		try {
			stmt.executeUpdate(
				"DELETE FROM `" + xAuthSettings.tblSession + "`" +
				"WHERE NOW() > DATEADD('SECOND', " + xAuthSettings.sessionLength + ", `logintime`)"
			);
		} catch (SQLException e) {
			xAuthLog.severe("Could not delete expired settings!", e);
		}
	}
}