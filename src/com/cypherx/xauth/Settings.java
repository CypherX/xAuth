package com.cypherx.xauth;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.util.config.Configuration;

public class Settings
{
	interface Keys
	{
		public static final String REG_ENABLED = "registration.enabled";
		public static final String ALLOW_CHANGE_PW = "misc.allow-change-pw";//old allow-changepw
		public static final String ALLOW_CHANGEPW = "misc.allow-changepw";//new allow-changepw
		public static final String SAVE_ON_CHANGE = "misc.save-on-change";//old autosave
		public static final String AUTOSAVE = "misc.autosave";//new autosave
		public static final String SESSION_TIMEOUT = "session.timeout";
		public static final String NOTIFY_LIMIT = "notify.limit";
		public static final String PW_MIN_LENGTH = "registration.pw-min-length";
		public static final String ALLOWED_CMDS = "misc.allowed-cmds";
	}

	private static Configuration config;
	private static final ConcurrentHashMap<String, Object> settings = new ConcurrentHashMap<String, Object>();

	public Settings (File file)
	{
		config = new Configuration(file);
		load();
	}

	public void load()
	{
		config.load();

		//Booleans
		String key = Keys.REG_ENABLED;
		if (config.getProperty(key) == null)
			config.setProperty(key, true);
		settings.put(key, config.getBoolean(key, true));

		key = Keys.ALLOW_CHANGE_PW;
		if (config.getProperty(key) != null)
		{
			settings.put(key, config.getBoolean(key, true));
			config.removeProperty(key);
		}
		key = Keys.ALLOW_CHANGEPW;
		if (config.getProperty(key) == null)
			config.setProperty(key, getBool(Keys.ALLOW_CHANGE_PW) == null ? true : getBool(Keys.ALLOW_CHANGE_PW));
		settings.put(key, config.getBoolean(key, true));

		key = Keys.SAVE_ON_CHANGE;
		if (config.getProperty(key) != null)
		{
			settings.put(key, config.getBoolean(key, true));
			config.removeProperty(key);
		}
		key = Keys.AUTOSAVE;
		if (config.getProperty(key) == null)
			config.setProperty(key, getBool(Keys.SAVE_ON_CHANGE) == null ? true : getBool(Keys.SAVE_ON_CHANGE));
		settings.put(key, config.getBoolean(key, true));
		//end Booleans

		//Integers
		key = Keys.SESSION_TIMEOUT;
		if (config.getProperty(key) == null)
			config.setProperty(key, 3600);
		settings.put(key, config.getInt(key, 3600));

		key = Keys.NOTIFY_LIMIT;
		if (config.getProperty(key) == null)
			config.setProperty(key, 5);
		settings.put(key, config.getInt(key, 5));

		key = Keys.PW_MIN_LENGTH;
		if (config.getProperty(key) == null)
			config.setProperty(key, 3);
		settings.put(key, config.getInt(key, 3));
		//end Integers

		//String Arrays
		key = Keys.ALLOWED_CMDS;
		if (config.getProperty(key) == null)
			config.setProperty(key, Arrays.asList(new String[] {"/register", "/login"}));
		settings.put(key, config.getStringList(key, Arrays.asList(new String[] {"/register", "/login"})));
		//end String Arrays

		config.save();
	}

	public void update(String key, Object value)
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

	@SuppressWarnings("unchecked")
	public List<String> getStrArr(String key)
	{
		return (List<String>)settings.get(key);
	}
}