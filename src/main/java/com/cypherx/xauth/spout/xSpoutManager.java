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
}