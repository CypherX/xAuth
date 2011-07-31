package com.cypherx.xauth.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.cypherx.xauth.Util;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.xAuthSettings;

public class DbUpdate {
	private File fVersion = new File(xAuth.dataFolder + File.separator + "DBVERSION");
	private int version; // Current database version
	private int sqlVersion = 0; // Version of the .sql files
	private Map<Integer, String> sqlFiles = new HashMap<Integer, String>();

	public DbUpdate() {
		loadVersion();
		loadSQLFiles();
	}

	public boolean checkVersion() {
		return sqlVersion <= version;
	}

	// Updates the database to the latest version
	public void update() {
		update(sqlVersion);
	}

	// Updates the database to the specified version
	public void update(int version) {
		if (version <= this.version)
			return;

		for (int i = this.version + 1; i <= version; i++) {
			String sql = Util.getResourceAsString("/" + sqlFiles.get(i));
			sql = sql.replace("{TABLE_ACCOUNT}", xAuthSettings.tblAccount);
			sql = sql.replace("{TABLE_SESSION}", xAuthSettings.tblSession);
			sql = sql.replace("{TABLE_LOCATION}", xAuthSettings.tblLocation);
			sql = sql.replace("{TABLE_STRIKE}", xAuthSettings.tblStrike);
			sql = sql.replace("{TABLE_INVENTORY}", xAuthSettings.tblInventory);

			String[] queries = sql.split(";");
			for (int j = 0; j < queries.length; j++) {
				String query = queries[j].trim();
				if (query.isEmpty())
					continue;

				Database.queryWrite(query);
			}
		}

		updateVersionFile(version);
	}

	private void loadVersion() {
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(fVersion));
			version = Integer.parseInt(reader.readLine());
		} catch (FileNotFoundException e) {
			createVersionFile();
			version = 0;
		} catch (IOException e) {
			xAuthLog.severe("Could not load database version from file!", e);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {}
		}
	}

	private void loadSQLFiles() {
		String jarPath = "plugins" + File.separator + "xAuth.jar";
		String sqlPath = "";

		if (xAuthSettings.datasource.equals("mysql"))
			sqlPath = "sql/mysql/";
		else
			sqlPath = "sql/h2/";

		try {
			JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			Enumeration<JarEntry> entries = jar.entries();

			while(entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
				if (name.startsWith(sqlPath) && name.endsWith(".sql")) {
					int version = Integer.parseInt(name.substring(name.lastIndexOf("/") + 1, name.indexOf("_")));

					if (sqlVersion < version)
						sqlVersion = version;

					sqlFiles.put(version, name);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createVersionFile() {
		BufferedWriter writer = null;

		try {
			fVersion.createNewFile();
			writer = new BufferedWriter(new FileWriter(fVersion));
			writer.write("0");
		} catch (IOException e) {
			xAuthLog.severe("Could not create database version file!", e);
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {}
		}
	}

	private void updateVersionFile(int version) {
		BufferedWriter writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(fVersion));
			writer.write(Integer.toString(version));
		} catch (IOException e) {
			xAuthLog.severe("Could not update database version file!", e);
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {}
		}
	}
}