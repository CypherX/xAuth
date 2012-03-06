package com.cypherx.xauth.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import com.cypherx.xauth.PlayerManager;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthPlayer;

public class xAuthEntityListener implements Listener {
	private final PlayerManager plyrMngr;

	public xAuthEntityListener(final xAuth plugin) {
		this.plyrMngr = plugin.getPlyrMngr();
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (entity instanceof Player && ((Player)entity).isOnline()) { // player taking damage
			xAuthPlayer xp = plyrMngr.getPlayer(((Player)entity).getName());
			if (plyrMngr.isRestricted(xp, event) || plyrMngr.hasGodmode(xp))
				event.setCancelled(true);
		} else if (event instanceof EntityDamageByEntityEvent) { // player dealing damage to other entity
			EntityDamageByEntityEvent edbeEvent = (EntityDamageByEntityEvent) event;
			Entity damager = edbeEvent.getDamager();
			if (damager instanceof Player) {
				xAuthPlayer player = plyrMngr.getPlayer(((Player)damager).getName());
				if (plyrMngr.isRestricted(player, edbeEvent)) {
					plyrMngr.sendNotice(player);
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.isCancelled())
			return;

		Entity target = event.getTarget();
		if (target instanceof Player) {
			xAuthPlayer xp = plyrMngr.getPlayer(((Player) target).getName());
			if (plyrMngr.isRestricted(xp, event))
				event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (entity instanceof Player) {
			xAuthPlayer xp = plyrMngr.getPlayer(((Player) entity).getName());
			if (plyrMngr.isRestricted(xp, event))
				event.setCancelled(true);
		}
	}
}