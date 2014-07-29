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
package de.luricos.bukkit.xAuth.tasks;

import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author lycano
 */
public class TimeoutTask extends BukkitRunnable {

    private String playerName = "";
    private String messageNode = "";

    public TimeoutTask(String playerName) {
        this(playerName, "misc.timeout");
    }

    public TimeoutTask(String playerName, String messageNode) {
        this.playerName = playerName;
        this.messageNode = messageNode;
    }

    @Override
    public void run() {
        if (Bukkit.getPlayerExact(this.playerName) == null)
            return;

        Bukkit.getPlayerExact(this.playerName).kickPlayer(xAuth.getPlugin().getMessageHandler().getNode(this.messageNode));
    }
}
