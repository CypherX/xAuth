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
import com.cypherx.xauth.utils.Utils;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.xAuthPlayer.Status;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import java.sql.Timestamp;

public class xAuthPlayerListener implements Listener {
    private final xAuth plugin;
    private final PlayerManager playerManager;

    public xAuthPlayerListener(xAuth plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        if (!event.getResult().equals(Result.ALLOWED))
            return;

        Player p = event.getPlayer();
        if (p.isOnline()) {
            xAuthPlayer xp = playerManager.getPlayer(p);
            boolean reverse = plugin.getConfig().getBoolean("single-session.reverse");

            if (reverse && !xp.isAuthenticated()) {
                if (!plugin.getConfig().getBoolean("single-session.guests.reverse")) {
                    Timestamp expireTime = new Timestamp(xp.getConnectTime().getTime() +
                            (plugin.getConfig().getInt("single-session.guests.immunity-length") * 1000));
                    reverse = !(expireTime.compareTo(new Timestamp(System.currentTimeMillis())) < 0);
                }
            }

            if (reverse)
                event.disallow(Result.KICK_OTHER, plugin.getMessageHandler().getNode("join.error.online"));
        }

        String ipAddress = event.getAddress().getHostAddress();
        if (Utils.isIPAddress(ipAddress))
            if (plugin.getStrikeManager().isLockedOut(ipAddress, p.getName()))
                event.disallow(Result.KICK_OTHER, plugin.getMessageHandler().getNode("join.error.lockout"));

        if (!isValidName(p.getName()))
            event.disallow(Result.KICK_OTHER, plugin.getMessageHandler().getNode("join.error.name"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (p == null || !p.isOnline())
            return;

        xAuthPlayer xp = playerManager.getPlayer(p, plugin.getConfig().getBoolean("main.reload-on-join"));
        xp.setConnectTime(new Timestamp(System.currentTimeMillis()));

        String node = "";
        boolean protect = false;

        if (xp.isRegistered() || plugin.isAuthURL()) {
            if (playerManager.checkSession(xp)) {
                xp.setStatus(Status.Authenticated);
                plugin.getAuthClass(xp).online(p.getName());
                node = "join.resume";
            } else {
                xp.setStatus(Status.Registered);
                node = "join.login";
                protect = true;
                //playerManager.protect(xp);
            }
        } else if (playerManager.mustRegister(p)) {
            xp.setStatus(Status.Guest);
            node = "join.register";
            protect = true;
            //playerManager.protect(xp);
        }

        if (protect) {
            xp.setProtected(true);
            scheduleDelayedProtect(xp);
        }

        if (!node.isEmpty())
            sendDelayedMessage(p, node, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();
        xAuthPlayer p = playerManager.getPlayer(playerName);

        if (p.isProtected())
            playerManager.unprotect(p);

        plugin.getAuthClass(p).offline(playerName);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(PlayerChatEvent event) {
        xAuthPlayer p = playerManager.getPlayer(event.getPlayer());
        if (playerManager.isRestricted(p, event)) {
            playerManager.sendNotice(p);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        xAuthPlayer p = playerManager.getPlayer(event.getPlayer().getName());
        if (playerManager.isRestricted(p, event)) {
            String command = event.getMessage().split(" ")[0].replaceFirst("/", "");

            // Idea was to auto-detect command aliases so they didn't have to be added to the allow list.
            // Currently doesn't work as it allows native Minecraft commands to slip through
            /*PluginCommand pCommand = plugin.getServer().getPluginCommand(command);
               if (pCommand == null)
               return;

               if (!plugin.getConfig().getStringList("guest.allowed-commands").contains(pCommand.getName())) {
                   playerManager.sendNotice(p);
                   event.setMessage("/");
                   event.setCancelled(true);
               }*/

            if (!plugin.getConfig().getStringList("guest.allowed-commands").contains(command)) {
                playerManager.sendNotice(p);
                event.setMessage("/");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        if (!plugin.getConfig().getBoolean("guest.hide-inventory")) {
            xAuthPlayer p = playerManager.getPlayer(event.getPlayer());
            if (playerManager.isRestricted(p, event)) {
                playerManager.sendNotice(p);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        xAuthPlayer p = playerManager.getPlayer(event.getPlayer().getName());
        if (playerManager.isRestricted(p, event)) {
            Action action = event.getAction();
            Material type = event.getClickedBlock().getType();

            // TODO add missing blocks
            if (action == Action.LEFT_CLICK_BLOCK) {
                if (type == Material.NOTE_BLOCK
                        || type == Material.WOODEN_DOOR
                        || type == Material.LEVER
                        || type == Material.IRON_DOOR
                        || type == Material.STONE_BUTTON
                        || type == Material.TRAP_DOOR) {
                    playerManager.sendNotice(p);
                    event.setCancelled(true);
                }
            } else if (action == Action.RIGHT_CLICK_BLOCK) {
                if (type == Material.DISPENSER
                        || type == Material.NOTE_BLOCK
                        || type == Material.BED
                        || type == Material.CHEST
                        || type == Material.WORKBENCH
                        || type == Material.FURNACE
                        || type == Material.SIGN
                        || type == Material.WOODEN_DOOR
                        || type == Material.LEVER
                        || type == Material.IRON_DOOR
                        || type == Material.STONE_BUTTON
                        || type == Material.JUKEBOX
                        || type == Material.TRAP_DOOR
                        || type == Material.ENCHANTMENT_TABLE) {
                    playerManager.sendNotice(p);
                    event.setCancelled(true);
                }
            } else if (action == Action.PHYSICAL) {
                if (type == Material.SOIL || type == Material.STONE_PLATE || type == Material.WOOD_PLATE)
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // do nothing if player hasn't moved a block or world.
        // This will also filter out any player that only moves his mouse
        if (!this.hasChangedBlockCoordinates(event.getFrom(), event.getTo())) {
            return;
        }

        xAuthPlayer p = playerManager.getPlayer(event.getPlayer());
        if (playerManager.isRestricted(p, event)) {
            World w = p.getPlayer().getWorld();

            Location loc = plugin.getConfig().getBoolean("guest.protect-location") ?
                    plugin.getLocationManager().getLocation(w) : p.getPlayerData().getLocation();

            Location testLoc = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
            // @TODO check if this is causing playerdeath
            while ((w.getBlockAt(testLoc).isEmpty() || w.getBlockAt(testLoc).isLiquid()) && testLoc.getY() >= 0) {
                testLoc.setY((int) testLoc.getY() - 1);
            }

            // @TODO this would set you one block higher then before (eye level), still needed in later builds?
            if (testLoc.getY() > 0) {
                loc.setY(testLoc.getY() + 1);
            }

            event.setTo(loc);
            playerManager.sendNotice(p);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        xAuthPlayer p = playerManager.getPlayer(event.getPlayer());
        if (playerManager.isRestricted(p, event))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!plugin.getConfig().getBoolean("guest.hide-inventory")) {
            HumanEntity entity = event.getWhoClicked();
            if (entity instanceof Player) {
                xAuthPlayer p = playerManager.getPlayer(((Player) entity).getName());
                if (playerManager.isRestricted(p, event)) {
                    playerManager.sendNotice(p);
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isValidName(String pName) {
        if (pName.length() < plugin.getConfig().getInt("filter.min-length"))
            return false;

        String allowed = plugin.getConfig().getString("filter.allowed");
        if (allowed.length() > 0) {
            for (int i = 0; i < pName.length(); i++) {
                if (allowed.indexOf(pName.charAt(i)) == -1)
                    return false;
            }
        }

        String disallowed = plugin.getConfig().getString("filter.disallowed");
        if (disallowed.length() > 0) {
            for (int i = 0; i < pName.length(); i++) {
                if (disallowed.indexOf(pName.charAt(i)) >= 0) {
                    return false;
                }
            }
        }

        return !(plugin.getConfig().getBoolean("filter.blank-name") && Utils.isWhitespace(pName));
    }

    private void scheduleDelayedProtect(final xAuthPlayer xp) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                playerManager.protect(xp);
            }
        }, 1);
    }

    private void sendDelayedMessage(final Player player, final String node, int delay) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                if (player.isOnline())
                    plugin.getMessageHandler().sendMessage(node, player);
            }
        }, delay);
    }

    private boolean hasChangedBlockCoordinates(final Location fromLoc, final Location toLoc) {
        return !(fromLoc.getWorld().equals(toLoc.getWorld())
                && fromLoc.getBlockX() == toLoc.getBlockX()
                && fromLoc.getBlockY() == toLoc.getBlockY()
                && fromLoc.getBlockZ() == toLoc.getBlockZ());
    }
}