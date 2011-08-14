package com.cypherx.xauth.spout;

import org.bukkit.entity.Player;

import org.getspout.spoutapi.gui.PopupScreen;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.listeners.xAuthSpoutScreenListener;

public class xSpoutManager {
	private final xAuth plugin;

	public xSpoutManager(xAuth plugin) {
		this.plugin = plugin;
		(new xAuthSpoutScreenListener(plugin)).registerEvent();
	}

	public void showScreen(Player player, ScreenType type) {
		SpoutPlayer sPlayer = (SpoutPlayer)player;
		PopupScreen screen = null;

		switch (type) {
			case LOGIN:
				screen = new LoginScreen(plugin, sPlayer);
		}

		sPlayer.getMainScreen().attachPopupScreen(screen);
	}

	// TODO: Remove when next version of Spout is released
	public static boolean isVersionCommand(String message) {
		String split[] = message.substring(1).split("\\.");
		if (split.length == 3) {
			try {
				Integer.valueOf(split[0]);
				Integer.valueOf(split[1]);
				Integer.valueOf(split[2]);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		return false;
	}
}