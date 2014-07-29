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
package de.luricos.bukkit.xAuth.permissions.backends;

import de.luricos.bukkit.xAuth.permissions.PermissionBackend;
import de.luricos.bukkit.xAuth.permissions.PermissionManager;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * @author lycano
 */
public class GroupManagerSupport extends PermissionBackend {

    protected org.anjocaido.groupmanager.GroupManager provider = null;

    public GroupManagerSupport(PermissionManager manager, Configuration config, String providerName) {
        super(manager, config, providerName);
    }

    @Override
    public void initialize() {
        Plugin testPlugin = Bukkit.getServer().getPluginManager().getPlugin(providerName);
        if ((testPlugin != null) && (Bukkit.getServer().getPluginManager().isPluginEnabled(providerName))) {
            provider = (org.anjocaido.groupmanager.GroupManager) testPlugin;
        }
        xAuthLog.info("Attached to GroupManager");
    }

    @Override
    public void reload() {
        provider = null;
        xAuthLog.info("Detached from GroupManagerSupport");

        this.initialize();
    }

    @Override
    public boolean hasPermission(Player player, final String permissionString)
    {
        AnjoPermissionsHandler handler = provider.getWorldsHolder().getWorldPermissions(player);
        return handler != null && handler.has(player, permissionString);
    }

    @Override
    public boolean hasGroup(Player player, String groupName) {
        AnjoPermissionsHandler handler = provider.getWorldsHolder().getWorldPermissions(player);
        return handler != null && handler.inGroup(player.getName(), groupName);
    }

    @Override
    public boolean hasGroup(String playerName, String groupName) {
        return true;
    }

    @Override
    public void joinGroup(Player player, String groupName) {
        /*
        if (this.hasGroup(player, groupName))
            return;

        OverloadedWorldHolder worldHolder = provider.getWorldsHolder().getWorldData(player.getWorld().getName());
        worldHolder.createGroup(groupName);
        worldHolder.createUser(player.getName());

        worldHolder.getUser(player.getName()).setGroup(worldHolder.getGroup(groupName));
        xAuthLog.info("Essentials user '" + player.getName() + "' created and joined group '" + groupName + "'");
        */
    }

    @Override
    public void joinGroup(String playerName, String groupName) {
    }

}
