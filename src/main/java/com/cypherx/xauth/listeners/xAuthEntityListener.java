/*
 * xAuth for Bukkit
 * Copyright (C) 2012 Lycano <https://github.com/lycano/xAuth/>
 *
 * Copyright (C) 2011 CypherX <https://github.com/CypherX/xAuth/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cypherx.xauth.listeners;

import com.cypherx.xauth.PlayerManager;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

public class xAuthEntityListener implements Listener {
    private final PlayerManager playerManager;

    public xAuthEntityListener(final xAuth plugin) {
        this.playerManager = plugin.getPlayerManager();
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player && ((Player) entity).isOnline()) { // player taking damage
            xAuthPlayer xp = playerManager.getPlayer(((Player) entity).getName());
            if (playerManager.isRestricted(xp, event) || playerManager.hasGodmode(xp, event.getCause()))
                event.setCancelled(true);
        }

        if (event instanceof EntityDamageByEntityEvent) { // player dealing damage to other entity
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;
            Entity damager = entityDamageByEntityEvent.getDamager();
            if (damager instanceof Player) {
                xAuthPlayer player = playerManager.getPlayer(((Player) damager).getName());
                if (playerManager.isRestricted(player, entityDamageByEntityEvent)) {
                    playerManager.sendNotice(player);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        if (target instanceof Player) {
            xAuthPlayer xp = playerManager.getPlayer(((Player) target).getName());
            if (playerManager.isRestricted(xp, event))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            xAuthPlayer xp = playerManager.getPlayer(((Player) entity).getName());
            if (playerManager.isRestricted(xp, event))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        for (LivingEntity entity : event.getAffectedEntities()) {
            if (entity instanceof Player) {
                xAuthPlayer xp = playerManager.getPlayer(((Player) entity).getName());
                if (playerManager.isRestricted(xp, event))
                    event.setIntensity(entity, 0);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            xAuthPlayer xp = playerManager.getPlayer(((Player) entity).getName());
            if (playerManager.isRestricted(xp, event))
                event.setCancelled(true);
        }
    }
}