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
package com.cypherx.xauth;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.Timestamp;

public class xAuthPlayer {
    private String playerName;
    private int accountId = 0;
    private Status status = Status.Guest;
    private PlayerData playerData;
    private Timestamp lastNotifyTime;
    private Timestamp loginTime;
    private GameMode gameMode;
    private boolean creativeMode;
    private int timeoutTaskId = -1;
    private boolean isProtected = false;
    private boolean isLocked = true;
    private Timestamp connectTime;

    public xAuthPlayer(String playerName) {
        this(playerName, -1, true, Status.Guest);
    }

    public xAuthPlayer(String playerName, int accountId) {
        this(playerName, accountId, true, Status.Guest);
    }

    public xAuthPlayer(String playerName, int accountId, boolean locked, Status status) {
        this.playerName = playerName;
        this.accountId = accountId;
        this.isLocked = locked;
        this.status = status;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Player getPlayer() {
        return Bukkit.getPlayerExact(playerName);
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public boolean isLocked() {
        return this.isLocked;
    }

    public void lockPlayer() {
        this.setIsLocked(true);
    }

    public void activatePlayer() {
        this.setIsLocked(false);
    }

    public void setIsLocked(boolean locked) {
        this.isLocked = locked;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public void setPlayerData(PlayerData playerData) {
        this.playerData = playerData;
    }

    public Timestamp getLastNotifyTime() {
        return lastNotifyTime;
    }

    public void setLastNotifyTime(Timestamp lastNotifyTime) {
        this.lastNotifyTime = lastNotifyTime;
    }

    public Timestamp getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Timestamp loginTime) {
        this.loginTime = loginTime;
    }

    public boolean isCreativeMode() {
        return (this.gameMode.equals(GameMode.CREATIVE));
    }

    public boolean isAdventureMode() {
        return (this.gameMode.equals(GameMode.ADVENTURE));
    }

    public boolean isSurvivalMode() {
        return (this.gameMode.equals(GameMode.SURVIVAL));
    }

    public void setGameMode(GameMode newGameMode) {
        this.gameMode = newGameMode;
    }

    public int getTimeoutTaskId() {
        return timeoutTaskId;
    }

    public void setTimeoutTaskId(int timeoutTaskId) {
        this.timeoutTaskId = timeoutTaskId;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public Timestamp getConnectTime() {
        return connectTime;
    }

    public void setConnectTime(Timestamp connectTime) {
        this.connectTime = connectTime;
    }

    public boolean isGuest() {
        return status == Status.Guest;
    }

    public boolean isOnline() {
        Player player = this.getPlayer();
        return ((player != null) && (player.isOnline()));
    }

    public boolean isRegistered() {
        return status != Status.Guest;
    }

    public boolean isAuthenticated() {
        return status == Status.Authenticated;
    }

    public String getIPAddress() {
        Player player = getPlayer();
        if (player == null)
            return null;

        try {
            return player.getAddress().getAddress().getHostAddress();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public Location getLocation() {
        return getPlayer().getLocation();
    }

    public enum Status {
        Guest, // not registered
        Registered, // registered but not logged in
        Authenticated // logged in
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }
}