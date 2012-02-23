package com.cypherx.xauth.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.plugin.PluginManager;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthPlayer;

public class xAuthEntityListener implements Listener {
	private final xAuth plugin;

	public xAuthEntityListener(xAuth plugin) {
		this.plugin = plugin;
	}

	public void registerEvents()  {
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvents(this, plugin);
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (entity instanceof Player && ((Player)entity).isOnline()) { // player taking damage
			xAuthPlayer xPlayer = plugin.getPlayer(((Player)entity).getName());

			//if (!xAuthSettings.rstrDmgTaken && !xPlayer.isRegistered())
				//return;

			if (xPlayer.isGuest())
				event.setCancelled(true);
			else if (xPlayer.hasSession())
				if (xPlayer.hasGodmode())
					event.setCancelled(true);
		} else if (event instanceof EntityDamageByEntityEvent) { // player dealing damage to other entity
			EntityDamageByEntityEvent edbeEvent = (EntityDamageByEntityEvent)event;
			Entity damager = edbeEvent.getDamager();

			if (damager instanceof Player) {
				xAuthPlayer xPlayer = plugin.getPlayer(((Player)damager).getName());

				//if (!xAuthSettings.rstrDmgGiven && !xPlayer.isRegistered())
					//return;

				if (xPlayer.isGuest()) {
					if (xPlayer.canNotify())
						xPlayer.sendIllegalActionNotice();

					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.isCancelled())
			return;

		Entity target = event.getTarget();
		if (target instanceof Player) {
			xAuthPlayer xPlayer = plugin.getPlayer(((Player)target).getName());

			//if (!xAuthSettings.rstrMobTarget && !xPlayer.isRegistered())
				//return;

			if (xPlayer.isGuest())
				event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (entity instanceof Player) {
			xAuthPlayer xPlayer = plugin.getPlayer(((Player)entity).getName());
			if (xPlayer.isGuest())
				event.setCancelled(true);
		}
	}
}