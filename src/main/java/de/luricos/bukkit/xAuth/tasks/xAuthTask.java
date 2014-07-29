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

/**
 * @author lycano
 */
public class xAuthTask {

    private int taskId = -1;
    private xAuthTaskType taskType = xAuthTaskType.UNDEFINED;
    private String playerName = null;

    public enum xAuthTaskType {
        UNDEFINED, DELAYED_PROTECT, DELAYED_MESSAGE, DELAYED_PREMIUM_CHECK, KICK_TIMEOUT
    }

    public xAuthTask() {
    }

    public xAuthTask(String playerName, int taskId, xAuthTaskType taskType) {
        this.taskId = taskId;
        this.playerName = playerName;
        this.taskType = taskType;
    }

    public int getTaskId() {
        return this.taskId;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public xAuthTaskType getType() {
        return this.taskType;
    }

}