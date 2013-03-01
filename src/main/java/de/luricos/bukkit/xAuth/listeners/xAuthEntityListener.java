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
package de.luricos.bukkit.xAuth.listeners;

import de.luricos.bukkit.xAuth.events.*;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;

public class xAuthEntityListener extends xAuthEventListener {

    public xAuthEntityListener() {
    }

    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof Player))
            return;

        Player damagee = (Player) damager;
        if (this.isAllowed(damagee, event, damagee))
            return;

        xAuthPlayer xp = playerManager.getPlayer(damagee.getName());
        playerManager.sendNotice(xp);
        event.setCancelled(true);

        this.callEvent(xAuthPlayerDamageByEntityEvent.Action.DAMAGE_BY_ENTITY_CANCELLED, xp.getStatus());
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player))
            return;

        Player player = (Player) entity;
        xAuthPlayer xp = playerManager.getPlayer(player.getName());

        if ((!this.isAllowed(player, event, player)) || playerManager.hasGodMode(xp, event.getCause())) {
            event.setCancelled(true);
            this.callEvent(xAuthPlayerDamageEvent.Action.PLAYER_DAMAGE_CANCELLED, xp.getStatus());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        if (!(target instanceof Player))
            return;

        Player player = (Player) target;
        if (this.isAllowed(player, event, target))
            return;

        event.setCancelled(true);

        this.callEvent(xAuthEntityTargetEvent.Action.ENTITY_TARGET_CANCELLED, event.getReason());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player))
            return;

        Player player = (Player) entity;
        if (this.isAllowed(player, event, player))
            return;

        event.setCancelled(true);

        this.callEvent(xAuthPlayerFoodLevelChangeEvent.Action.FOODLEVEL_CHANGE_CANCELLED, playerManager.getPlayer(player.getName()).getStatus());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        for (LivingEntity entity : event.getAffectedEntities()) {
            if (!(entity instanceof Player))
                continue;

            Player player = (Player) entity;
            if (this.isAllowed(player, event, player))
                continue;

            // dont allow splashes (set to 0) when the entity does not have the permission
            event.setIntensity(entity, 0);

            this.callEvent(xAuthPlayerPotionSplashEvent.Action.POTION_SPLASH_CANCELLED, playerManager.getPlayer(player.getName()).getStatus());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player))
            return;

        Player player = (Player) entity;
        if (this.isAllowed(player, event, player))
            return;

        event.setCancelled(true);

        this.callEvent(xAuthPlayerRegainHealthEvent.Action.REGAIN_HEALTH_CANCELLED, playerManager.getPlayer(player.getName()).getStatus());
    }
}