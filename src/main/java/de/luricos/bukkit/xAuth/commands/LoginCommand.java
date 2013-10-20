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
package de.luricos.bukkit.xAuth.commands;

import de.luricos.bukkit.xAuth.auth.AuthMethod;
import de.luricos.bukkit.xAuth.events.xAuthLoginEvent;
import de.luricos.bukkit.xAuth.utils.CommandLineTokenizer;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand extends xAuthCommand implements CommandExecutor {

    public LoginCommand() {
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        args = CommandLineTokenizer.tokenize(args);

        if (!this.isAllowedCommand(sender, "login.permission", "login"))
            return true;

        Player player = (Player) sender;
        if (xAuth.getPlugin().getPlayerManager().getPlayer(player).isAuthenticated()) {
            this.getMessageHandler().sendMessage("login.error.authenticated", player);
            return true;
        }

        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(player, true);

        if (args.length < 1) {
            this.getMessageHandler().sendMessage("login.usage", xp.getPlayer());
            return true;
        }

        String playerName = xp.getName();
        String password = args[0];

        AuthMethod a = xAuth.getPlugin().getAuthClass(xp);
        boolean passChecks = a.login(playerName, password);
        String response = a.getResponse();

        if (passChecks) {
            boolean success = xAuth.getPlugin().getPlayerManager().doLogin(xp);
            if (success) {
                if (xAuth.getPlugin().isAuthURL() && xAuth.getPlugin().getConfig().getBoolean("authurl.broadcast-login") && response != null && response != "")
                    xAuth.getPlugin().getServer().broadcastMessage(response);
                response = "login.success";
                a.online(playerName);

                this.callEvent(xAuthLoginEvent.Action.PLAYER_LOGIN, xp.getStatus());

                xAuthLog.info(playerName + " authenticated");
            } else {
                response = "login.error.general";
            }
        }

        if (response != null)
            this.getMessageHandler().sendMessage(response, xp.getPlayer());

        return true;
    }
}