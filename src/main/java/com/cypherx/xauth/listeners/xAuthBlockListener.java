package com.cypherx.xauth.listeners;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthPlayer;

public class xAuthBlockListener extends BlockListener {
	private final xAuth plugin;

	public xAuthBlockListener(xAuth plugin) {
		this.plugin = plugin;
	}

	public void registerEvents() {
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Lowest, plugin);
		pm.registerEvent(Event.Type.BLOCK_PLACE, this, Event.Priority.Lowest, plugin);
	}

	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled())
			return;

		xAuthPlayer xPlayer = plugin.getDataManager().getPlayerByName(event.getPlayer().getName());

		//if (!xAuthSettings.rstrBreak && !xPlayer.isRegistered())
			//return;

		if (xPlayer.isGuest()) {
			if (xPlayer.canNotify())
				xPlayer.sendIllegalActionNotice();

			event.setCancelled(true);
		}
	}

	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled())
			return;

		xAuthPlayer xPlayer = plugin.getDataManager().getPlayerByName(event.getPlayer().getName());

		//if (!xAuthSettings.rstrPlace && !xPlayer.isRegistered())
			//return;

		if (xPlayer.isGuest()) {
			if (xPlayer.canNotify())
				xPlayer.sendIllegalActionNotice();

			event.setCancelled(true);
		}
	}
}