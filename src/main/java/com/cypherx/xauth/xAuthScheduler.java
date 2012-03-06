package com.cypherx.xauth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class xAuthScheduler {
	private final xAuth plugin;

	public xAuthScheduler(final xAuth plugin) {
		this.plugin = plugin;
	}

	// TODO change to Sync?
	public void sendDelayedMessage(final Player player, final String node, int delay) {
		Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (player.isOnline())
					plugin.getMsgHndlr().sendMessage(node, player);
			}
		}, delay);
	}

	// TODO change to Sync?
	public int scheduleTimeoutTask(final Player player, final int timeout) {
		return Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (player.isOnline())
					player.kickPlayer(plugin.getMsgHndlr().get("misc.timeout"));
			}
		}, plugin.getConfig().getInt("guest.timeout") * 20);
	}
}