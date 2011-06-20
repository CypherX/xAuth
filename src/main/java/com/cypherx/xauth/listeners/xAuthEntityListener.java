package com.cypherx.xauth.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.plugin.PluginManager;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthPlayer;

public class xAuthEntityListener extends EntityListener {
	private final xAuth plugin;

	public xAuthEntityListener(xAuth plugin) {
		this.plugin = plugin;
	}

	public void registerEvents()  {
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this, Event.Priority.Lowest, plugin);
		pm.registerEvent(Event.Type.ENTITY_TARGET, this, Event.Priority.Lowest, plugin);
	}

	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (entity instanceof Player && ((Player)entity).isOnline()) { // player taking damage
			xAuthPlayer xPlayer = plugin.getDataManager().getPlayerByName(((Player)entity).getName());

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
				xAuthPlayer xPlayer = plugin.getDataManager().getPlayerByName(((Player)damager).getName());

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

	public void onEntityTarget(EntityTargetEvent event) {
		if (event.isCancelled())
			return;

		Entity target = event.getTarget();
		if (target instanceof Player) {
			xAuthPlayer xPlayer = plugin.getDataManager().getPlayerByName(((Player)target).getName());

			//if (!xAuthSettings.rstrMobTarget && !xPlayer.isRegistered())
				//return;

			if (xPlayer.isGuest())
				event.setCancelled(true);
		}
	}
}