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
 *//*

package com.luricos.bukkit.plugins;

import xAuth;
import xAuthLog;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class xPermissions {
    private static Permission perms = null;
    //public static String groupGuest;
    //public static String groupAuth;

    public static void init(xAuth plugin) {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null)
            return;

        RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        if (perms != null)
            xAuthLog.info("Vault found, advanced permission support enabled");

        //groupGuest = plugin.getConfig().getString("groups.guest");
        //groupAuth = plugin.getConfig().getString("groups.authenticated");
    }

    public static boolean has(CommandSender sender, String permission) {
        if (sender instanceof Player)
            return sender.hasPermission(permission);
        else
            return (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender);
    }

    */
/*public static void addGroup(Player player, String group) {
         if (perms == null)
             xAuthLog.warning("Vault plugin not found, group cannot be added");
         else if (perms.getName().equals("SuperPerms"))
             xAuthLog.warning("SuperPerms does not support groups.");
         else
             perms.playerAddGroup(player.getWorld(), player.getName(), group);
     }

     public static void removeGroup(Player player, String group) {
         if (perms == null)
             xAuthLog.warning("Vault plugin not found, group cannot be removed");
         else if (perms.getName().equals("SuperPerms"))
             xAuthLog.warning("SuperPerms does not support groups.");
         else
             perms.playerRemoveGroup(player.getWorld(), player.getName(), group);
     }*//*


    */
/*public static void setGroup(Player p, GroupType group) {
         if (perms == null)
             xAuthLog.warning("Vault plugin not found, group cannot be changed");
         else if (perms.getName().equals("SuperPerms"))
             xAuthLog.warning("SuperPerms does not support groups");
         else {
             switch (group) {
             case GUEST:
                 perms.playerRemoveGroup(p.getWorld(), p.getName(), groupAuth);
                 perms.playerAddGroup(p.getWorld(), p.getName(), groupGuest);
                 break;
             case AUTHENTICATED:
                 perms.playerRemoveGroup(p.getWorld(), p.getName(), groupGuest);
                 perms.playerAddGroup(p.getWorld(), p.getName(), groupAuth);
                 break;
             }
         }
     }

     public enum GroupType {
         GUEST,
         AUTHENTICATED
     }*//*

}*/
