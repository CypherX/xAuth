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
package de.luricos.bukkit.xAuth.strike;

import de.luricos.bukkit.xAuth.database.Table;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class StrikeManager {
    private final xAuth plugin;
    private Map<String, StrikeRecord> strikeRecords = new HashMap<String, StrikeRecord>();

    public StrikeManager(final xAuth plugin) {
        this.plugin = plugin;
    }

    public StrikeRecord getRecord(String ipAddress) {
        if (strikeRecords.containsKey(ipAddress))
            return strikeRecords.get(ipAddress);

        StrikeRecord record = new StrikeRecord();
        strikeRecords.put(ipAddress, record);
        return record;
    }

    public void strikeout(Player player) {
        player.kickPlayer(plugin.getMessageHandler().getNode("misc.strikeout"));
        xAuthLog.info(player.getName() + " kicked for passing the incorrect password threshold");

        if (plugin.getConfig().getInt("strikes.lockout-length") > 0) {
            String ipAddress = player.getAddress().getAddress().getHostAddress();
            String playerName = player.getName();

            Connection conn = plugin.getDatabaseController().getConnection();
            PreparedStatement ps = null;

            try {
                String sql = String.format("INSERT INTO `%s` (`ipaddress`, `playername`, `time`) VALUES (?, ?, ?)",
                        plugin.getDatabaseController().getTable(Table.LOCKOUT));
                ps = conn.prepareStatement(sql);
                ps.setString(1, ipAddress);
                ps.setString(2, playerName);
                ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                ps.executeUpdate();
            } catch (SQLException e) {
                xAuthLog.severe(String.format("Failed to insert lockout record for player: %s (%s)", playerName, ipAddress), e);
            } finally {
                plugin.getDatabaseController().close(conn, ps);
            }
        }
    }

    public boolean isLockedOut(String ipAddress, String playerName) {
        if (plugin.getConfig().getInt("strikes.lockout-length") < 1)
            return false;

        Connection conn = plugin.getDatabaseController().getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String sql = String.format("SELECT `time` FROM `%s` WHERE `ipaddress` = ? AND `playername` = ?",
                    plugin.getDatabaseController().getTable(Table.LOCKOUT));
            ps = conn.prepareStatement(sql);
            ps.setString(1, ipAddress);
            ps.setString(2, playerName);
            rs = ps.executeQuery();
            if (!rs.next())
                return false;

            Timestamp lockoutTime = rs.getTimestamp("time");
            Timestamp expireTime = new Timestamp(lockoutTime.getTime() + (plugin.getConfig().getInt("strikes.lockout-length") * 1000));
            return expireTime.compareTo(new Timestamp(System.currentTimeMillis())) > 0;
        } catch (SQLException e) {
            xAuthLog.severe(String.format("Failed to load lockout time for player: %s (%s)", playerName, ipAddress), e);
            return false;
        } finally {
            plugin.getDatabaseController().close(conn, ps, rs);
        }
    }
}