package com.cypherx.xauth.plugins;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.GenericTextField;
import org.getspout.spoutapi.gui.InGameHUD;
import org.getspout.spoutapi.gui.Label;
import org.getspout.spoutapi.gui.PopupScreen;
import org.getspout.spoutapi.gui.TextField;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.cypherx.xauth.xAuth;

public class xSpout {
	private static Plugin spoutPlugin;
	private static Plugin bcPlugin;

	public static void setup(xAuth plugin) {
		spoutPlugin = plugin.getServer().getPluginManager().getPlugin("Spout");
		bcPlugin = plugin.getServer().getPluginManager().getPlugin("BukkitContrib");
	}

	public static boolean isVersionCommand(String message) {
		if (spoutPlugin == null && bcPlugin == null)
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

	public static void showLoginScreen(Player player) {
		SpoutPlayer sPlayer = (SpoutPlayer)player;
		if (sPlayer.isSpoutCraftEnabled()) {
			InGameHUD mainScreen = sPlayer.getMainScreen();
			PopupScreen popup = new GenericPopup();
			mainScreen.attachPopupScreen(popup);

			Label lblHeader = new GenericLabel();
			lblHeader.setText("Please log in using the form below:").setHexColor(0xFFFFFF);
			lblHeader.setCentered(true);
			lblHeader.setX(mainScreen.getWidth() / 2);
			lblHeader.setY(40);
			popup.attachWidget(lblHeader);

			TextField field = new GenericTextField();
			field.setWidth(100).setHeight(12);
			field.setX((mainScreen.getWidth() / 2) - (field.getWidth() / 2));
			field.setY((mainScreen.getHeight() / 2) - (field.getHeight() / 2));
			field.setMaximumCharacters(64);
			popup.attachWidget(field);

			Label lblPassword = new GenericLabel();
			lblPassword.setText("Password:").setHexColor(0xFFFFFF);
			lblPassword.setX(field.getX()).setY(field.getY() - 10);
			popup.attachWidget(lblPassword);

			Button button = new GenericButton();
			button.setText("Login").setCentered(true);
			button.setWidth(100).setHeight(20).setX(field.getX()).setY(field.getY() + 15);
			popup.attachWidget(button);
		}
	}
}