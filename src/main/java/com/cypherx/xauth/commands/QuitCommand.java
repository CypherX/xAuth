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

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.xAuthPlayer.Status;
import com.martiansoftware.jsap.CommandLineTokenizer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuitCommand implements CommandExecutor {

    public QuitCommand() {
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        args = CommandLineTokenizer.tokenize(args);

        if (sender instanceof Player) {
            xAuthPlayer p = xAuth.getPlugin().getPlayerManager().getPlayer((Player) sender);
            String response = null;
            boolean kickPlayer = false;

            if (p.isAuthenticated()) {
                boolean success = xAuth.getPlugin().getPlayerManager().deleteSession(p.getAccountId());
                if (success) {
                    xAuth.getPlugin().getPlayerManager().protect(p);
                    p.setStatus(Status.Registered);
                    xAuth.getPlugin().getAuthClass(p).offline(p.getPlayerName());
                    response = "quit.success";
                    kickPlayer = true;
                } else {
                    response = "quit.error.general";
                }
            } else {
                response = "quit.error.logged";
            }

            if (kickPlayer) {
                p.getPlayer().kickPlayer(xAuth.getPlugin().getMessageHandler().getNode(response));
            } else {
                xAuth.getPlugin().getMessageHandler().sendMessage(response, p.getPlayer());
            }
            return true;
        }

        return false;
    }
}