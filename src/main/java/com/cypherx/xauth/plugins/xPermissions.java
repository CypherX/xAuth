package com.cypherx.xauth.plugins;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class xPermissions {
	private static PermType permType;
	private static PermissionHandler permissionHandler;

	public static void setup(xAuth plugin) {
		Plugin permissionsPlugin = plugin.getServer().getPluginManager().getPlugin("Permissions");

		if (permissionsPlugin != null) {
			permType = PermType.PERMISSIONS;
			permissionHandler = ((Permissions) permissionsPlugin).getHandler();
			xAuthLog.info("'Permissions' v" + permissionsPlugin.getDescription().getVersion() + " support enabled!");
		} else {
			permType = PermType.SUPERPERMS;
			xAuthLog.info("'Permissions' not detected, using Bukkit Superperms");
		}
	}

	public static boolean has(Player player, String permission) {
		switch (permType) {
			case PERMISSIONS:
				return permissionHandler.has(player, permission);
			case SUPERPERMS:
				return player.hasPermission(permission);
			default:
				return player.isOp();
		}
	}

	private enum PermType {
		PERMISSIONS,
		SUPERPERMS
	}
}