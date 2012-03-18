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

		// Test connection to database
		if (!dbCtrl.isConnectable()) {
			xAuthLog.severe("Failed to establish " + dbCtrl.getDBMS() + " database connection!");

			// disable (for now, may change in the future)
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		xAuthLog.info("Successfully established connection to " + dbCtrl.getDBMS() + " database");
		dbCtrl.runUpdater();

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
		getCommand("xauth").setExecutor(new xAuthCommand(this));

		lol();

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
		msgCtrl.reloadConfig();
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
		if (this.getConfig().getBoolean("authurl.enabled"))
			return new AuthURL(this, p.getIPAddress());
		else
			return new AuthSQL(this, p);
	}

	private void lol() {
		Long now = System.currentTimeMillis() / 1000;
		if (now >= 1333256400 && now <= 1333342799) {
			xAuthLog.warning("Your trial version of xAuth expires today!");
			xAuthLog.warning("Purchase the full version on Steam for $19.99.");
		} else if (now >= 1333342800 && now <= 1333429199)
			xAuthLog.warning("April Fools!! xAuth will always be free!");
	}
}