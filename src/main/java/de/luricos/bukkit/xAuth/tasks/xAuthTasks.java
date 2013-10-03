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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class xAuthTasks {

    private Map<Integer, xAuthTask> playerTaskList = new HashMap<Integer, xAuthTask>();

	public xAuthTasks() {
	}

    /**
     * Used to kick a player after guests.timeout (default)
     *
     * @param playerName The players name
     */
    public void scheduleKickTimeoutTask(String playerName, int delay) {
        this.scheduleSyncDelayedTask(playerName, xAuthTask.xAuthTaskType.KICK_TIMEOUT, new TimeoutTask(playerName), delay * 20);
    }

    /**
     * Used to schedule a delayed protect task
     *
     * @param playerName the players name
     */
    public void scheduleDelayedProtectTask(String playerName) {
        this.scheduleSyncDelayedTask(playerName, xAuthTask.xAuthTaskType.DELAYED_PROTECT, new DelayedProtectTask(playerName), 1);
    }

    /**
     * Send a message delayed
     *
     * @param playerName the players name
     * @param node used message node
     */
    public void scheduleDelayedMessageTask(String playerName, String node) {
        DelayedMessageTask delayedMessageTask = new DelayedMessageTask(playerName, node);
        this.scheduleSyncDelayedTask(playerName, xAuthTask.xAuthTaskType.DELAYED_MESSAGE, delayedMessageTask, 1);
    }

    /**
     * Used to check the PlayerName against mojang paid check jsp
     * This method is used to block non-premium accounts to join the server when in PremiumMode
     *
     * @param playerName the players name
     */
    public void scheduleDelayedPremiumCheck(String playerName) {
        if (!xAuth.getPlugin().isPremiumMode())
            return;

        this.scheduleSyncDelayedTask(playerName, xAuthTask.xAuthTaskType.DELAYED_PREMIUM_CHECK, new DelayedPremiumCheck(playerName), 1);
    }

    public void scheduleSyncDelayedTask(String playerName, xAuthTask.xAuthTaskType taskType, BukkitRunnable runnable, long delay) {
        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(xAuth.getPlugin(), runnable, delay);
        this.playerTaskList.put(taskId, new xAuthTask(playerName, taskId, taskType));
    }


    public List<xAuthTask> getPlayerTasks(String playerName) {
        List<xAuthTask> playerTasks = new ArrayList<xAuthTask>();
        for (int taskId : this.playerTaskList.keySet()) {
            if (this.playerTaskList.get(taskId).getPlayerName().equalsIgnoreCase(playerName)) {
                playerTasks.add(this.playerTaskList.get(taskId));
            }
        }

        return playerTasks;
    }

    public xAuthTask getPlayerTask(String playerName, xAuthTask.xAuthTaskType taskType) {
        List<xAuthTask> playerTasks = getPlayerTasks(playerName);
        if (playerTasks.size() <= 0)
            return new xAuthTask();

        for (xAuthTask playerTask : playerTasks) {
            if ((!(playerTask.getType().equals(xAuthTask.xAuthTaskType.UNDEFINED))) && (!(playerTask.getType().equals(taskType))) && (!(playerTask.getPlayerName().equalsIgnoreCase(playerName)))) {
                continue;
            }

            return this.playerTaskList.get(playerTask.getTaskId());
        }

        return new xAuthTask();
    }

    public xAuthTask getPlayerTaskById(int taskId) {
        return this.playerTaskList.get(taskId);
    }

    public boolean hasTasks(String playerName){
        List<xAuthTask> playerTasks = this.getPlayerTasks(playerName);
        return playerTasks.size() > 0;
    }

    public boolean isTaskRunning(int taskId) {
        return Bukkit.getScheduler().isCurrentlyRunning(taskId);
    }

    public int countPlayerTasks(String playerName) {
        return this.getPlayerTasks(playerName).size();
    }

    public int countTasks() {
        return this.playerTaskList.size();
    }

    public void cancelTasks(String playerName) {
        this.cancelTasks(playerName, xAuthTask.xAuthTaskType.UNDEFINED);
    }

    public void cancelTasks(String playerName, xAuthTask.xAuthTaskType taskType) {
        for (xAuthTask playerTask : this.getPlayerTasks(playerName)) {
            this.cancelTask(playerTask.getPlayerName(), taskType);
        }
    }

    public void cancelTask(String playerName, xAuthTask.xAuthTaskType taskType) {
        if (!(this.hasTasks(playerName)))
            return;

        xAuthTask playerTask = this.getPlayerTask(playerName, taskType);
        int taskId = playerTask.getTaskId();

        Bukkit.getScheduler().cancelTask(taskId);
        this.playerTaskList.remove(taskId);
    }
}