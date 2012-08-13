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
package com.cypherx.xauth.permissions.backends;

import com.cypherx.xauth.permissions.PermissionBackend;
import com.cypherx.xauth.utils.xAuthLog;
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

    public GroupManagerSupport(com.cypherx.xauth.permissions.PermissionManager manager, Configuration config, String providerName) {
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
    }

    @Override
    public boolean hasPermission(Player player, final String permissionString)
    {
        AnjoPermissionsHandler handler = provider.getWorldsHolder().getWorldPermissions(player);
        return handler != null && handler.has(player, permissionString);
    }
}
