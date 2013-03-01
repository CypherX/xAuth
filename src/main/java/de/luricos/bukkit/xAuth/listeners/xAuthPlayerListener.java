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
import de.luricos.bukkit.xAuth.restrictions.PlayerRestrictionHandler;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.utils.xAuthUtils;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import java.sql.Timestamp;

public class xAuthPlayerListener extends xAuthEventListener {

    public xAuthPlayerListener() {
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        if (!event.getResult().equals(Result.ALLOWED))
            return;

        Player player = event.getPlayer();
        String message;
        if (player.isOnline()) {
            xAuthPlayer xp = playerManager.getPlayer(player);
            boolean reverse = xAuth.getPlugin().getConfig().getBoolean("single-session.reverse");

            if (reverse && !xp.isAuthenticated()) {
                if (!xAuth.getPlugin().getConfig().getBoolean("single-session.guests.reverse")) {
                    Timestamp expireTime = new Timestamp(xp.getConnectTime().getTime() +
                            (xAuth.getPlugin().getConfig().getInt("single-session.guests.immunity-length") * 1000));
                    reverse = !(expireTime.compareTo(new Timestamp(System.currentTimeMillis())) < 0);
                }
            }

            if (reverse) {
                message = xAuth.getPlugin().getMessageHandler().getNode("join.error.online");
                event.disallow(Result.KICK_OTHER, message);

                this.callEvent(xAuthPlayerLoginEvent.Action.PLAYER_KICK, message);
            }
        }

        String ipAddress = event.getAddress().getHostAddress();
        if (xAuthUtils.isIPAddress(ipAddress)) {
            if (xAuth.getPlugin().getStrikeManager().isLockedOut(ipAddress, player.getName())) {
                message = xAuth.getPlugin().getMessageHandler().getNode("join.error.lockout");
                event.disallow(Result.KICK_OTHER, message);

                this.callEvent(xAuthPlayerLoginEvent.Action.PLAYER_KICK, message);
            }
        }

        if (!isValidName(player.getName())) {
            event.disallow(Result.KICK_OTHER, xAuth.getPlugin().getMessageHandler().getNode("join.error.name"));
        }

        this.callEvent(xAuthPlayerLoginEvent.Action.PLAYER_LOGGED_IN, null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player == null || !player.isOnline())
            return;

        xAuthPlayer xp = playerManager.getPlayer(player, xAuth.getPlugin().getConfig().getBoolean("main.reload-on-join"));
        xp.setConnectTime(new Timestamp(System.currentTimeMillis()));
        xp.setGameMode(player.getGameMode());

        String node = "";
        boolean protect = false;

        if (xp.isRegistered() || xAuth.getPlugin().isAuthURL()) {
            if ((!xp.isLocked()) && (playerManager.checkSession(xp))) {
                xp.setStatus(xAuthPlayer.Status.AUTHENTICATED);
                xAuth.getPlugin().getAuthClass(xp).online(player.getName());
                node = "join.resume";
            } else {
                xp.setStatus(xAuthPlayer.Status.REGISTERED);
                node = "join.login";
                protect = true;
                //playerManager.protect(xp);
            }
        } else if (playerManager.mustRegister(player)) {
            xp.setStatus(xAuthPlayer.Status.GUEST);
            node = "join.register";
            protect = true;
            //playerManager.protect(xp);
        }

        if (protect) {
            xp.setProtected(true);
            scheduleDelayedProtect(xp);
        }

        if (!node.isEmpty())
            sendDelayedMessage(player, node, 1);

        scheduleDelayedPremiumCheck(xp);

        this.callEvent(xAuthPlayerJoinEvent.Action.PLAYER_JOINED);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();
        xAuthPlayer xp = playerManager.getPlayer(playerName);

        // player did not reset his password so we lock his account for security reasons.
        if (xp.isReset()) {
            xp.lockPlayer();
            playerManager.lockAcc(xp.getAccountId());
            xp.setReset(false);
            xAuth.getPlugin().getPlayerManager().getPlayer(playerName).setReset(false);
        }

        // @TODO update player object
        // @TODO update logout time (to implement)

        if (xp.isProtected())
            playerManager.unprotect(xp);

        xAuth.getPlugin().getAuthClass(xp).offline(playerName);
        xAuth.getPlugin().getPlayerManager().releasePlayer(playerName);

        this.callEvent(xAuthPlayerQuitEvent.Action.PLAYER_DISCONNECTED);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerChat(PlayerChatEvent event) {
        if (this.hasPlayerChatPermission(event.getPlayer(), event))
            return;

        event.setCancelled(true);
        this.callEvent(xAuthPlayerChatEvent.Action.CHAT_CANCELLED, playerManager.getPlayer(event.getPlayer().getName()).getStatus());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (this.hasPlayerChatPermission(event.getPlayer(), event))
            return;

        event.setCancelled(true);
        this.callEvent(xAuthPlayerChatEvent.Action.CHAT_CANCELLED, playerManager.getPlayer(event.getPlayer().getName()).getStatus());
    }

    private boolean hasPlayerChatPermission(Player player, Event event) {
        return new PlayerRestrictionHandler(player, event.getEventName(), player).hasPermission();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        // can the player execute that command?
        if (!canExecuteCommand(event.getPlayer(), event))
            return;

        // filter out xAuth commands in server.log due to a new MC-1.3.2 feature, depends on config option filter.commands
        xAuthLog.filterMessage(event.getMessage());
    }

    private boolean canExecuteCommand(final Player player, final PlayerCommandPreprocessEvent event) {
        String[] commands = event.getMessage().toLowerCase().replaceFirst("/", "").split("\\s");
        // filter only foreign commands
        if (xAuth.getPlugin().getCommands().contains(commands[0]))
            return true;

        PlayerRestrictionHandler restrictionHandler = new PlayerRestrictionHandler(player, event.getEventName(), commands);
        xAuthPlayer xp = playerManager.getPlayer(player.getName());

        boolean allowed = this.isAllowedCommand(player, commands);

        if (!allowed) {
            this.sendCommandRestrictedMessage(xp, event, restrictionHandler, commands);
            this.callEvent(xAuthPlayerExecuteCommandEvent.Action.COMMAND_DENIED, xp.getStatus());
        }

        this.callEvent(xAuthPlayerExecuteCommandEvent.Action.COMMAND_ALLOWED, xp.getStatus());

        return allowed;
    }

    private void sendCommandRestrictedMessage(final xAuthPlayer xp, final PlayerCommandPreprocessEvent event, final PlayerRestrictionHandler restrictionHandler, String[] command) {
        playerManager.sendNotice(xp, restrictionHandler.getRestrictionNode().getAction() + '.' + command);
        event.setMessage("/");
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        if (xAuth.getPlugin().getConfig().getBoolean("guest.hide-inventory"))
            return;

        if (this.isAllowed(event.getPlayer(), event, event))
            return;

        xAuthPlayer xp = playerManager.getPlayer(event.getPlayer());
        playerManager.sendNotice(xp);
        event.setCancelled(true);

        this.callEvent(xAuthPlayerDropItemEvent.Action.ITEMDROP_DENIED, xp.getStatus());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity interactWithEntity = event.getRightClicked();
        Player player = event.getPlayer();

        if ((this.isAllowed(player, event, interactWithEntity)) || (this.isAllowed(player, event, player)))
            return;

        xAuthPlayer xp = playerManager.getPlayer(player);
        playerManager.sendNotice(xp);
        event.setCancelled(true);

        this.callEvent(xAuthPlayerInteractEntityEvent.Action.INTERACT_WITH_ENTITY_DENIED, xp.getStatus());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (this.isAllowed(player, event, event.getClickedBlock()))
            return;

        xAuthPlayer xp = playerManager.getPlayer(player.getName());
        playerManager.sendNotice(xp);
        event.setCancelled(true);

        this.callEvent(xAuthPlayerInteractEvent.Action.INTERACT_DENIED, xp.getStatus());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // do nothing if player hasn't moved a block or world.
        // This will also filter out any player that only moves his mouse
        if (!this.hasChangedBlockCoordinates(event.getFrom(), event.getTo()))
            return;

        if (this.isAllowed(event.getPlayer(), event, event.getPlayer()))
            return;

        xAuthPlayer xp = playerManager.getPlayer(event.getPlayer());

        World w = xp.getPlayer().getWorld();

        Location loc = xAuth.getPlugin().getConfig().getBoolean("guest.protect-location") ?
                xAuth.getPlugin().getLocationManager().getLocation(w) : xp.getPlayerData().getLocation();

        Location testLoc = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
        // @TODO check if this is causing PlayerDeath
        while ((w.getBlockAt(testLoc).isEmpty() || w.getBlockAt(testLoc).isLiquid()) && testLoc.getY() >= 0) {
            testLoc.setY((int) testLoc.getY() - 1);
        }

        // @TODO this would set you one block higher then before (eye level), still needed in later builds?
        if (testLoc.getY() > 0) {
            loc.setY(testLoc.getY() + 1);
        }

        event.setTo(loc);
        playerManager.sendNotice(xp);

        this.callEvent(xAuthPlayerMoveEvent.Action.MOVE_DENIED, xp.getStatus());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (this.isAllowed(event.getPlayer(), event, event))
            return;

        event.setCancelled(true);

        this.callEvent(xAuthPlayerPickupItemEvent.Action.PICKUP_DENIED, playerManager.getPlayer(event.getPlayer().getName()).getStatus());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerGameModeChange(final PlayerGameModeChangeEvent event) {
        // update gamemode settings for xAuthPlayer class
        Player player = event.getPlayer();
        xAuthPlayer xp = playerManager.getPlayer(player);
        xp.setGameMode(event.getNewGameMode());

        this.callEvent(xAuthPlayerGameModeChangeEvent.Action.GAMEMODE_CHANGED, xp.getStatus());
    }

    private boolean isValidName(String pName) {
        if (pName.length() < xAuth.getPlugin().getConfig().getInt("filter.min-length"))
            return false;

        String allowed = xAuth.getPlugin().getConfig().getString("filter.allowed");
        if (allowed.length() > 0) {
            for (int i = 0; i < pName.length(); i++) {
                if (allowed.indexOf(pName.charAt(i)) == -1)
                    return false;
            }
        }

        String disallowed = xAuth.getPlugin().getConfig().getString("filter.disallowed");
        if (disallowed.length() > 0) {
            for (int i = 0; i < pName.length(); i++) {
                if (disallowed.indexOf(pName.charAt(i)) >= 0) {
                    return false;
                }
            }
        }

        return !(xAuth.getPlugin().getConfig().getBoolean("filter.blank-name") && xAuthUtils.isWhitespace(pName));
    }

    private void scheduleDelayedProtect(final xAuthPlayer xp) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(xAuth.getPlugin(), new Runnable() {
            public void run() {
                playerManager.protect(xp);
            }
        }, 1);
    }

    /**
     * Used to check the playername against mojang paid check jsp
     * This method is used to block non-premium accounts to join the server when in PremiumMode
     *
     * @param xp xAuthPlayer
     */
    private void scheduleDelayedPremiumCheck(final xAuthPlayer xp) {
        if (!xAuth.getPlugin().isPremiumMode())
            return;

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(xAuth.getPlugin(), new Runnable() {
            public void run() {
                if ((!xp.isOnline()) || (!xp.isRegistered()))
                    return;

                String playerName = xp.getPlayerName();
                boolean userIsPremium = playerManager.checkPremiumUser(playerName);
                if (!userIsPremium) {
                    xAuthLog.info(xAuth.getPlugin().getMessageHandler().getNode("join.non-premium", null, playerName));
                    return;
                }

                xAuth.getPlugin().getPlayerManager().setPremium(xp.getAccountId(), userIsPremium);
            }
        }, 1);
    }

    /**
     * Send Messages delayed and only if the task is not already running (prevents spammy consoles)
     *
     * @param player Player The player object
     * @param node Stirng MessageHandler node
     * @param delay int Define delay
     */
    private void sendDelayedMessage(final Player player, final String node, final int delay) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(xAuth.getPlugin(), new Runnable() {
            public void run() {
                if (player.isOnline())
                    xAuth.getPlugin().getMessageHandler().sendMessage(node, player);
            }
        }, delay);
    }

    /**
     * Check if the player has moved a block to prevent PlayerMoveEvent to become spammy
     * Also allow a one block jump to not reset on every jump
     *
     * @param fromLoc Location from
     * @param toLoc Location to
     *
     * @return boolean true if the location is different from its source
     */
    private boolean hasChangedBlockCoordinates(final Location fromLoc, final Location toLoc) {
        return !(fromLoc.getWorld().equals(toLoc.getWorld())
                && fromLoc.getBlockX() == toLoc.getBlockX()
                && (fromLoc.getBlockY() <= (toLoc.getBlockY() + 1))
                && fromLoc.getBlockZ() == toLoc.getBlockZ());
    }
}