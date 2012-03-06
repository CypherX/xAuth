package com.cypherx.xauth.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.cypherx.xauth.PlayerManager;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthPlayer;

public class xAuthBlockListener implements Listener {
	private final PlayerManager plyrMngr;

	public xAuthBlockListener(final xAuth plugin) {
		this.plyrMngr = plugin.getPlyrMngr();
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled())
			return;

		xAuthPlayer xp = plyrMngr.getPlayer(event.getPlayer());
		if (plyrMngr.isRestricted(xp, event)) {
			plyrMngr.sendNotice(xp);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled())
			return;

		xAuthPlayer xp = plyrMngr.getPlayer(event.getPlayer());
		if (plyrMngr.isRestricted(xp, event)) {
			plyrMngr.sendNotice(xp);
			event.setCancelled(true);
		}
	}
}