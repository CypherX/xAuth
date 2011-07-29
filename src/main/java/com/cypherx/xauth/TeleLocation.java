package com.cypherx.xauth;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class TeleLocation {
	private UUID uid;
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;
	private int global;

	public TeleLocation() {}

	public TeleLocation(Location location, boolean global) {
		World world = location.getWorld();
		uid = world.getUID();
		x = location.getX();
		y = location.getY();
		z = location.getZ();
		yaw = location.getYaw();
		pitch = location.getPitch();
		this.global = (global ? 1 : 0);
	}

	public void setUID(UUID uid) {
		this.uid = uid;
	}

	public UUID getUID() {
		return uid;
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

	public void setGlobal(int global) {
		this.global = global;
	}

	public int getGlobal() {
		return global;
	}

	public void setLocation(Location location) {
		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
	}

	public Location getLocation() {
		World world = Bukkit.getServer().getWorld(uid);
		return new Location(world, x, y, z, yaw, pitch);
	}
}

