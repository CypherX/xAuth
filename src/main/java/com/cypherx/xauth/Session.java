package com.cypherx.xauth;

import java.util.Date;

import org.bukkit.entity.Player;

public class Session
{
	private Player player;
	private Date loginTime;
	private String addr;
	
	public Session(Player player)
	{
		this.player = player;
		loginTime = new Date();
		addr = player.getAddress().getAddress().getHostAddress();
	}

	public Boolean isExpired(Date timeoutTime)
	{
		if (timeoutTime.compareTo(new Date()) < 0)
			return true;

		return false;
	}
	
	public Boolean isValidAddr(String testAddr)
	{
		if (addr.equals(testAddr))
			return true;

		return false;
	}

	public Player getPlayer() { return player; }
	
	public Long getLoginTime() { return loginTime.getTime(); }
}