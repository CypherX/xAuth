package com.cypherx.xauth;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class MessageHandler {
	private final xAuth plugin;
	private final String fileName = "messages.yml";
	private final File configFile;
	private FileConfiguration config = null;

	public MessageHandler(final xAuth plugin) {
		this.plugin = plugin;
		this.configFile = new File(plugin.getDataFolder(), fileName);
	}

	public FileConfiguration getConfig() {
		if (config == null)
			reloadConfig();

		return config;
	}

	private void reloadConfig() {
		config = YamlConfiguration.loadConfiguration(configFile);

		InputStream defConfigStream = plugin.getResource(fileName);
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			config.setDefaults(defConfig);
		}
	}

	public void saveConfig() {
		try {
			getConfig().save(configFile);
		} catch (IOException e) {
			xAuthLog.severe("Could not save message configuration to " + configFile, e);
		}
	}

	public void sendMessage(String node, CommandSender sender) {
		sendMessage(node, sender, null);
	}

	public void sendMessage(String node, CommandSender sender, String targetName) {
		String message = get(node, sender.getName(), targetName);

		for (String line : message.split("\n"))
			sender.sendMessage(line);
	}

	public String get(String node) {
		return get(node, null, null);		
	}

	private String get(String node, String playerName, String targetName) {
		return replace(config.getString(node, node), playerName, targetName);
	}

	private String replace(String message, String playerName, String targetName) {
		// colors
		message = message.replace("{BLACK}", "&0");
		message = message.replace("{DARKBLUE}", "&1");
		message = message.replace("{DARKGREEN}", "&2");
		message = message.replace("{DARKTEAL}", "&3");
		message = message.replace("{DARKRED}", "&4");
		message = message.replace("{PURPLE}", "&5");
		message = message.replace("{GOLD}", "&6");
		message = message.replace("{GRAY}", "&7");
		message = message.replace("{DARKGRAY}", "&8");
		message = message.replace("{BLUE}", "&9");
		message = message.replace("{BRIGHTGREEN}", "&a");
		message = message.replace("{TEAL}", "&b");
		message = message.replace("{RED}", "&c");
		message = message.replace("{PINK}", "&d");
		message = message.replace("{YELLOW}", "&e");
		message = message.replace("{WHITE}", "&f");
		message = message.replace("&", "\u00a7");

		// player
		if (playerName != null)
			message = message.replace("{PLAYER}", playerName);

		// target
		if (targetName != null)
			message = message.replace("{TARGET}", targetName);

		// TODO other replacement vars

		// newline
		message = message.replace("{NEWLINE}", "\n");
		return message;
	}
}