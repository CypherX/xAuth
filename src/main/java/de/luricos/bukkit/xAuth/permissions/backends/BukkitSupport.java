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
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

/**
 * @author lycano
 */
public class BukkitSupport extends PermissionBackend {

    public BukkitSupport(PermissionManager manager, Configuration config, String providerName) {
        super(manager, config, providerName);
    }

    @Override
    public void initialize() {
        xAuthLog.info("Attached to Bukkit");
    }

    @Override
    public void reload() {
        xAuthLog.info("Detached from BukkitSupport");

        this.initialize();
    }

    @Override
    public boolean hasPermission(Player player, String permissionString) {
        return player.hasPermission(permissionString);
    }

    @Override
    public boolean hasGroup(Player player, String groupName) {
        return true;
    }

    @Override
    public boolean hasGroup(String playerName, String groupName) {
        return true;
    }

    @Override
    public void joinGroup(Player player, String groupName) {
    }

    @Override
    public void joinGroup(String playerName, String groupName) {
    }
}
