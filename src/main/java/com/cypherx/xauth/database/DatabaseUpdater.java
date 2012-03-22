package com.cypherx.xauth.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.cypherx.xauth.Utils;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;

public class DatabaseUpdater {
	private final xAuth plugin;
	private final DatabaseController dbCon;
	private File dbVersionFile;
	private final Properties dbVersionProp = new Properties();

	public DatabaseUpdater(final xAuth plugin, final DatabaseController dbCon) {
		this.plugin = plugin;
		this.dbCon = dbCon;

		loadVersionFile();
	}

	private void loadVersionFile() {
		// Load table version file
		dbVersionFile = new File(plugin.getDataFolder(), "DBVERSION");
		try {
			if (!dbVersionFile.exists())
				dbVersionFile.createNewFile();

			dbVersionProp.load(new FileInputStream(dbVersionFile));
		} catch (IOException e) {
			xAuthLog.severe("Failed to load database version file!");
		}
	}

	private void updateVersionFile(String tblId, int version) {
		dbVersionProp.setProperty(tblId, String.valueOf(version));
		try {
			dbVersionProp.store(new FileOutputStream(dbVersionFile), null);
		} catch (IOException e) {
			xAuthLog.severe("Failed to update database table version file!", e);
		}
	}

	public void runUpdate() {
		for (Table tbl : dbCon.getActiveTables()) {
			String tblId = tbl.toString().toLowerCase();
			//String tblName = plugin.getConfig().getString("mysql.tables." + tblId);
			String tblName = dbCon.getTable(tbl);
			List<String> updateFiles = loadUpdateFiles(tblId);

			// -1 = not created, 0 = default table, 1+ = updated
			int currentVersion = Integer.parseInt(dbVersionProp.getProperty(tblId, "-1"));

			// Create table if it does not exist
			if (currentVersion == -1) {
				Connection conn = dbCon.getConnection();

				try {
					String createSql = loadSQL("sql/" + tblId + "/table.sql", tblName);
					executeQuery(createSql, conn);	
					xAuthLog.info("Table created: " + tblName);
					currentVersion = 0;
				} catch (TableUpdateException e) {
					xAuthLog.severe("Failed to create table: " + tblName, e);
				} finally {
					dbCon.close(conn);
				}
			}

			// 0 = No updates exist, 1+ = updates exist
			int updateVersion = getLatestUpdateVersion(updateFiles);

			// Time to update~
			if (updateVersion > currentVersion && currentVersion > -1) {
				Connection conn = dbCon.getConnection();

				try {
					// Let's not commit changes as they get executed (in case of error)
					conn.setAutoCommit(false);

					// Loop through all update files..
					for (String updateFile : updateFiles) {

						// Load queries from update files and split into lines (one query per line)
						String[] updateSql = loadSQL(updateFile, tblName).split(System.getProperty("line.separator"));

						// ANOTHER LOOP (Loop through each query)
						for (String sql : updateSql)
							executeQuery(sql, conn);

						// Let's commit those changes now that no errors have occurred
						conn.commit();						
						currentVersion++;
					}

					xAuthLog.info(String.format("Table [%s] updated to revision [%s]", tblName, formatVersion(currentVersion)));
				} catch (TableUpdateException e) {
					xAuthLog.severe(String.format("Something went wrong while updating table [%s] to revision [%s]", tblName, formatVersion(currentVersion + 1)), e);

					try {
						// oh noes, an error has occurred! quick, rollback any changes from this update
						conn.rollback();
					} catch (SQLException ex) {}
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					dbCon.close(conn);
				}
			}

			// Save updated version to file
			updateVersionFile(tblId, currentVersion);
		}
	}

	private List<String> loadUpdateFiles(String tblId) {
		List<String> updateFiles = new ArrayList<String>();
		String updatePath = "sql/"  + tblId + "/updates/" + dbCon.getDBMS().toLowerCase();

		try {
			JarFile jar = new JarFile(plugin.getJar());
			Enumeration<JarEntry> entries = jar.entries();

			//Loop through contents of jar to get update file names and latest version
			while(entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
				if (name.startsWith(updatePath) && name.endsWith(".sql"))
					updateFiles.add(name);
			}
		} catch (IOException e) {
			xAuthLog.severe("Failed to load update files for table: " + tblId, e);
		}

		return updateFiles;
	}

	private int getLatestUpdateVersion(List<String> updateFiles) {
		if (updateFiles.size() < 1)
			return 0;

		String filePath = updateFiles.get(updateFiles.size() - 1);
		String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
		return Integer.parseInt(fileName.split("_")[0]);
	}

	private String loadSQL(String path, String tblName) {
		String sql = Utils.streamToString(plugin.getResource(path));
		sql = sql.replace("{TABLE}", tblName);
		sql = sql.replace("{TABLE_ACCOUNT}",
				plugin.getDbCtrl().getTable(Table.ACCOUNT)); // foreign key in session table
		return sql;
	}

	private void executeQuery(String query, Connection conn) throws TableUpdateException {
		if (query.length() > 0) {
			PreparedStatement ps = null;
	
			try {
				ps = conn.prepareStatement(query);
				ps.executeUpdate();
			} catch (SQLException e) {
				throw new TableUpdateException(e.getMessage());
			} finally {
				dbCon.close(ps);
			}
		}
	}

	// hurrr aesthetics
	private String formatVersion(int currentVersion) {
		String strCurVer = String.valueOf(currentVersion);
		if (strCurVer.length() == 1)
			return "00" + strCurVer;
		else if (strCurVer.length() == 2)
			return "0" + strCurVer;

		return strCurVer;
	}
}