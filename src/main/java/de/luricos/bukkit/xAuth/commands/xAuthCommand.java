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

import de.luricos.bukkit.xAuth.MessageHandler;
import de.luricos.bukkit.xAuth.events.*;
import de.luricos.bukkit.xAuth.restrictions.PlayerRestrictionHandler;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * @author lycano
 */
public class xAuthCommand {

    private MessageHandler messageHandler = xAuth.getPlugin().getMessageHandler();

    protected void callEvent(final xAuthChangePasswordEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthChangePasswordEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthChangePasswordEvent(action, status));
    }

    protected void callEvent(final xAuthLoginEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthLoginEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthLoginEvent(action, status));
    }

    protected void callEvent(final xAuthLogoutEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthLogoutEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthLogoutEvent(action, status));
    }

    protected void callEvent(final xAuthQuitEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthQuitEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthQuitEvent(action, status));
    }

    protected void callEvent(final xAuthRegisterEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthRegisterEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthRegisterEvent(action, status));
    }

    protected void callEvent(final xAuthResetPasswordEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthResetPasswordEvent.Action action, final xAuthPlayer.Status status) {
        this.callEvent(new xAuthResetPasswordEvent(action, status));
    }

    protected boolean isAllowedCommand(final CommandSender sender, final String messageNode, final String... command) {
        return (sender instanceof ConsoleCommandSender) || this.isAllowedCommand((Player) sender, messageNode, command);
    }

    protected boolean isAllowedCommand(final Player player, final String messageNode, final String... command) {
        boolean allowed = new PlayerRestrictionHandler(player, "PlayerCommandPreProcessEvent", command).hasPermission();
        if (!allowed)
            xAuth.getPlugin().getMessageHandler().sendMessage(messageNode, player);

        return allowed;
    }

    protected MessageHandler getMessageHandler() {
        return this.messageHandler;
    }
}
