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

public class LocationManager {
	private final xAuth plugin;
	private Map<UUID, Location> locations = new HashMap<UUID, Location> ();
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
					plugin.getConfig().getString("mysql.tables.location"));
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
}