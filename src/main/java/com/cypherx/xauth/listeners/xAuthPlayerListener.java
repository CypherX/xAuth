package com.cypherx.xauth.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.cypherx.xauth.Session;
import com.cypherx.xauth.Util;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthMessages;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.xAuthSettings;

public class xAuthPlayerListener extends PlayerListener {
	private final xAuth plugin;

	public xAuthPlayerListener(xAuth plugin) {
		this.plugin = plugin;
	}

	public void registerEvents() {
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_CHAT, this, Event.Priority.Lowest, plugin);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this, Event.Priority.Lowest, plugin);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Lowest, plugin);
		pm.registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.PLAYER_KICK, this, Event.Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.PLAYER_LOGIN, this, Event.Priority.Lowest, plugin);
		pm.registerEvent(Event.Type.PLAYER_MOVE, this, Event.Priority.Lowest, plugin);
		pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, this, Event.Priority.Lowest, plugin);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.Monitor, plugin);
	}

	public void onPlayerLogin(PlayerLoginEvent event) {
		if (!event.getResult().equals(Result.ALLOWED))
			return;

		String host = event.getKickMessage();
		if (host != null && plugin.isBanned(host))
			event.disallow(Result.KICK_OTHER, xAuthMessages.get("joinErrBanned", null, null));

		Player player = event.getPlayer();
		if (xAuthSettings.reverseESS && player.isOnline())
			event.disallow(Result.KICK_OTHER, xAuthMessages.get("joinErrOnline", null, null));

		if (!Util.isValidName(player))
			event.disallow(Result.KICK_OTHER, xAuthMessages.get("joinErrName", null, null));
	}

	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		xAuthPlayer xPlayer = plugin.getDataManager().getPlayerJoin(player.getName());
		boolean isRegistered = xPlayer.isRegistered();

		if (!xPlayer.isAuthenticated() && (isRegistered || (!isRegistered && xPlayer.mustRegister()))) {
			final String fieldName;
			plugin.createGuest(xPlayer);

			if (isRegistered)
				fieldName = "joinLogin";
			else
				fieldName = "joinRegister";

			// this is needed to send the message after the "xxx has joined.." announcement
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
				public void run() {
					xAuthMessages.send(fieldName, player);
				}
			}, 1);
		}
	}

	public void onPlayerKick(PlayerKickEvent event) {
		// prevent WorldGuard from kicking the already online player
		// if another with the same name joins
		if (xAuthSettings.reverseESS) {
			Plugin wgPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");

			if (wgPlugin != null && event.getReason().equals("Logged in from another location."))
				event.setCancelled(true);
		}
	}

	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		xAuthPlayer xPlayer = plugin.getDataManager().getPlayer(player.getName());

		if (xPlayer.isGuest())
			plugin.removeGuest(xPlayer);
		else if (xPlayer.hasSession()) {
			Session session = xPlayer.getSession();
			if (session.isExpired())
				plugin.getDataManager().deleteSession(xPlayer);
		}
	}

	public void onPlayerChat(PlayerChatEvent event) {
		if (event.isCancelled())
			return;

		xAuthPlayer xPlayer = plugin.getDataManager().getPlayer(event.getPlayer().getName());

		//if (!xAuthSettings.rstrChat && !xPlayer.isRegistered())
			//return;
		
		if (xPlayer.isGuest()) {
			if (xPlayer.canNotify())
				xPlayer.sendIllegalActionNotice();

			event.setCancelled(true);
		}
	}

	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled())
			return;

		xAuthPlayer xPlayer = plugin.getDataManager().getPlayer(event.getPlayer().getName());

		//if (!xAuthSettings.rstrCommands && !xPlayer.isRegistered())
			//return;

		if (xPlayer.isGuest()) {
			String command = event.getMessage().split(" ")[0].replaceFirst("/", "");

			if (xAuthSettings.allowedCmds.contains(command))
				return;

			if (xPlayer.canNotify())
				xPlayer.sendIllegalActionNotice();

			event.setMessage("/");
			event.setCancelled(true);
		}
	}

	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled())
			return;

		xAuthPlayer xPlayer = plugin.getDataManager().getPlayer(event.getPlayer().getName());

		//if (!xAuthSettings.rstrInteract && !xPlayer.isRegistered())
			//return;

		if (xPlayer.isGuest()) {
			Action action = event.getAction();
			Material type = event.getClickedBlock().getType();

			if (action == Action.LEFT_CLICK_BLOCK) {
				if (type == Material.NOTE_BLOCK
						|| type == Material.WOODEN_DOOR
						|| type == Material.LEVER
						|| type == Material.IRON_DOOR
						|| type == Material.STONE_BUTTON
						|| type == Material.TRAP_DOOR) {
					if (xPlayer.canNotify())
						xPlayer.sendIllegalActionNotice();

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
						|| type == Material.TRAP_DOOR) {
					if (xPlayer.canNotify())
						xPlayer.sendIllegalActionNotice();

					event.setCancelled(true);
				}
			} else if (action == Action.PHYSICAL) {
				if (type == Material.SOIL || type == Material.STONE_PLATE || type == Material.WOOD_PLATE)
					event.setCancelled(true);
			}
		}
	}

	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		xAuthPlayer xPlayer = plugin.getDataManager().getPlayer(player.getName());

		//if (!xAuthSettings.rstrMovement && !xPlayer.isRegistered())
			//return;

		if (xPlayer.isGuest()) {
			//Location loc = plugin.getLocationToTeleport(player.getWorld());
			//player.teleport(loc);

			//event.setFrom(loc);
			event.setTo(plugin.getLocationToTeleport(player.getWorld()));

			if (xPlayer.canNotify())
				xPlayer.sendIllegalActionNotice();

			//event.setCancelled(true);
		}
	}

	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (event.isCancelled())
			return;

		xAuthPlayer xPlayer = plugin.getDataManager().getPlayer(event.getPlayer().getName());

		//if (!xAuthSettings.rstrPickup && !xPlayer.isRegistered())
			//return;

		if (xPlayer.isGuest())
			event.setCancelled(true);
	}
}