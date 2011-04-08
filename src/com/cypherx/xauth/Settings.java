package com.cypherx.xauth;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.util.config.Configuration;

public class Settings
{
	private static String[] keys = 
	{
		"registration.enabled",
		"registration.forced",
		"misc.allow-changepw",
		"misc.autosave",
		"session.timeout",
		"session.verifyip",
		"notify.limit",
		"misc.allowed-cmds",
		"login.strikes.enabled",
		"login.strikes.amount",
		"filter.enabled",
		"filter.allowed",
		"filter.block-blankname",
		"password.min-length",
		"password.complexity.enabled",
		"password.complexity.lowercase",
		"password.complexity.uppercase",
		"password.complexity.numbers",
		"password.complexity.symbols"
	};

	private static final String[][] keyUpdates =
	{
		{"misc.allow-change-pw", "misc.allow-changepw"},
		{"misc.save-on-change", "misc.autosave"},
		{"registration.pw-min-length", "password.min-length"},
		{"security.filter.enabled", "filter.enabled"},
		{"security.filter.allowed", "filter.allowed"}
	};

	private static final String[] keyRemovals =
	{
		"security"
	};

	private final File file;
	private static Configuration config;
	private static final ConcurrentHashMap<String, Object> defaults = new ConcurrentHashMap<String, Object>();
	private static final ConcurrentHashMap<String, Object> settings = new ConcurrentHashMap<String, Object>();

	public Settings(File f)
	{
		file = f;
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

	public void fillDefaults()
	{
		defaults.put("registration.enabled", true);
		defaults.put("registration.forced", true);
		defaults.put("session.timeout", 3600);
		defaults.put("session.verifyip", true);
		defaults.put("notify.limit", 5);
		defaults.put("misc.allow-changepw", true);
		defaults.put("misc.allowed-cmds", Arrays.asList(new String[]{"/register", "/login"}));
		defaults.put("misc.autosave", true);
		defaults.put("login.strikes.enabled", true);
		defaults.put("login.strikes.amount", 5);
		defaults.put("filter.enabled", true);
		defaults.put("filter.allowed", "abcdefghijklmnopqrstuvwxyz0123456789_- ()[]{}");
		defaults.put("filter.block-blankname", true);
		defaults.put("password.min-length", 3);
		defaults.put("password.complexity.enabled", false);
		defaults.put("password.complexity.lowercase", false);
		defaults.put("password.complexity.uppercase", false);
		defaults.put("password.complexity.numbers", false);
		defaults.put("password.complexity.symbols", false);
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

	private void removeKeys()
	{
		for (String key : keyRemovals)
		{
			if (config.getProperty(key) != null)
				config.removeProperty(key);
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
		Object value = settings.get(key);

		if (value instanceof String)
			return Boolean.parseBoolean((String)value);
		
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
		//COMMAND_PREPROCESS error debugging
		if (!(settings.get(key) instanceof List))
		{
			System.out.println("[xAuth] COMMAND_PREPROCESS Error: Report this in the xAuth thread.");
			System.out.println("[xAuth] Value:" + settings.get(key));
			System.out.println("[xAuth] Attempting to autocorrect..");
			xAuth.settings = new Settings(file);
		}

		/*Object value = settings.get(key);

		if (value instanceof String[])
			System.out.println("string array");
		else if (value instanceof String)
			System.out.println("string");
		else if (value instanceof List)
			System.out.println("list");

		System.out.println(value);*/

		return (List<String>)settings.get(key);
	}
}