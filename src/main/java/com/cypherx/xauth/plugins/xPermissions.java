package com.cypherx.xauth.plugins;

import org.bukkit.entity.Player;

public class xPermissions {


	public static boolean has(Player player, String permission) {
			return player.hasPermission(permission);
	}

}