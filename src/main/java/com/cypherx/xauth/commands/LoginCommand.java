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
package com.cypherx.xauth.commands;

import com.cypherx.xauth.auth.Auth;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.utils.xAuthLog;
import com.cypherx.xauth.xAuthPlayer;
import com.martiansoftware.jsap.CommandLineTokenizer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {
    private final xAuth plugin;

    public LoginCommand(final xAuth plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        args = CommandLineTokenizer.tokenize(args);

        if (sender instanceof Player) {
            xAuthPlayer p = plugin.getPlayerManager().getPlayer((Player) sender);

            if (args.length < 1) {
                plugin.getMessageHandler().sendMessage("login.usage", p.getPlayer());
                return true;
            }

            String playerName = p.getPlayerName();
            String password = args[0];

            Auth a = plugin.getAuthClass(p);
            boolean passChecks = a.login(playerName, password);
            String response = a.getResponse();

            if (passChecks) {
                boolean success = plugin.getPlayerManager().doLogin(p);
                if (success) {
                    if (plugin.isAuthURL() && plugin.getConfig().getBoolean("authurl.broadcast-login") && response != null && response != "")
                        plugin.getServer().broadcastMessage(response);
                    response = "login.success";
                    a.online(p.getPlayerName());
                    xAuthLog.info(playerName + " has logged in");
                } else
                    response = "login.error.general";
            }

            if (response != null)
                plugin.getMessageHandler().sendMessage(response, p.getPlayer());

            return true;
        }

        return false;
    }
}