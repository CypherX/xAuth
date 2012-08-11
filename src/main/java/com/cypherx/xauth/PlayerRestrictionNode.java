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

import com.cypherx.xauth.utils.xAuthLog;
import org.bukkit.event.Event;

/**
 * @author lycano
 */
public class PlayerRestrictionNode {

    protected String[] splitEventName;
    protected String eventName;
    protected String eventAction;

    protected String restrictNode;
    protected String allowNode;

    private String eventType;

    public PlayerRestrictionNode(Event event) {
        this.splitEventName = event.getEventName().split("\\.");
        this.eventName = splitEventName[this.splitEventName.length - 1];
        this.splitEventName = eventName.split("(?=\\p{Upper})");

        // first element (0) will always be empty
        this.eventType = this.splitEventName[1].toLowerCase(); //player, block, entity
        this.eventAction = this.splitEventName[2].toLowerCase(); // move, place, target, etc.

        this.restrictNode = String.format("restrict.%s.%s", this.eventType, this.eventAction);
        this.allowNode = String.format("allow.%s.%s", this.eventType, this.eventAction);
    }

    public String getRestrictionNode() {
        xAuthLog.fine("Requested RestrictionNode: " + restrictNode);
        return restrictNode;
    }

    public String getEventType() {
        xAuthLog.fine("Requested RestrictionEventType: " + eventType);
        return eventType;
    }

    public String getAction() {
        xAuthLog.fine("Requested RestrictionEventAction: " + eventAction);
        return eventAction;
    }

    public String getEventName() {
        xAuthLog.fine("Requested RestrictionEventName: " + eventName);
        return eventName;
    }

    public String getAllowNode() {
        xAuthLog.fine("Requested RestrictionAllowNode: " + allowNode);
        return allowNode;
    }

}
