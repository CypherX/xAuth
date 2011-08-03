package com.cypherx.xauth.plugins;

import org.bukkit.plugin.Plugin;

import com.cypherx.xauth.xAuth;

public class xBukkitContrib {
	private static Plugin bcPlugin;

	public static void setup(xAuth plugin) {
		bcPlugin = plugin.getServer().getPluginManager().getPlugin("Spout");
		if (bcPlugin == null)
			bcPlugin = plugin.getServer().getPluginManager().getPlugin("BukkitContrib");
	}

	public static boolean isVersionCommand(String message) {
		if (bcPlugin == null)
			return false;

		String split[] = message.substring(1).split("\\.");
		if (split.length == 3) {
			try {
				Integer.valueOf(split[0]);
				Integer.valueOf(split[1]);
				Integer.valueOf(split[2]);
			} catch (NumberFormatException e) {
				return false;
			}
		}

		return true;
	}
}