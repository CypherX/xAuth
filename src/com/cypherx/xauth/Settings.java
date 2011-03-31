package com.cypherx.xauth;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.util.config.Configuration;

public class Settings
{
	private static String[] keys = 
	{
		"registration.enabled",
		"misc.allow-changepw",
		"misc.autosave",
		"session.timeout",
		"notify.limit",
		"registration.pw-min-length",
		"misc.allowed-cmds",
		"login.strikes.enabled",
		"login.strikes.amount",
		"security.filter.enabled",
		"security.filter.allowed",
		"security.filter.blankname"
	};

	private static final String[][] keyUpdates =
	{
		{"misc.allow-change-pw", "misc.allow-changepw"},
		{"misc.save-on-change", "misc.autosave"}
	};

	private static Configuration config;
	private static final ConcurrentHashMap<String, Object> defaults = new ConcurrentHashMap<String, Object>();
	private static final ConcurrentHashMap<String, Object> settings = new ConcurrentHashMap<String, Object>();

	public Settings(File file)
	{
		config = new Configuration(file);
		config.load();
		fillDefaults();
		
		if (file.exists() && keyUpdates.length > 0)		
			updateKeys();

		load();
		config.save();
	}

	public void fillDefaults()
	{
		defaults.put("registration.enabled", true);
		defaults.put("registration.pw-min-length", 3);
		defaults.put("session.timeout", 3600);
		defaults.put("notify.limit", 5);
		defaults.put("misc.allow-changepw", true);
		defaults.put("misc.allowed-cmds", new String[]{"/register", "/login"});
		defaults.put("misc.autosave", true);
		defaults.put("login.strikes.enabled", true);
		defaults.put("login.strikes.amount", 5);
		defaults.put("security.filter.enabled", true);
		defaults.put("security.filter.allowed", "abcdefghijklmnopqrstuvwxyz0123456789_- ()[]{}");
		defaults.put("security.filter.blankname", true);
	}

	public void updateKeys()
	{
		String fromKey, toKey;
		Object holder;
		for (String[] update : keyUpdates)
		{
			fromKey = update[0];
			if (config.getProperty(fromKey) != null)
			{
				toKey = update[1];
				holder = config.getProperty(fromKey);
				config.removeProperty(fromKey);
				config.setProperty(toKey, holder);
			}
		}
	}

	public void load()
	{
		for (String key : keys)
		{
			if (config.getProperty(key) == null)
				config.setProperty(key, defaults.get(key));
			settings.put(key, config.getProperty(key));
		}

		//clear defaults to free memory
		defaults.clear();
	}

	public void updateValue(String key, Object value)
	{
		settings.replace(key, value);
		config.setProperty(key, value);
		config.save();
	}

	public Boolean getBool(String key)
	{
		return (Boolean)settings.get(key);
	}

	public int getInt(String key)
	{
		return (Integer)settings.get(key);
	}

	public String getStr(String key)
	{
		return (String)settings.get(key);
	}

	@SuppressWarnings("unchecked")
	public List<String> getStrList(String key)
	{
		//List<String> cmds = (List<String>)settings.get(key);
		return (List<String>)settings.get(key);
	}
}