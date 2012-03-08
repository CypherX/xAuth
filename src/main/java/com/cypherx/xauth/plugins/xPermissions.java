package com.cypherx.xauth.plugins;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.cypherx.xauth.xAuthLog;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;

public class xPermissions {
	private static PermissionsType type = null;
	private static Plugin permissionsPlugin;

	public static void init() {
		Plugin permissionsEx = Bukkit.getServer().getPluginManager().getPlugin("PermissionsEx");
		Plugin bPermissions = Bukkit.getServer().getPluginManager().getPlugin("bPermissions");

		if (permissionsEx != null) {
			type = PermissionsType.PermissionsEx;
			permissionsPlugin = permissionsEx;
		} else if (bPermissions != null) {
			type = PermissionsType.bPermissions;
			permissionsPlugin = bPermissions;
		} else
			type = PermissionsType.BukkitPerms;

		if (permissionsPlugin != null) {
			PluginDescriptionFile desc = permissionsPlugin.getDescription();
			xAuthLog.info("Permissions support enabled: " + desc.getName() + " v" + desc.getVersion());
		} else
			xAuthLog.info("Bukkit Permissions enabled (no plugin detected)");
	}

	public static boolean has(CommandSender sender, String permission) {
		if (sender instanceof Player)
			return has((Player)sender, permission);
		else if (sender instanceof ConsoleCommandSender)
			return true;
		else
			return false;
	}

	public static boolean has(Player player, String permission) {
		switch (type) {
		case PermissionsEx:
			return PermissionsEx.getPermissionManager().has(player, permission);
		case bPermissions:
			return ApiLayer.hasPermission(player.getWorld().getName(), CalculableType.USER, player.getName(), permission);
		case BukkitPerms:
			return player.hasPermission(permission);
		default:
			return player.isOp();
		}
	}

	/*public static void addGroup(Player player, String group) {
		switch (type) {
		case PermissionsEx:
			PermissionsEx.getUser(player).addGroup(group);
			break;
		case bPermissions:
			ApiLayer.addGroup(player.getWorld().getName(), CalculableType.USER, player.getName(), group);
			break;
		case BukkitPerms:
			xAuthLog.warning("Bukkit Permissions does not support modifying groups");
			break;
		}
	}

	public static void removeGroup(Player player, String group) {
		switch (type) {
		case PermissionsEx:
			PermissionsEx.getUser(player).removeGroup(group);
			break;
		case bPermissions:
			ApiLayer.removeGroup(player.getWorld().getName(), CalculableType.USER, player.getName(), group);
			break;
		case BukkitPerms:
			xAuthLog.warning("Bukkit Permissions does not support modifying groups");
			break;
		}
	}*/

	private enum PermissionsType {
		PermissionsEx,
		bPermissions,
		BukkitPerms
	}
}