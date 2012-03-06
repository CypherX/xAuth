package com.cypherx.xauth;

import java.io.File;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.cypherx.xauth.auth.*;
import com.cypherx.xauth.commands.*;
import com.cypherx.xauth.database.DatabaseController;
import com.cypherx.xauth.listeners.*;
import com.cypherx.xauth.password.PasswordHandler;
import com.cypherx.xauth.plugins.xPermissions;
import com.cypherx.xauth.strike.StrikeManager;

public class xAuth extends JavaPlugin {
	private DatabaseController dbCtrl;
	private MessageHandler msgCtrl;
	private PlayerManager plyrMngr;
	private xAuthScheduler schdlr;
	private PlayerDataHandler plyrDtHndlr;
	private PasswordHandler pwdHndlr;
	private LocationManager locMngr;
	private StrikeManager strkMngr;
	//private boolean safeMode = false;

	public void onDisable() {
		for (Player p : getServer().getOnlinePlayers()) {
			xAuthPlayer xp = plyrMngr.getPlayer(p);
			if (xp.isProtected())
				plyrMngr.unprotect(xp);
		}

		if (dbCtrl != null)
			dbCtrl.close();
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

		// Initialize permissions support
		xPermissions.init();

		// Initialize database controller
		dbCtrl = new DatabaseController(this);

		// Test connection to MySQL server
		if (dbCtrl.isConnectable()) { // Able to connect to MySQL server, proceed normally
			xAuthLog.info("Successfully established connection to MySQL server");
			dbCtrl.runUpdater();			
		} else/* if (getConfig().getBoolean("main.use-safemode")) */{ // Failed to connect to MySQL server, disable plugin
			//xAuthLog.warning("MySQL server connection not found, safe-mode enabled");
			xAuthLog.severe("Failed to establish MySQL server connection!");
			//safeMode = true;

			// disable (for now, may change in the future)
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		// Initialize ALL THE CLASSES
		plyrMngr 	= new PlayerManager(this);
		schdlr 		= new xAuthScheduler(this);
		plyrDtHndlr = new PlayerDataHandler(this);
		pwdHndlr 	= new PasswordHandler(this);
		locMngr 	= new LocationManager(this);
		strkMngr	= new StrikeManager(this);

		Player[] players = getServer().getOnlinePlayers();
		if (players.length > 0)
			plyrMngr.handleReload(players);

		// Initialize listeners
		new xAuthPlayerListener(this);
		new xAuthBlockListener(this);
		new xAuthEntityListener(this);

		// Register commands
		getCommand("register").setExecutor(new RegisterCommand(this));
		getCommand("login").setExecutor(new LoginCommand(this));
		getCommand("logout").setExecutor(new LogoutCommand(this));
		getCommand("changepw").setExecutor(new ChangePwdCommand(this));

		xAuthLog.info(String.format("v%s Enabled!", getDescription().getVersion()));
	}

	private void loadConfiguration() {
		// configuration
		getConfig().options().copyDefaults(true);
		saveConfig();

		// messages
		msgCtrl = new MessageHandler(this);
		msgCtrl.getConfig().options().copyDefaults(true);
		msgCtrl.saveConfig();
	}

	public File getJar() { return getFile(); }
	public DatabaseController getDbCtrl() { return dbCtrl; }
	public MessageHandler getMsgHndlr() { return msgCtrl; }
	public PlayerManager getPlyrMngr() { return plyrMngr; }
	public xAuthScheduler getSchdlr() { return schdlr; }
	public PlayerDataHandler getPlyrDtHndlr() { return plyrDtHndlr; }
	public PasswordHandler getPwdHndlr() { return pwdHndlr; }
	public LocationManager getLocMngr() { return locMngr; }
	public StrikeManager getStrkMngr() { return strkMngr; }

	public Auth getAuthClass(xAuthPlayer p){
		if(this.getConfig().getBoolean("authurl.enabled"))
			/*return new AuthURL(getConfig().getString("authurl.url"), p.getIPAddress(), getConfig().getBoolean("authurl.registration"),
					getConfig().getBoolean("authurl.status"), getConfig().getBoolean("authurl.groups"));*/
			return new AuthURL(this, p.getIPAddress());
		else
			return new AuthSQL(this, p);
	}

	//public boolean isSafeMode() { return safeMode; }
}