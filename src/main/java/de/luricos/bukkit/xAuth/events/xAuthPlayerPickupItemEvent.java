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
package de.luricos.bukkit.xAuth.events;

import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.event.HandlerList;

/**
 * @author lycano
 */
public class xAuthPlayerPickupItemEvent extends xAuthEvent {
    protected Action action;
    protected xAuthPlayer.Status status;

    private static final HandlerList handlers = new HandlerList();

    public xAuthPlayerPickupItemEvent(Action action, xAuthPlayer.Status status) {
        super(action.toString());

        this.action = action;
        this.status = status;
    }

    public Action getAction() {
        return this.action;
    }

    public xAuthPlayer.Status getStatus() {
        return this.status;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum Action {
        PICKUP_DENIED
    }
}
