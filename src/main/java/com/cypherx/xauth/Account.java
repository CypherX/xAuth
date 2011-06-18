package com.cypherx.xauth;

import java.sql.Timestamp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Account {
    private int id = 0;
	private String playerName;
	private String password;
	private String email;
	private Timestamp registerDate;
	private String registerHost;
	private Timestamp lastLoginDate;
	private String lastLoginHost;
	private int active = 0;

	public Account() {}

	public Account(String playerName, String password, String email) {
		this.playerName = playerName;
		this.password = password;
		this.email = email;
		registerDate = Util.getNow();
		registerHost = Util.getHostFromPlayer(getPlayer());
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayer(Player player) {
		this.playerName = player.getName();
	}

	public Player getPlayer() {
		return Bukkit.getServer().getPlayer(playerName);
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setRegisterDate(Timestamp registerDate) {
		this.registerDate = registerDate;
	}

	public Timestamp getRegisterDate() {
		return registerDate;
	}

	public void setRegisterHost(String registerHost) {
		this.registerHost = registerHost;
	}

	public String getRegisterHost() {
		return registerHost;
	}

	public void setLastLoginDate(Timestamp lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public Timestamp getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginHost(String lastLoginHost) {
		this.lastLoginHost = lastLoginHost;
	}

	public String getLastLoginHost() {
		return lastLoginHost;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public int getActive() {
		return active;
	}
}