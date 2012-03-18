package com.cypherx.xauth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.cypherx.xauth.database.Table;

public class LocationManager {
	private final xAuth plugin;
	private Map<UUID, Location> locations = new HashMap<UUID, Location>();
	private UUID globalUID = null;

	public LocationManager(final xAuth plugin) {
		this.plugin = plugin;
		loadLocations();
	}

	public void loadLocations() {
		Connection conn = plugin.getDbCtrl().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = String.format("SELECT * FROM `%s`",
					plugin.getDbCtrl().getTable(Table.LOCATION));
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();

			while (rs.next()) {
				UUID uid = UUID.fromString(rs.getString("uid"));
				double x = rs.getDouble("x");
				double y = rs.getDouble("y");
				double z = rs.getDouble("z");
				float yaw = rs.getFloat("yaw");
				float pitch = rs.getFloat("pitch");
				int global = rs.getInt("global");
				locations.put(uid, new Location(Bukkit.getServer().getWorld(uid), x, y, z, yaw, pitch));

				if (global == 1)
					globalUID = uid;
			}
		} catch (SQLException e) {
			xAuthLog.severe("Failed to load teleport locations!", e);
		} finally {
			plugin.getDbCtrl().close(conn, ps, rs);
		}
	}

	public Location getLocation(World world) {
		UUID uid = globalUID == null ? world.getUID() : globalUID;
		Location loc = locations.get(uid);
		return loc == null ? world.getSpawnLocation() : loc;
	}

	public boolean setLocation(Location loc, boolean global) {
		UUID uid = loc.getWorld().getUID();
		Connection conn = plugin.getDbCtrl().getConnection();
		PreparedStatement ps = null;

		try {
			String sql;
			if (plugin.getDbCtrl().isMySQL())
				sql = String.format("INSERT INTO `%s` VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `uid` = VALUES(`uid`), `x` = VALUES(`x`), `y` = VALUES(`y`), `z` = VALUES(`z`), `yaw` = VALUES(`yaw`), `pitch` = VALUES(`pitch`), `global` = VALUES(`global`)",
						plugin.getDbCtrl().getTable(Table.LOCATION));
			else
				sql = String.format("MERGE INTO `%s` VALUES (?, ?, ?, ?, ?, ?, ?)",
						plugin.getDbCtrl().getTable(Table.LOCATION));

			ps = conn.prepareStatement(sql);
			ps.setString(1, uid.toString());
			ps.setDouble(2, loc.getX());
			ps.setDouble(3, loc.getY());
			ps.setDouble(4, loc.getZ());
			ps.setFloat(5, loc.getYaw());
			ps.setFloat(6, loc.getPitch());
			ps.setInt(7, global ? 1 : 0);
			ps.executeUpdate();

			locations.put(uid, loc);
			if (global)
				globalUID = uid;
			else if (!global && globalUID != null && globalUID == uid)
				globalUID = null;

			return true;
		} catch (SQLException e) {
			xAuthLog.severe("Failed to set location!", e);
			return false;
		} finally {
			plugin.getDbCtrl().close(conn, ps);
		}
	}

	public boolean removeLocation(World world) {
		UUID uid = world.getUID();
		Connection conn = plugin.getDbCtrl().getConnection();
		PreparedStatement ps = null;

		try {
			String sql = String.format("DELETE FROM `%s` WHERE `uid` = ?",
					plugin.getDbCtrl().getTable(Table.LOCATION));
			ps = conn.prepareStatement(sql);
			ps.setString(1, uid.toString());
			ps.executeUpdate();

			locations.remove(uid);
			if (uid == globalUID)
				globalUID = null;

			return true;
		} catch (SQLException e) {
			xAuthLog.severe("Failed to remove location!", e);
			return false;
		} finally {
			plugin.getDbCtrl().close(conn, ps);
		}
	}

	public boolean isLocationSet(World world) {
		return locations.containsKey(world.getUID());
	}

	public UUID getGlobalUID() { return globalUID; }
}