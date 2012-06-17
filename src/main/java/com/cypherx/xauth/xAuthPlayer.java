package com.cypherx.xauth;

import java.sql.Timestamp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class xAuthPlayer {
	private String playerName;
	private int accountId = 0;
	private Status status = Status.Guest;
	private PlayerData playerData;
	private Timestamp lastNotifyTime;
	private Timestamp loginTime;
	private boolean creativeMode;
	private int timeoutTaskId = -1;
	private boolean isProtected = false;
	private Timestamp connectTime;

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
	public PlayerData getPlayerData() { return playerData; }
	public void setPlayerData(PlayerData playerData) { this.playerData = playerData; }
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
	public Timestamp getConnectTime() { return connectTime; }
	public void setConnectTime(Timestamp connectTime) { this.connectTime = connectTime; }

	public boolean isGuest() { return status == Status.Guest; }
	public boolean isRegistered() {	return status != Status.Guest; }
	public boolean isAuthenticated() { return status == Status.Authenticated; }

	public String getIPAddress() {
		Player player = getPlayer();
		if (player == null)
			return null;

		try {
			return player.getAddress().getAddress().getHostAddress();
		} catch (NullPointerException e) {
			return null;
		}
	}

	public enum Status {
		Guest, // not registered
		Registered, // registered but not logged in
		Authenticated // logged in
	}
}