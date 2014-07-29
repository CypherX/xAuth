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

import de.luricos.bukkit.xAuth.PlayerManager;
import de.luricos.bukkit.xAuth.events.*;
import de.luricos.bukkit.xAuth.restrictions.PlayerRestrictionHandler;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class xAuthEventListener implements Listener {
    protected final PlayerManager playerManager;

    public xAuthEventListener() {
        this.playerManager = xAuth.getPlugin().getPlayerManager();
    }

    protected void callEvent(final xAuthSystemEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthSystemEvent.Action action) {
        this.callEvent(new xAuthSystemEvent(action));
    }

    protected void callEvent(final xAuthPlayerChatEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerChatEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthPlayerChatEvent(action, status));
    }

    protected void callEvent(final xAuthPlayerDamageByEntityEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerDamageByEntityEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthPlayerDamageByEntityEvent(action, status));
    }

    protected void callEvent(final xAuthPlayerDamageEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerDamageEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthPlayerDamageEvent(action, status));
    }

    protected void callEvent(final xAuthPlayerDropItemEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerDropItemEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthPlayerDropItemEvent(action, status));
    }

    protected void callEvent(final xAuthPlayerExecuteCommandEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerExecuteCommandEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthPlayerExecuteCommandEvent(action, status));
    }

    protected void callEvent(final xAuthPlayerFoodLevelChangeEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerFoodLevelChangeEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthPlayerFoodLevelChangeEvent(action, status));
    }

    protected void callEvent(final xAuthPlayerGameModeChangeEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerGameModeChangeEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthPlayerGameModeChangeEvent(action, status));
    }

    protected void callEvent(final xAuthPlayerInteractEntityEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerInteractEntityEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthPlayerInteractEntityEvent(action, status));
    }

    protected void callEvent(final xAuthPlayerInteractEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerInteractEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthPlayerInteractEvent(action, status));
    }

    protected void callEvent(final xAuthPlayerJoinEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerJoinEvent.Action action) {
        this.callEvent(new xAuthPlayerJoinEvent(action));
    }

    protected void callEvent(final xAuthPlayerLoginEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerLoginEvent.Action action, final String message) {
        this.callEvent(new xAuthPlayerLoginEvent(action, message));
    }

    protected void callEvent(final xAuthPlayerMoveEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerMoveEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthPlayerMoveEvent(action, status));
    }

    protected void callEvent(final xAuthPlayerPickupItemEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerPickupItemEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthPlayerPickupItemEvent(action, status));
    }

    protected void callEvent(final xAuthPlayerPotionSplashEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerPotionSplashEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthPlayerPotionSplashEvent(action, status));
    }

    protected void callEvent(final xAuthPlayerQuitEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerQuitEvent.Action action) {
        this.callEvent(new xAuthPlayerQuitEvent(action));
    }

    protected void callEvent(final xAuthPlayerRegainHealthEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerRegainHealthEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthPlayerRegainHealthEvent(action, status));
    }

    protected void callEvent(final xAuthEntityTargetEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthEntityTargetEvent.Action action, final EntityTargetEvent.TargetReason reason) {
        this.callEvent(new xAuthEntityTargetEvent(action, reason));
    }

    protected void callEvent(final xAuthBlockBreakEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthBlockBreakEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthBlockBreakEvent(action, status));
    }

    protected void callEvent(final xAuthBlockPlaceEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthBlockPlaceEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthBlockPlaceEvent(action, status));
    }

    protected void callEvent(final xAuthHangingBreakByPlayerEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthHangingBreakByPlayerEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthHangingBreakByPlayerEvent(action, status));
    }

    protected boolean isAllowed(final Player player, final Event event, final Object... obj) {
        return new PlayerRestrictionHandler(player, event.getEventName(), obj).hasPermission();
    }

    protected boolean isAllowedCommand(final Player player, final String... command) {
        return new PlayerRestrictionHandler(player, "PlayerCommandPreProcessEvent", command).hasPermission();
    }

}