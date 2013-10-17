/*
 * xAuth for Bukkit
 * Copyright (C) 2012 Lycano <https://github.com/lycano/xAuth/>
 *
 * Copyright (C) 2011 CypherX <https://github.com/CypherX/xAuth/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.luricos.bukkit.xAuth;

import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.utils.xAuthUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MessageHandler {
    private final xAuth plugin;
    private final String fileName = "messages.yml";
    private File configFile = null;
    private FileConfiguration config = null;

    public MessageHandler(final xAuth plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), fileName);
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }

        return config;
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), fileName);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            config.setDefaults(defConfig);
        }
    }

    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }

        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            xAuthLog.severe("Could not save message configuration to " + configFile, e);
        }
    }

    public void sendMessage(String node, CommandSender sender) {
        this.sendMessage(node, sender, null);
    }

    public void sendMessage(String node, CommandSender sender, String targetName) {
        if (sender != null) {
            String message = getNode(node, sender.getName(), targetName);

            for (String line : message.split("\n"))
                sender.sendMessage(line);
        }
    }

    public String getNode(String node) {
        return getNode(node, null, null);
    }

    public String getNode(String node, String playerName, String targetName) {
        return replace(config.getString(node, node), playerName, targetName);
    }

    private String replace(String message, String playerName, String targetName) {
        // player
        if (playerName != null)
            message = message.replace("{PLAYER}", playerName);

        // target
        if (targetName != null)
            message = message.replace("{TARGET}", targetName);

        // TODO other replacement vars
        message = message.replace("{PWMINLENGTH}", String.valueOf(plugin.getConfig().getInt("password.min-length")));

        // newline
        message = message.replace("{NEWLINE}", "\n");

        // replace colors
        message = xAuthUtils.replaceColors(message);

        return message;
    }
}