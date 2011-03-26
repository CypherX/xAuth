package com.cypherx.xauth;

import java.util.concurrent.ConcurrentHashMap;
import java.io.File;

import org.bukkit.util.config.Configuration;

public class Strings
{
	private static String[] keys = 
	{
		"register.login",
		"register.usage",
		"register.err.disabled",
		"register.err.registered",
		"register.err.password",
		"register.success1",
		"register.success2"
	};

	private static final String[][] keyUpdates =
	{
		
	};

	private static Configuration config;
	private static final ConcurrentHashMap<String, String> defaults = new ConcurrentHashMap<String, String>();
	private static final ConcurrentHashMap<String, String> strings = new ConcurrentHashMap<String, String>();

	public Strings(File file)
	{
		config = new Configuration(file);
		config.load();
		fillDefaults();
		
		if (file.exists() && keyUpdates.length > 0)		
			updateKeys();

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
	}

	public String getString(String key)
	{
		return strings.get(key);
	}

	public String getString(String key, Object replacement)
	{
		return strings.get(key).replace("%1", replacement.toString());
		//return replace(strings.get(key), new Object[]{replacement});
	}

	public String replace(String str, Object[] replacements)
	{
		int i;
		for (i = 0; i < replacements.length; i++)
			str.replace(("%" + i).toString(), replacements[i].toString());
		return str;
	}
}