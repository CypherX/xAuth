package com.cypherx.xauth.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.cypherx.xauth.PlayerManager;
import com.cypherx.xauth.Utils;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.xAuthPlayer.Status;
import com.cypherx.xauth.database.Table;

public class xAuthPlayerListener implements Listener {
	private final xAuth plugin;
	private final PlayerManager plyrMngr;

	public xAuthPlayerListener(final xAuth plugin) {
		this.plugin = plugin;
		this.plyrMngr = plugin.getPlyrMngr();
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLogin(final PlayerLoginEvent event) {
		if (!event.getResult().equals(Result.ALLOWED))
			return;

		Player p = event.getPlayer();
		String ipAddress = event.getKickMessage();
		if (Utils.isIPAddress(ipAddress))
			if (plugin.getStrkMngr().isLockedOut(ipAddress, p.getName()))
				event.disallow(Result.KICK_OTHER, plugin.getMsgHndlr().get("join.error.lockout"));

		if (!isValidName(p.getName()))
			event.disallow(Result.KICK_OTHER, plugin.getMsgHndlr().get("join.error.name"));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if (p == null || !p.isOnline())
			return;

		xAuthPlayer xp = plyrMngr.getPlayer(p, true);
		String node = "";

		if (xp.isRegistered() || plugin.getConfig().getBoolean("authurl.enabled")) {
			if (plyrMngr.checkSession(xp)) {
				xp.setStatus(Status.Authenticated);
				plugin.getAuthClass(xp).online(p.getName());
				node = "join.resume";
			} else {
				node = "join.login";
				plyrMngr.protect(xp);
			}
		} else if (plyrMngr.mustRegister(p)) {
			node = "join.register";
			plyrMngr.protect(xp);
		}

		if (!node.isEmpty())
			plugin.getSchdlr().sendDelayedMessage(p, node, 1);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		xAuthPlayer p = plyrMngr.getPlayer(event.getPlayer());
		if (p.isProtected())
			plyrMngr.unprotect(p);

		plugin.getAuthClass(p).offline(event.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerChat(PlayerChatEvent event) {
		xAuthPlayer p = plyrMngr.getPlayer(event.getPlayer());
		if (plyrMngr.isRestricted(p, event)) {
			plyrMngr.sendNotice(p);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		xAuthPlayer p = plyrMngr.getPlayer(event.getPlayer().getName());
		if (plyrMngr.isRestricted(p, event)) {
			String command = event.getMessage().split(" ")[0].replaceFirst("/", "");

			// Idea was to auto-detect command aliases so they didn't have to be added to the allow list.
			// Currently doesn't work as it allows native Minecraft commands to slip through
			/*PluginCommand pCommand = plugin.getServer().getPluginCommand(command);
			if (pCommand == null)
			return;

			if (!plugin.getConfig().getStringList("guest.allowed-commands").contains(pCommand.getName())) {
				plyrMngr.sendNotice(p);
				event.setMessage("/");
				event.setCancelled(true);
			}*/

			if (!plugin.getConfig().getStringList("guest.allowed-commands").contains(command)) {
				plyrMngr.sendNotice(p);
				event.setMessage("/");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		xAuthPlayer p = plyrMngr.getPlayer(event.getPlayer().getName());
		if (plyrMngr.isRestricted(p, event)) {
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
					plyrMngr.sendNotice(p);
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
					plyrMngr.sendNotice(p);
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
		xAuthPlayer p = plyrMngr.getPlayer(event.getPlayer());
		if (plyrMngr.isRestricted(p, event)) {
			event.setTo(plugin.getDbCtrl().isTableActive(Table.LOCATION) ?
					plugin.getLocMngr().getLocation(event.getPlayer().getWorld()) : p.getLocation());
			plyrMngr.sendNotice(p);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		xAuthPlayer p = plyrMngr.getPlayer(event.getPlayer());
		if (plyrMngr.isRestricted(p, event))
			event.setCancelled(true);
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
				if (disallowed.indexOf(pName.charAt(i)) >= 0)
					return false;
			}
		}

		if (plugin.getConfig().getBoolean("filter.blank-name") && Utils.isWhitespace(pName))
			return false;

		return true;
	}
}