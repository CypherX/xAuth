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

import de.luricos.bukkit.xAuth.events.xAuthQuitEvent;
import de.luricos.bukkit.xAuth.utils.CommandLineTokenizer;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuitCommand extends xAuthCommand implements CommandExecutor {

    public QuitCommand() {
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        args = CommandLineTokenizer.tokenize(args);

        if (!this.isAllowedCommand(sender, "quit.permission", "quit"))
            return true;

        Player player = (Player) sender;
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(player);
        String playerName = xp.getName();
        String response = null;
        boolean kickPlayer = false;

        if (xp.isAuthenticated()) {
            boolean success = xAuth.getPlugin().getPlayerManager().deleteSession(xp.getAccountId());
            if (success) {
                xAuth.getPlugin().getPlayerManager().protect(xp);
                xp.setStatus(xAuthPlayer.Status.REGISTERED);
                xAuth.getPlugin().getAuthClass(xp).offline(playerName);
                response = "quit.success";
                kickPlayer = true;
                xAuthLog.info(playerName + " logged out");
            } else {
                response = "quit.error.general";
            }
        } else {
            response = "quit.error.logged";
        }

        if (kickPlayer) {
            this.callEvent(xAuthQuitEvent.Action.PLAYER_QUIT, xp.getStatus());

            xp.getPlayer().kickPlayer(this.getMessageHandler().getNode(response));
        } else {
            this.getMessageHandler().sendMessage(response, xp.getPlayer());
        }

        return true;
    }
}