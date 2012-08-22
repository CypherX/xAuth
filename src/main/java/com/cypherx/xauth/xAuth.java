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

import com.cypherx.xauth.auth.Auth;
import com.cypherx.xauth.auth.AuthSQL;
import com.cypherx.xauth.auth.AuthURL;
import com.cypherx.xauth.commands.*;
import com.cypherx.xauth.database.DatabaseController;
import com.cypherx.xauth.exceptions.xAuthNotAvailable;
import com.cypherx.xauth.listeners.xAuthBlockListener;
import com.cypherx.xauth.listeners.xAuthEntityListener;
import com.cypherx.xauth.listeners.xAuthEventListener;
import com.cypherx.xauth.listeners.xAuthPlayerListener;
import com.cypherx.xauth.password.PasswordHandler;
import com.cypherx.xauth.permissions.PermissionBackend;
import com.cypherx.xauth.permissions.PermissionManager;
import com.cypherx.xauth.permissions.backends.BukkitSupport;
import com.cypherx.xauth.permissions.backends.GroupManagerSupport;
import com.cypherx.xauth.permissions.backends.PermissionsExSupport;
import com.cypherx.xauth.strike.StrikeManager;
import com.cypherx.xauth.updater.Updater;
import com.cypherx.xauth.utils.Utils;
import com.cypherx.xauth.utils.xAuthLog;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Level;

public class xAuth extends JavaPlugin {
    private DatabaseController databaseController;
    private MessageHandler messageHandler;
    private PlayerManager playerManager;
    private PlayerDataHandler playerDataHandler;
    private PasswordHandler passwordHandler;
    private LocationManager locationManager;
    private StrikeManager strikeManager;

    protected PermissionManager permissionManager;
    protected Configuration config;

    private String h2Version = "1.3.164";
    private String libURLPath = "http://bukkit.luricos.de/plugins/xAuth/lib/";

    public void onLoad() {
        this.initConfiguration();
        xAuthLog.initLogger();
    }

    public void onDisable() {
        if (databaseController != null) {
            for (Player p : getServer().getOnlinePlayers()) {
                xAuthPlayer xp = playerManager.getPlayer(p);
                if (xp.isProtected())
                    playerManager.unprotect(xp);
            }

            databaseController.close();
        }

        // remove permissionManager instance
        if (this.permissionManager != null) {
            this.permissionManager.end();
        }

        this.reloadConfig();
        this.saveConfig();

        // free config object
        config = null;
        messageHandler = null;

        xAuthLog.info(String.format("v%s Disabled!", getDescription().getVersion()));
    }

    public void onEnable() {
        // Create any required directories
        getDataFolder().mkdirs();

        // Create/load configuration and customizable messages
        loadConfiguration();

        // Disable plugin if auto-disable is set to true and server is running in online-mode
        if (getConfig().getBoolean("main.auto-disable") && getServer().getOnlineMode()) {
            xAuthLog.info("Auto-disabling, server is running in online-mode");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getConfig().getBoolean("main.check-for-updates")) {
            Updater updater = new Updater(getDescription().getVersion());
            if (updater.isUpdateAvailable())
                updater.printMessage();
        }

        File h2File = new File("lib", "h2-" + h2Version + ".jar");
        if (!h2File.exists() && getConfig().getBoolean("main.download-library") && !getConfig().getBoolean("mysql.enabled")) {
            xAuthLog.info("-------------------------------");
            xAuthLog.info("Downloading required H2 library..");
            downloadLib(h2File);
            xAuthLog.info("Download complete.");
            xAuthLog.info("");
            xAuthLog.info("Reload the server to enable xAuth.");
            xAuthLog.info("-------------------------------");

            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize permissions support
        // register Permission backends
        PermissionBackend.registerBackendAlias("pex", PermissionsExSupport.class);
        PermissionBackend.registerBackendAlias("gm", GroupManagerSupport.class);
        PermissionBackend.registerBackendAlias("bukkit", BukkitSupport.class);

        // load config if not already done
        if (this.config == null) {
            this.initConfiguration();
        }

        // resolve currently used PermissionPlugin
        this.resolvePermissionBackends();

        // init permissionManager; backend is set via config static call
        if (this.permissionManager == null) {
            this.permissionManager = new PermissionManager(this.config);
        }

        // Initialize database controller
        databaseController = new DatabaseController(this);

        // Test connection to database
        if (!databaseController.isConnectable()) {
            xAuthLog.severe("Failed to establish " + databaseController.getDBMS() + " database connection!");

            // disable (for now, may change in the future)
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        xAuthLog.info("Successfully established connection to " + databaseController.getDBMS() + " database");
        databaseController.runUpdater();

        // Initialize ALL THE CLASSES
        playerManager = new PlayerManager(this);
        playerDataHandler = new PlayerDataHandler(this);
        passwordHandler = new PasswordHandler(this);
        locationManager = new LocationManager(this);
        strikeManager = new StrikeManager(this);

        Player[] players = getServer().getOnlinePlayers();
        if (players.length > 0)
            playerManager.handleReload(players);

        // Initialize listeners
        this.getServer().getPluginManager().registerEvents(new xAuthEventListener(), this);
        this.getServer().getPluginManager().registerEvents(new xAuthEntityListener(), this);
        this.getServer().getPluginManager().registerEvents(new xAuthPlayerListener(), this);
        this.getServer().getPluginManager().registerEvents(new xAuthBlockListener(), this);

        // Register commands
        getCommand("register").setExecutor(new RegisterCommand());
        getCommand("login").setExecutor(new LoginCommand());
        getCommand("logout").setExecutor(new LogoutCommand());
        getCommand("quit").setExecutor(new QuitCommand());
        getCommand("changepw").setExecutor(new ChangePwdCommand());
        getCommand("xauth").setExecutor(new xAuthCommand(this));

        xAuthLog.info(String.format("v%s Enabled!", getDescription().getVersion()));
        lol();
    }

    private void initConfiguration() {
        if (this.config == null) {
            this.config = this.getConfig();
        }
        this.config.options().copyDefaults(true);
        saveConfig();
    }

    private void loadConfiguration() {
        // configuration
        this.initConfiguration();

        // messages; do not overwrite existing values
        messageHandler = new MessageHandler(this);
        messageHandler.getConfig().options().copyDefaults(true);
        messageHandler.saveConfig();
        messageHandler.reloadConfig();
    }

    /**
     * Resolve permissions plugin
     *
     * first enabled will be used.
     * Config node permissions.backend will be set to linked backend
     */
    private void resolvePermissionBackends() {
        for (String providerAlias : PermissionBackend.getRegisteredAliases()) {
            String pluginName = PermissionBackend.getBackendPluginName(providerAlias);
            xAuthLog.info("Attempting to use supported permissions plugin '" + pluginName + "'");

            Plugin permToLoad = Bukkit.getPluginManager().getPlugin(pluginName);
            if ((pluginName.equals(PermissionBackend.getDefaultBackend().getProviderName())) || ((permToLoad != null) && (permToLoad.isEnabled()))) {
                this.config.set("permissions.backend", providerAlias);
                xAuthLog.info("Config node permissions.backend changed to '" + providerAlias + "'");
                return;
            } else {
                xAuthLog.fine("Permission backend '" + providerAlias + "' was not found as plugin or not enabled!");
            }
        }
    }

    private void downloadLib(File h2File) {
        File dir = new File("lib");
        if (!dir.exists())
            dir.mkdir();

        Utils.downloadFile(h2File, this.libURLPath + h2File.getName());
    }

    public File getJar() {
        return getFile();
    }

    public DatabaseController getDatabaseController() {
        return databaseController;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public PlayerDataHandler getPlayerDataHandler() {
        return playerDataHandler;
    }

    public PasswordHandler getPasswordHandler() {
        return passwordHandler;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public StrikeManager getStrikeManager() {
        return strikeManager;
    }

    public Auth getAuthClass(xAuthPlayer p) {
        if (isAuthURL())
            return new AuthURL(this, p.getIPAddress());
        else
            return new AuthSQL(this, p);
    }

    public boolean isAuthURL() {
        return this.getConfig().getBoolean("authurl.enabled");
    }

    public void reload() {
        this.reloadConfig();
        loadConfiguration();

        playerManager.reload();
    }

    /**
     * Get the permissionManager.
     *
     * @return the PermissionManager
     */
    public static PermissionManager getPermissionManager() {
        try {
            if (!isPluginAvailable()) {
                // show warnings only to externals
                if (xAuthLog.getLevel().intValue() < Level.WARNING.intValue())
                    throw new xAuthNotAvailable("This plugin is not ready yet.");
            }
        } catch (xAuthNotAvailable e) {
            xAuthLog.warning(e.getMessage());
        }

        return ((xAuth) getPlugin()).permissionManager;
    }

    /**
     * Gets the plugin.
     *
     * @return the plugin instance
     */
    public static xAuth getPlugin() {
        Plugin plugin = org.bukkit.Bukkit.getServer().getPluginManager().getPlugin("xAuth");
        if (plugin == null || !(plugin instanceof xAuth)) {
            throw new RuntimeException("'xAuth' not found. 'xAuth' plugin disabled?");
        }

        return ((xAuth) plugin);
    }

    public static boolean isPluginAvailable() {
        Plugin plugin = getPlugin();
        if ((plugin instanceof xAuth)) {
            if (((xAuth) plugin).permissionManager != null) {
                return true;
            }
        }

        return false;
    }


    private void lol() {
        Calendar cal = new GregorianCalendar(TimeZone.getDefault());
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if (month == 3 && day == 1) {
            xAuthLog.warning("Your trial version of xAuth expires today!");
            xAuthLog.warning("Purchase the full version on Steam for $19.99.");
        } else if (month == 3 && day == 2)
            xAuthLog.info("April Fools!! xAuth will always be free!");
    }
}