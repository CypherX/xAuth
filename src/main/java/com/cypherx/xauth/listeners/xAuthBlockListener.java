package com.cypherx.xauth.listeners;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthPlayer;

public class xAuthBlockListener implements Listener {
	private final xAuth plugin;

	public xAuthBlockListener(xAuth plugin) {
		this.plugin = plugin;
	}

	public void registerEvents() {
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled())
			return;

		xAuthPlayer xPlayer = plugin.getPlayer(event.getPlayer().getName());

		//if (!xAuthSettings.rstrBreak && !xPlayer.isRegistered())
			//return;

		if (xPlayer.isGuest()) {
			if (xPlayer.canNotify())
				xPlayer.sendIllegalActionNotice();

			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled())
			return;

		xAuthPlayer xPlayer = plugin.getPlayer(event.getPlayer().getName());

		//if (!xAuthSettings.rstrPlace && !xPlayer.isRegistered())
			//return;

		if (xPlayer.isGuest()) {
			if (xPlayer.canNotify())
				xPlayer.sendIllegalActionNotice();

			event.setCancelled(true);
		}
	}
}