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

public class RegisterCommand implements CommandExecutor {
    private final xAuth plugin;

    public RegisterCommand(final xAuth plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        args = CommandLineTokenizer.tokenize(args);

        if (sender instanceof Player) {
            xAuthPlayer p = plugin.getPlayerManager().getPlayer((Player) sender);

            if ((plugin.getConfig().getBoolean("registration.require-email") && args.length < 2) || args.length < 1) {
                plugin.getMessageHandler().sendMessage("register.usage", p.getPlayer());
                return true;
            }

            String playerName = p.getPlayerName();
            String password = args[0];
            String email = args.length > 1 ? args[1] : null;

            Auth a = plugin.getAuthClass(p);
            boolean success = a.register(playerName, password, email);

            String response = a.getResponse();
            if (response != null)
                plugin.getMessageHandler().sendMessage(response, p.getPlayer());

            if (success) {
                if (!plugin.getConfig().getBoolean("registration.require-login"))
                    plugin.getPlayerManager().doLogin(p);

                xAuthLog.info(playerName + " has registered");
            }

            return true;
        }

        return false;
    }
}