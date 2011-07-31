package com.cypherx.xauth;

import java.sql.Timestamp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.cypherx.xauth.plugins.xPermissions;
//import org.bukkit.inventory.ItemStack;


public class xAuthPlayer {
	private String playerName;
	private Account account;
	private Session session;
	private boolean guest = false;
	private Location location;
	private Timestamp lastNotifyTime;
	private int strikes = 0;
	private int timeoutTaskId;

	public xAuthPlayer(String playerName) {
		this.playerName = playerName;
	}

	public xAuthPlayer(String playerName, Account account, Session session) {
		this.playerName = playerName;
		this.account = account;

		if (session != null)
			this.session = session;
	}

	public boolean isRegistered() {
		return (account != null);
	}

	public boolean hasSession() {
		return (session != null);
	}

	public boolean isAuthenticated() {
		if (session == null)
			return false;

		if (session.isExpired() || !session.getHost().equals(Util.getHostFromPlayer(getPlayer())))
			return false;

		return true;
	}

	public boolean mustRegister() {
		if (xAuthSettings.regForced)
			return true;

		return xPermissions.has(getPlayer(), "xauth.register");
	}

	public boolean canNotify() {
		if (lastNotifyTime == null)
			return true;

		Timestamp nextNotifyTime = new Timestamp(lastNotifyTime.getTime() + (xAuthSettings.notifyCooldown * 1000));
		if (nextNotifyTime.compareTo(Util.getNow()) < 0)
			return true;

		return false;
	}

	public void sendIllegalActionNotice() {
		xAuthMessages.send("miscIllegal", getPlayer());
		lastNotifyTime = Util.getNow();
	}

	public boolean hasGodmode() {
		if (xAuthSettings.godmodeLength < 1)
			return false;

		Timestamp expireTime = new Timestamp(session.getLoginTime().getTime() + (xAuthSettings.godmodeLength * 1000));
		if (expireTime.compareTo(Util.getNow()) < 0)
			return false;

		return true;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getPlayerName() {
		return playerName;
	}

	public Player getPlayer() {
		return Bukkit.getServer().getPlayer(playerName);
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Account getAccount() {
		return account;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}

	public void setGuest(boolean guest) {
		this.guest = guest;
	}

	public boolean isGuest() {
		return guest;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	/*public void setInventory(ItemStack[] inventory) {
		this.inventory = inventory;
	}

	public ItemStack[] getInventory() {
		return inventory;
	}

	public void setArmor(ItemStack[] armor) {
		this.armor = armor;
	}

	public ItemStack[] getArmor() {
		return armor;
	}*/

	public void setLastNotifyTime(Timestamp lastNotifyTime) {
		this.lastNotifyTime = lastNotifyTime;
	}

	public Timestamp getLastNotifyTime() {
		return lastNotifyTime;
	}

	public void setStrikes(int strikes) {
		this.strikes = strikes;
	}

	public int getStrikes() {
		return strikes;
	}

	public void setTimeoutTaskId(int timeoutTaskId) {
		this.timeoutTaskId = timeoutTaskId;
	}

	public int getTimeoutTaskId() {
		return timeoutTaskId;
	}
}