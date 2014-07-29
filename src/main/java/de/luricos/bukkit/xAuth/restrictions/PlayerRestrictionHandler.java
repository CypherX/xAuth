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
package de.luricos.bukkit.xAuth.restrictions;

import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author lycano
 */
public class PlayerRestrictionHandler extends PlayerRestrictionProvider {

    private xAuthPlayer xPlayer;
    private Object[] obj;
    private xAuthPlayer.Status playerStatus;

    private PlayerRestrictionNode restrictionNode;
    private String permissionString;

    private boolean debugPermissions = false;
    private boolean guestRestrictFallback = true; // default deny for guests
    private boolean restrictDefault = false; // default allow for logged

    public PlayerRestrictionHandler(final Player player, final String eventName, Object... obj) {
        this.xPlayer = getPlayerManager().getPlayer(player.getName());
        this.obj = obj;
        this.restrictionNode =  new PlayerRestrictionNode(eventName);
        this.playerStatus = xPlayer.getStatus();
        this.debugPermissions = getConfig().getBoolean("permissions.debug", debugPermissions);
    }

    /**
     * Get xAuthPlayer
     *
     * @return xAuthPlayer
     */
    public xAuthPlayer getAuthPlayer() {
        return this.xPlayer;
    }

    /**
     * Fetch player via xAuthPlayer class
     *
     * @return Player Bukkit.getPlayerExact(String playerName)
     */
    public Player getPlayer() {
        return this.xPlayer.getPlayer();
    }

    public Object getObject() {
        return this.obj;
    }

    public xAuthPlayer.Status getPlayerStatus() {
        return playerStatus;
    }

    public boolean isGuest() {
        return getAuthPlayer().isGuest();
    }

    public boolean isAuthenticated() {
        return getAuthPlayer().isAuthenticated();
    }

    public boolean isRegistered() {
        return getAuthPlayer().isRegistered();
    }

    public PlayerRestrictionNode getRestrictionNode() {
        return this.restrictionNode;
    }

    /**
     * Get allowed permission node as string
     *
     * @return String allowed permission node
     */
    public String getAllowedPermissionString() {
        return this.getAllowedPermissionNode();
    }

    public String getAllowedPermissionNode() {
        return ((isAuthenticated() ? "xauth." : "guest.") + this.getRestrictionNode().getAllowedPermissionNode(obj));
    }

    /**
     * Get denied permission node as string
     *
     * @return String denied permission node
     */
    public String getDeniedPermissionString() {
        return this.getDeniedPermissionNode();
    }

    public String getDeniedPermissionNode() {
        return ((isAuthenticated() ? "xauth." : "guest.") +  this.getRestrictionNode().getDeniedPermissionNode(obj));
    }

    /**
     * Use this to check to check permissions depending on the players status
     *
     * @return boolean true if not restricted false otherwise
     */
    public boolean hasPermission() {
        boolean result = false;
        switch (playerStatus) {
            case GUEST:
            case REGISTERED:
                result = !this.hasGuestRestriction();

                sendDelayedDebugMessage("[HQ Guest] ConfigNode: '" + this.getGuestConfigurationString() + "',  result: " + result + "\n" +
                                  "Event: '" + this.getRestrictionNode().getEventName() + "', Section: '" + this.getRestrictionNode().getEventType() + "', Action: '" + this.getRestrictionNode().getAction() +"'");
                break;
            case AUTHENTICATED:
                result = !this.isRestricted();

                sendDelayedDebugMessage("[HQ Authed] PermissionNode: '" + this.getPermissionString() + "',  result: " + result + "\n" +
                                  "Event: '" + this.getRestrictionNode().getEventName() + "', Section: '" + this.getRestrictionNode().getEventType() + "', Action: '" + this.getRestrictionNode().getAction() + "'");
                break;
        }

        return result;
    }

    /**
     * Guest has restrictions enabled
     *
     * @return boolean true if guest restrictions enabled
     */
    private boolean hasGuestRestriction() {
        return this.getGuestConfigurationNode();
    }

    private boolean getGuestConfigurationNode() {
        return this.getConfig().getBoolean(this.getGuestConfigurationString(), this.guestRestrictFallback);
    }

    public String getGuestConfigurationString() {
        return "guest." + this.getRestrictionNode().getDeniedPermissionNode(obj);
    }

    /**
     * Player is restricted via permissions
     * Note: This system does not depend on guest restriction node configuration
     *
     * @return boolean true if the player is restricted
     *                 false if denied was found later or nothing was found (no permission set)
     */
    private boolean isRestricted() {
        boolean restrict = this.restrictDefault;

        // check if the user is allowed to do so else check for denied flag if nothing found allow actions, restrict = false
        this.permissionString = this.getAllowedPermissionString();
        if (getPermissionManager().has(getPlayer(), this.getAllowedPermissionString())) {
            this.permissionString = this.getAllowedPermissionString();
            restrict = false;
        } else  if (getPermissionManager().has(getPlayer(), this.getDeniedPermissionString())) {
            this.permissionString = this.getDeniedPermissionString();
            restrict = true;
        }

        return restrict;
    }

    public String getPermissionString() {
        return this.permissionString;
    }

    private void sendDelayedDebugMessage(final String msg) {
        if (!debugPermissions)
            return;

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(xAuth.getPlugin(), new Runnable() {
            public void run() {
                xAuthLog.info(msg);
            }
        }, 3);
    }
}
