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

import de.luricos.bukkit.xAuth.PlayerManager;
import de.luricos.bukkit.xAuth.permissions.PermissionManager;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author lycano
 */
public class PlayerRestrictionProvider {

    private PlayerManager playerManager;
    private PermissionManager permissionManager;
    private FileConfiguration config;

    public PlayerRestrictionProvider() {
    }

    /**
     * Get PlayerManager for internal use
     *
     * @return PlayerManager instance
     */
    protected PlayerManager getPlayerManager() {
        return xAuth.getPlugin().getPlayerManager();
    }

    /**
     * Get PermissionManager for internal use
     *
     * @return PermissionManager instance
     */
    protected PermissionManager getPermissionManager() {
        return xAuth.getPermissionManager();
    }

    /**
     * Get FileConfiguration for intenral use
     *
     * @return FileConfiguration instance
     */
    protected FileConfiguration getConfig() {
        return xAuth.getPlugin().getConfig();
    }
}
