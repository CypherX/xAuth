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
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageHandler {
    private final xAuth plugin;
    private final String fileName = "messages.yml";
    private File configFile = null;
    private FileConfiguration config = null;

    public MessageHandler(final xAuth plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), this.fileName);
        this.config = YamlConfiguration.loadConfiguration(this.configFile);

        this.updateConfig();
    }

    public void updateConfig() {
        YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(this.plugin.getResource(this.fileName));

        // check if current messages file is different from resource messages. If not create a backup of the current one.
        if ((this.config.options().header() == null) || (!(this.config.options().header().equals(newConfig.options().header())))) {
            String backupDateString = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date().getTime());

            xAuthLog.info("New messages.yml found in plugin. Creating backup of the current one.");
            try {
                this.config.save(new File(plugin.getDataFolder(), "messages-" + backupDateString + ".yml"));

                this.config = newConfig;
                this.saveConfig();
            } catch (IOException e) {
                xAuthLog.severe("Could not save a backup of message configuration to messages-" + backupDateString + ".yml", e);
            }
        }
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public void reloadConfig() {
        if (this.config == null) {
            this.configFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        this.config = YamlConfiguration.loadConfiguration(this.configFile);

        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource("messages.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.config.setDefaults(defConfig);
        }
    }

    public void saveConfig() {
        if (this.config == null || this.configFile == null) {
            return;
        }

        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            xAuthLog.severe("Could not save message configuration to " + this.configFile, e);
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