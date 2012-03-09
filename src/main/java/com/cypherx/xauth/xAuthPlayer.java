package com.cypherx.xauth;

import java.sql.Timestamp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class xAuthPlayer {
	private String playerName;
	private int accountId = 0;
	private Status status = Status.Guest;
	private PlayerInventory inventory;
	private Location location;
	private Timestamp lastNotifyTime;
	private Timestamp loginTime;
	private boolean creativeMode;
	private int timeoutTaskId = -1;
	private boolean isProtected = false;

	public xAuthPlayer(final String playerName) {
		this.playerName = playerName;
	}

	public xAuthPlayer(final String playerName, final int accountId) {
		this.playerName = playerName;
		this.accountId = accountId;
		status = Status.Registered;
	}

	public String getPlayerName() { return playerName; }
	public Player getPlayer() { return Bukkit.getServer().getPlayerExact(playerName); }
	public int getAccountId() { return accountId; }
	public void setAccountId(int accountId) { this.accountId = accountId; }
	public Status getStatus() { return status; }
	public void setStatus(Status status) { this.status = status; }
	public PlayerInventory getInventory() { return inventory; }
	public void setInventory(PlayerInventory inventory) { this.inventory = inventory; }
	public Location getLocation() { return location; }
	public void setLocation(Location location) { this.location = location; }
	public Timestamp getLastNotifyTime() { return lastNotifyTime; }
	public void setLastNotifyTime(Timestamp lastNotifyTime) { this.lastNotifyTime = lastNotifyTime; }
	public Timestamp getLoginTime() { return loginTime; }
	public void setLoginTime(Timestamp loginTime) { this.loginTime = loginTime; }
	public boolean isCreativeMode() { return creativeMode; }
	public void setCreative(boolean creativeMode) { this.creativeMode = creativeMode; }
	public int getTimeoutTaskId() { return timeoutTaskId; }
	public void setTimeoutTaskId(int timeoutTaskId) { this.timeoutTaskId = timeoutTaskId; }
	public boolean isProtected() { return isProtected; }
	public void setProtected(boolean isProtected) { this.isProtected = isProtected; }

	public boolean isGuest() { return status == Status.Guest; }
	public boolean isRegistered() {	return status != Status.Guest; }
	public boolean isAuthenticated() { return status == Status.Authenticated; }

	public String getIPAddress() {
		Player player = getPlayer();
		return player == null ? null : player.getAddress().getAddress().getHostAddress();
	}

	public enum Status {
		Guest, // not registered
		Registered, // registered but not logged in
		Authenticated // logged in
	}
}