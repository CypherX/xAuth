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

import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class xAuthInventoryListener extends xAuthEventListener {

    public xAuthInventoryListener() {
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (xAuth.getPlugin().getConfig().getBoolean("guest.hide-inventory"))
            return;

        HumanEntity entity = event.getWhoClicked();
        if (!(entity instanceof Player))
            return;

        Player player = (Player) entity;
        if (this.isAllowed(player, event, event.getInventory().getType()))
            return;

        playerManager.sendNotice(playerManager.getPlayer(player.getName()));
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        HumanEntity humanEntity = event.getPlayer();

        // TODO check what happens when opening villager inventory
        if (!(humanEntity instanceof Player))
            return;

        if (this.isAllowed((Player) humanEntity, event, event.getInventory().getType()))
            return;

        event.setCancelled(true);
    }
}