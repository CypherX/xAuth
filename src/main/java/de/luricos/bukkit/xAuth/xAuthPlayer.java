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

import de.luricos.bukkit.xAuth.password.PasswordType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.Timestamp;

public class xAuthPlayer {
    private String playerName;
    private int accountId = 0;
    private Status status = Status.GUEST;
    private PlayerData playerData;
    private Timestamp lastNotifyTime;
    private Timestamp loginTime;
    private GameMode gameMode;
    private boolean isProtected = false;
    private boolean isLocked = true;
    private boolean isReset = false;
    private boolean isPremium = false;
    private PasswordType pwType;
    private Timestamp connectTime;

    public xAuthPlayer(String playerName) {
        this(playerName, -1, true, false, Status.GUEST, PasswordType.DEFAULT.getTypeId(), false, Bukkit.getDefaultGameMode());
    }

    public xAuthPlayer(String playerName, int accountId) {
        this(playerName, accountId, true, false, Status.GUEST, PasswordType.DEFAULT.getTypeId(), false, Bukkit.getDefaultGameMode());
    }

    public xAuthPlayer(String playerName, int accountId, boolean locked, boolean isReset, Status status, int pwType, boolean isPremium, GameMode gameMode) {
        this.playerName = playerName;
        this.accountId = accountId;
        this.isLocked = locked;
        this.isReset = isReset;
        this.isPremium = isPremium;
        this.status = status;
        this.pwType = PasswordType.getType(pwType);
        this.gameMode = gameMode;
    }

    public String getName() {
        return playerName;
    }

    public String getPlayerName() {
        return getPlayer().getName();
    }

    public String getDisplayName() {
        return getPlayer().getDisplayName();
    }

    public Player getPlayer() {
        return Bukkit.getPlayerExact(this.playerName);
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

    public boolean isReset() {
        return this.isReset;
    }

    public void setReset(boolean mode) {
        this.isReset = mode;
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
        return status == Status.GUEST;
    }

    public boolean isOnline() {
        Player player = this.getPlayer();
        return ((player != null) && (player.isOnline()));
    }

    public boolean isRegistered() {
        return status != Status.GUEST;
    }

    public boolean isPremium() {
        return this.isPremium;
    }

    public void setPremium(final boolean premium) {
        this.isPremium = premium;
    }

    public boolean isAuthenticated() {
        return status == Status.AUTHENTICATED;
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

    public GameMode getGameMode() {
        return this.gameMode;
    }

    public PasswordType getPasswordType() {
        return this.pwType;
    }

    public enum Status {
        GUEST, // not registered
        REGISTERED, // registered but not logged in
        AUTHENTICATED // logged in
    }
}