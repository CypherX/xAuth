package com.cypherx.xauth;

import java.util.concurrent.ConcurrentHashMap;
import java.io.File;

import org.bukkit.util.config.Configuration;

public class Strings
{
	private static String[] keys = 
	{
		"register.login", "register.usage",	"register.err.disabled", "register.err.registered",	"register.err.password",
		"register.success1", "register.success2", "login.login", "login.usage",	"login.err.registered",	"login.err.logged",
		"login.err.password", "login.err.kick", "login.success", "changepw.usage1", "changepw.usage2", "changepw.err.login", 
		"changepw.err.disabled", "changepw.err.registered", "changepw.success.self", "changepw.success.other",
		"unregister.usage", "unregister.target", "unregister.success", "reload.success", "toggle.usage", "toggle.success",
		"logout.err.session", "logout.success.ended", "logout.success.other", "misc.illegal", "misc.reloaded",
		"misc.enabled",	"misc.disabled", "misc.filterkickmsg", "misc.blankkickmsg"
	};

	private static final String[][] keyUpdates = {};

	private static final String[] keyRemovals =
	{
		"toggle.err",
		"toggle.success",
	};

	private static Configuration config;
	private static final ConcurrentHashMap<String, String> defaults = new ConcurrentHashMap<String, String>();
	private static final ConcurrentHashMap<String, String> strings = new ConcurrentHashMap<String, String>();

	public Strings(File file)
	{
		config = new Configuration(file);
		config.load();
		fillDefaults();

		if (file.exists())
		{
			updateKeys();
			removeKeys();
		}

		load();
		config.save();
	}

	private void fillDefaults()
	{
		defaults.put("register.login", "&cYou are not registered. Please register using /register <password>.");
		defaults.put("register.usage", "&cCorrect Usage: /register <password>");
		defaults.put("register.err.disabled", "&cRegistrations are currently disabled.");
		defaults.put("register.err.registered", "&cYou are already registered.");
		defaults.put("register.err.password", "&cYour password must contain %1 or more characters.");
		defaults.put("register.success1", "&aYou have successfully registered!");
		defaults.put("register.success2", "&aYour password is: &f%1");

		defaults.put("login.login", "&cPlease log in using /login <password>.");
		defaults.put("login.usage", "&cCorrect Usage: /login <password>");
		defaults.put("login.err.registered", "&cYou are not registered.");
		defaults.put("login.err.logged", "&cYou are already logged in.");
		defaults.put("login.err.password", "&cIncorrect password!");
		defaults.put("login.err.kick", "Too many incorrect passwords!");
		defaults.put("login.success", "&aYou are now logged in.");

		defaults.put("changepw.usage1", "&cCorrect Usage: /changepw <newpassword>");
		defaults.put("changepw.usage2", "&cCorrect Usage: /changepw [player] <newpassword>");
		defaults.put("changepw.err.login", "&cYou must login before changing your password!");
		defaults.put("changepw.err.disabled", "&cPassword changes are currently disabled.");
		defaults.put("changepw.err.registered", "&cThis player is not registered!");
		defaults.put("changepw.success.self", "&aYour password has been changed to: &f%1");
		defaults.put("changepw.success.other", "&aPassword changed.");

		defaults.put("unregister.usage", "&cCorrect Usage: /unregister <player>");
		defaults.put("unregister.target", "&cYou have been unregistered.");
		defaults.put("unregister.success", "&a%1 has been unregistered.");

		defaults.put("reload.success", "&e[xAuth] Configuration and Accounts reloaded");

		defaults.put("toggle.usage", "&cCorrect Usage: /toggle <reg|changepw|autosave|filter|blankname|verifyip|strike|forcereg>");
		defaults.put("toggle.success", "&e[xAuth] Node %1.");

		defaults.put("logout.err.session", "&cThis player does not have an active session.");
		defaults.put("logout.success.ended", "&cYour session has been terminated. You must log in again.");
		defaults.put("logout.success.other", "&a%1's session has been terminated.");

		defaults.put("misc.illegal", "&7You must be logged in to do that!");
		defaults.put("misc.reloaded", "&cServer reloaded! You must log in again.");
		defaults.put("misc.enabled", "enabled");
		defaults.put("misc.disabled", "disabled");
		defaults.put("misc.filterkickmsg", "Your name contains one or more illegal characters.");
		defaults.put("misc.blankkickmsg", "Blank names are not allowed.");
	}

	private void removeKeys()
	{
		for (String key : keyRemovals)
		{
			if (config.getProperty(key) != null)
				config.removeProperty(key);
		}
	}

	private void updateKeys()
	{
		String fromKey, toKey, holder;
		for (String[] update : keyUpdates)
		{
			fromKey = update[0];
			if (config.getProperty(fromKey) != null)
			{
				toKey = update[1];
				holder = config.getString(fromKey);
				config.removeProperty(fromKey);
				if (!toKey.equals(""))
					config.setProperty(toKey, holder);
			}
		}
	}

	private void load()
	{
		for (String key : keys)
		{
			if (config.getProperty(key) == null)
				config.setProperty(key, defaults.get(key));
			strings.put(key, config.getString(key).replace("&", "\u00a7"));
		}

		//clear defaults to free memory
		defaults.clear();
	}

	public String getString(String key)
	{
		return strings.get(key);
	}

	public String getString(String key, Object replacement)
	{
		return strings.get(key).replace("%1", replacement.toString());
	}
}