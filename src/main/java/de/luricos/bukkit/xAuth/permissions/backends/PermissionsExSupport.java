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
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * The Class PermissionsExSupport.
 *
 * Add features for PermissionsEx implementation
 * 
 * @author lycano
 */

public class PermissionsExSupport extends PermissionBackend {

    protected PermissionManager provider = null;

    public PermissionsExSupport(de.luricos.bukkit.xAuth.permissions.PermissionManager manager, Configuration config, String providerName) {
        super(manager, config, providerName);
    }

    @Override
    public void initialize() {
        Plugin testPlugin = Bukkit.getServer().getPluginManager().getPlugin(getProviderName());
        if ((testPlugin != null) && (Bukkit.getServer().getPluginManager().isPluginEnabled(getProviderName()))) {
            final String version = testPlugin.getDescription().getVersion();
            checkPermissionsVersion(version);

            try {
                provider = PermissionsEx.getPermissionManager();
                xAuthLog.info("Attached to " + providerName + " version " + version);
            } catch (final ClassCastException e) {
                xAuthLog.warning("Failed to get Permissions Handler. Defaulting to built-in permissions.");
            }
        } else {
            xAuthLog.info("Permission Plugin not yet available. Defaulting to built-in permissions until Permissions is loaded.");
        }
    }

    @Override
    public void reload() {
        provider = null;
        xAuthLog.info("Detached from Permissions plugin '" + getProviderName() + "'.");

        this.initialize();
    }

    /**
     * Check permissions version.
     *
     * @param version
     *            the version
     */
    private static void checkPermissionsVersion(String version) {
        if (!isSupportedVersion(version)) {
            xAuthLog.warning("Not supported version. Recommended is at least 1.18");
        }
    }

    public static boolean isSupportedVersion(String verIn) {
        return isSupportedVersion(verIn, 1.18);
    }

    public static boolean isSupportedVersion(String verIn, Double checkVer) {
        String comp1 = verIn.replaceAll("\\.", "");
        int subVCount = verIn.length() - comp1.length();

        if ((subVCount < 2) && (Double.parseDouble(verIn) >= checkVer))
            return true;

        if ((subVCount < 2) && (Double.parseDouble(verIn) < checkVer))
            return false;

        int firstMatch = verIn.indexOf("");
        String verOut = verIn.substring(0, firstMatch) + "" + comp1.substring(firstMatch);

        return Double.parseDouble(verOut) >= checkVer;
    }

    @Override
    public boolean hasPermission(Player player, String permissionString) {
        return provider.has(player, permissionString);
    }

    @Override
    public boolean hasGroup(Player player, String groupName) {
        return hasGroup(player.getName(), groupName);
    }

    public boolean hasGroup(String playerName, String groupName) {
        PermissionUser user = provider.getUser(playerName);
        return user != null && user.getAllGroups().containsKey(groupName);
    }

    @Override
    public void joinGroup(Player player, String groupName) {
        joinGroup(player.getName(), groupName);
    }

    @Override
    public void joinGroup(String playerName, String groupName) {
        PermissionUser user = provider.getUser(playerName);
        if (hasGroup(playerName, groupName)) {
            xAuthLog.info("PermissionsEx user '" + playerName + "' is already assigned to group '" + groupName + "'");
            return;
        }

        user.addGroup(groupName);
        user.setGroups(new String[]{groupName});
        xAuthLog.info("PermissionsEx user '" + playerName + "' assigned to group '" + groupName + "'");
    }

}
