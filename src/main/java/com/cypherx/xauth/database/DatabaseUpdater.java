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

import com.cypherx.xauth.utils.Utils;
import com.cypherx.xauth.exceptions.TableUpdateException;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.utils.xAuthLog;

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
                        if (getUpdateVersion(updateFile) <= currentVersion)
                            continue;

                        // Load queries from update files and split into lines (one query per line)
                        String[] updateSql = loadSQL(updateFile, tblName).split(";");

                        // ANOTHER LOOP (Loop through each query)
                        for (String sql : updateSql)
                            executeQuery(sql + ";", conn);

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
                    } catch (SQLException ex) {
                    }
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
        String updatePath = "sql/" + tblId + "/updates/" + dbCon.getDBMS().toLowerCase();

        try {
            JarFile jar = new JarFile(plugin.getJar());
            Enumeration<JarEntry> entries = jar.entries();

            //Loop through contents of jar to get update file names and latest version
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(updatePath) && name.endsWith(".sql"))
                    updateFiles.add(name);
            }
        } catch (IOException e) {
            xAuthLog.severe("Failed to load update files for table: " + tblId, e);
        }

        return updateFiles;
    }

    private int getUpdateVersion(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
        return Integer.parseInt(fileName.split("_")[0]);
    }

    private int getLatestUpdateVersion(List<String> updateFiles) {
        if (updateFiles.size() < 1)
            return 0;

        String filePath = updateFiles.get(updateFiles.size() - 1);
        return getUpdateVersion(filePath);
    }

    private String loadSQL(String path, String tblName) {
        String sql = Utils.streamToString(plugin.getResource(path));
        sql = sql.replace("{TABLE}", tblName);
        sql = sql.replace("{TABLE_ACCOUNT}",

        plugin.getDatabaseController().getTable(Table.ACCOUNT)); // foreign key in session table
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