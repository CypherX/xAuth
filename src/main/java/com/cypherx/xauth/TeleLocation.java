package com.cypherx.xauth;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class TeleLocation {
	private String worldName;
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;

	public TeleLocation() {}

	public TeleLocation(Location location) {
		worldName = location.getWorld().getName();
		x = location.getX();
		y = location.getY();
		z = location.getZ();
		yaw = location.getYaw();
		pitch = location.getPitch();
	}

	public void setWorldName(String worldName) {
		this.worldName = worldName;
	}

	public String getWorldName() {
		return worldName;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getX() {
		return x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getY() {
		return y;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public double getZ() {
		return z;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public float getYaw() {
		return yaw;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float getPitch() {
		return pitch;
	}

	public void setLocation(Location location) {
		this.worldName = location.getWorld().getName();
		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
	}

	public Location getLocation() {
		World world = Bukkit.getServer().getWorld(worldName);
		return new Location(world, x, y, z, yaw, pitch);
	}
}

