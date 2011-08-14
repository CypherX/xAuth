package com.cypherx.xauth.listeners;

import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.event.screen.ScreenCloseEvent;
import org.getspout.spoutapi.event.screen.ScreenListener;
import org.bukkit.event.Event;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.spout.xAuthScreen;

public class xAuthSpoutScreenListener extends ScreenListener {
	private final xAuth plugin;

	public xAuthSpoutScreenListener(xAuth plugin) {
		this.plugin = plugin;
	}

	public void registerEvent() {
		plugin.getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, this, Event.Priority.Monitor, plugin);
	}

	public void onScreenClose(ScreenCloseEvent event) {
		if (event.isCancelled())
			return;

		if (event.getScreen() instanceof xAuthScreen)
			((xAuthScreen)event.getScreen()).handleClose(event.getPlayer());
	}

	public void onButtonClick(ButtonClickEvent event) {
		if (event.isCancelled())
			return;

		if (event.getScreen() instanceof xAuthScreen)
			((xAuthScreen)event.getScreen()).handleClick(event.getButton(), event.getPlayer());
	}
}