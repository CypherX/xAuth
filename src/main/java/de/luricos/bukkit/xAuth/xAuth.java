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
package de.luricos.bukkit.xAuth;

import de.luricos.bukkit.xAuth.auth.AuthMethod;
import de.luricos.bukkit.xAuth.auth.AuthMethodSQL;
import de.luricos.bukkit.xAuth.auth.AuthMethodURL;
import de.luricos.bukkit.xAuth.commands.*;
import de.luricos.bukkit.xAuth.database.DatabaseController;
import de.luricos.bukkit.xAuth.exceptions.xAuthNotAvailable;
import de.luricos.bukkit.xAuth.listeners.*;
import de.luricos.bukkit.xAuth.password.PasswordHandler;
import de.luricos.bukkit.xAuth.permissions.PermissionBackend;
import de.luricos.bukkit.xAuth.permissions.PermissionManager;
import de.luricos.bukkit.xAuth.permissions.backends.BukkitSupport;
import de.luricos.bukkit.xAuth.permissions.backends.GroupManagerSupport;
import de.luricos.bukkit.xAuth.permissions.backends.PermissionsExSupport;
import de.luricos.bukkit.xAuth.strike.StrikeManager;
import de.luricos.bukkit.xAuth.tasks.xAuthTasks;
import de.luricos.bukkit.xAuth.updater.Updater;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class xAuth extends JavaPlugin {
    private DatabaseController databaseController;
    private MessageHandler messageHandler;
    private PlayerManager playerManager;
    private PlayerDataHandler playerDataHandler;
    private PasswordHandler passwordHandler;
    private LocationManager locationManager;
    private StrikeManager strikeManager;

    private PermissionManager permissionManager;
    private Configuration config;
    private Updater updater;

    private List<String> commands = new ArrayList<String>();

    private String h2Version = "1.3.164";
    //private String libURLPath = "http://bukkit.luricos.de/plugins/xAuth/lib/";

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
        if (isAutoDisable() && getServer().getOnlineMode()) {
            xAuthLog.info("Auto-disabling, server is running in online-mode");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // init the updater
        this.initUpdater();

        // check for update if updater is enabled
        this.checkUpdate();

        File h2File = new File("lib", "h2-" + h2Version + ".jar");
        if ((!h2File.exists()) && (!getConfig().getBoolean("mysql.enabled"))) {
            xAuthLog.severe("-------------------------------");
            xAuthLog.severe("H2 library missing!");
            xAuthLog.severe("");
            xAuthLog.severe("Please follow the instructions at my dev.bukkit project page");
            xAuthLog.severe("http://dev.bukkit.org/server-mods/xAuth/pages/required-dependencies/");
            xAuthLog.severe("-------------------------------");

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
        playerManager = new PlayerManager(this, new xAuthTasks());
        playerDataHandler = new PlayerDataHandler(this);
        passwordHandler = new PasswordHandler(this);
        locationManager = new LocationManager(this);
        strikeManager = new StrikeManager(this);

        Player[] players = getServer().getOnlinePlayers();
        if (players.length > 0)
            playerManager.handleReload(players);

        // register event-listeners
        this.registerEvents();

        // Register commands
        getCommand("register").setExecutor(new RegisterCommand());
        getCommand("login").setExecutor(new LoginCommand());
        getCommand("logout").setExecutor(new LogoutCommand());
        getCommand("quit").setExecutor(new QuitCommand());
        getCommand("changepw").setExecutor(new ChangePwdCommand());
        getCommand("xauth").setExecutor(new xAuthAdminCommands());

        // add commands to keyring
        for (String command : this.getDescription().getCommands().keySet()) {
            this.commands.add(command);
            this.commands.addAll(this.getCommand(command).getAliases());
        }

        // enable command logging when set via config
        if (getConfig().getBoolean("filter.commands"))
            xAuthLog.enableFeature(xAuthLog.xAuthLogFeatures.FILTER_COMMANDS);

        xAuthLog.info(String.format("v%s Enabled!", getDescription().getVersion()));
        //lol();
    }

    public List<String> getCommands() {
        return this.commands;
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new xAuthEventListener(), this);
        this.getServer().getPluginManager().registerEvents(new xAuthEntityListener(), this);
        this.getServer().getPluginManager().registerEvents(new xAuthPlayerListener(), this);
        this.getServer().getPluginManager().registerEvents(new xAuthBlockListener(), this);
        this.getServer().getPluginManager().registerEvents(new xAuthHangingListener(), this);
        this.getServer().getPluginManager().registerEvents(new xAuthInventoryListener(), this);
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

    public AuthMethod getAuthClass(xAuthPlayer p) {
        if (isAuthURL())
            return new AuthMethodURL(this, p.getIPAddress());
        else
            return new AuthMethodSQL(this, p);
    }

    public boolean isAuthURL() {
        return this.getConfig().getBoolean("authurl.enabled");
    }

    public void reload() {
        xAuthLog.info("-- Internal reload in progress");

        // unregister event-listeners from this plugin
        HandlerList.unregisterAll(this);

        // reload config & messages
        this.reloadConfig();
        this.config = this.getConfig();
        messageHandler.reloadConfig();

        this.initUpdater();
        this.checkUpdate();

        playerManager.reload();

        permissionManager.reset();

        xAuthLog.disableFeatures();
        if (this.getConfig().getBoolean("filter.commands")) {
            xAuthLog.info("Enabling Commands-Filter Feature");
            xAuthLog.enableFeature(xAuthLog.xAuthLogFeatures.FILTER_COMMANDS);
        }

        // re-register events
        this.registerEvents();

        xAuthLog.info("-- Reload finished");
    }

    public void initUpdater() {
        this.updater = new Updater(this, 35934, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
    }

    public void checkUpdate() {
        this.updater.run();
    }

    public boolean isAutoDisable(){
        return this.getConfig().getBoolean("main.auto-disable");
    }

    public boolean isPremiumMode() {
        return this.getConfig().getBoolean("main.check-premium");
    }

    /**
     * Get the Updater.
     *
     * @return the Updater
     */
    public static Updater getUpdater() {
        try {
            if (!isPluginAvailable()) {
                // show warnings only to externals
                if (xAuthLog.getLevel().intValue() < Level.WARNING.intValue())
                    throw new xAuthNotAvailable("This plugin is not ready yet.");
            }
        } catch (xAuthNotAvailable e) {
            xAuthLog.warning(e.getMessage());
        }

        return ((xAuth) getPlugin()).updater;
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
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("xAuth");
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

    /** Disabled
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
    */
}