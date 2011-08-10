package com.cypherx.xauth;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.util.config.Configuration;

import com.cypherx.xauth.database.Database;

public class xAuthSettings {
	private static File file;
	private static Configuration config;
	public static boolean changed = false;

	// main
	public static String datasource = "h2";
	public static boolean autoDisable = true;
	public static boolean downloadLib = true;
	public static boolean reverseESS = true;

	// authURL
	public static boolean authURLEnabled = false;
	public static String authURL = "http://127.0.0.1/auth.php?field=minecra";

	// mysql
	public static String mysqlHost = "localhost";
	public static int mysqlPort = 3306;
	public static String mysqlUser = "root";
	public static String mysqlPass = "";
	public static String mysqlDb = "";
	public static String tblAccount = "accounts";
	public static String tblSession = "sessions";
	public static String tblStrike = "strike_bans";
	public static String tblLocation = "tele_locations";
	public static String tblInventory = "inventory";

	// registration
	public static boolean regEnabled = true;
	public static boolean regForced = true;
	public static boolean requireEmail = false;
	public static boolean validateEmail = true;
	public static int accountLimit = 0;
	public static boolean activation = false;

	// login
	public static int maxStrikes = 5;
	public static String strikeAction = "kick";
	public static int banLength = 3600;

	// password
	public static int pwMinLength = 6;
	public static boolean pwAllowChange = true;
	public static boolean pwCompLower = false;
	public static boolean pwCompUpper = false;
	public static boolean pwCompNumber = false;
	public static boolean pwCompSymbol = false;

	// guest
	public static int guestTimeout = 300;
	public static int notifyCooldown = 5;
	public static List<String> allowedCmds = Arrays.asList(new String[]{"register", "login", "l"});
	public static boolean protectLoc = true;

	// session
	public static int sessionLength = 3600;
	public static boolean verifyIp = true;
	public static int godmodeLength = 5;

	// filter
	public static int filterMinLength = 2;
	public static String filterAllowed = "*";
	public static boolean filterBlank = true;

	// restrictions
	/*public static boolean rstrChat = true;
	public static boolean rstrCommands = true;
	public static boolean rstrInteract = true;
	public static boolean rstrMovement = true;
	public static boolean rstrPickup = true;
	public static boolean rstrBreak = true;
	public static boolean rstrPlace = true;
	public static boolean rstrDmgTaken = true;
	public static boolean rstrDmgGiven = true;
	public static boolean rstrMobTarget = true;*/

	/*
	 * REMEMBER TO CHANGE VERSION AFTER MODIFYING DEFAULT SETTINGS
	 */
	public static int version = 5;

	public static void setup() {
		file = new File(xAuth.dataFolder, "config.yml");

		if (!file.exists()) {
			xAuthLog.info("Creating file: config.yml");
			Util.writeConfig(file, xAuthSettings.class);
		} else {
			config = new Configuration(file);
			config.load();
			loadSettings();
			update();
		}
	}

	public static void loadSettings() {
		datasource = getString("main.datasource", datasource);
		autoDisable = getBool("main.auto-disable", autoDisable);
		downloadLib = getBool("main.download-libraries", downloadLib);
		reverseESS = getBool("main.reverse-enforce-single-session", reverseESS);

		authURLEnabled = getBool("authurl.enabled", authURLEnabled);
		authURL = getString("authurl.url", authURL);

		mysqlHost = getString("mysql.host", mysqlHost);
		mysqlPort = getInt("mysql.port", mysqlPort);
		mysqlUser = getString("mysql.username", mysqlUser);
		mysqlPass = getString("mysql.password", mysqlPass);
		mysqlDb = getString("mysql.database", mysqlDb);
		tblAccount = getString("mysql.tables.account", tblAccount);
		tblSession = getString("mysql.tables.session", tblSession);
		tblStrike = getString("mysql.tables.strike", tblStrike);
		tblLocation = getString("mysql.tables.location", tblLocation);
		tblInventory = getString("mysql.tables.inventory", tblInventory);

		regEnabled = getBool("registration.enabled", regEnabled);
		regForced = getBool("registration.forced", regForced);
		requireEmail = getBool("registration.require-email", requireEmail);
		validateEmail = getBool("registration.validate-email", validateEmail);
		accountLimit = getInt("registration.account-limit", accountLimit);
		activation = getBool("registration.activation", activation);

		maxStrikes = getInt("login.strikes.amount", maxStrikes);
		strikeAction = getString("login.strikes.action", strikeAction);
		banLength = getInt("login.strikes.length", banLength);

		pwMinLength = getInt("password.min-length", pwMinLength);
		pwAllowChange = getBool("password.allow-change", pwAllowChange);
		pwCompLower = getBool("password.complexity.lowercase", pwCompLower);
		pwCompUpper = getBool("password.complexity.uppercase", pwCompUpper);
		pwCompNumber = getBool("password.complexity.number", pwCompNumber);
		pwCompSymbol = getBool("password.complexity.symbol", pwCompSymbol);

		guestTimeout = getInt("guest.timeout", guestTimeout);
		notifyCooldown = getInt("guest.notify-cooldown", notifyCooldown);
		allowedCmds = getStrList("guest.allowed-commands", allowedCmds);
		protectLoc = getBool("guest.protect-location", protectLoc);

		sessionLength = getInt("session.length", sessionLength);
		verifyIp = getBool("session.verifyip", verifyIp);
		godmodeLength = getInt("session.godmode-length", godmodeLength);

		filterMinLength = getInt("filter.min-length", filterMinLength);
		filterAllowed = getString("filter.allowed", filterAllowed);
		filterBlank = getBool("filter.blankname", filterBlank);

		/*rstrChat = getBool("restrict.chat", rstrChat);
		rstrCommands = getBool("restrict.commands", rstrCommands);
		rstrInteract = getBool("restrict.interact", rstrInteract);
		rstrMovement = getBool("restrict.movement", rstrMovement);
		rstrPickup = getBool("restrict.item-pickup", rstrPickup);
		rstrBreak = getBool("restrict.block-break", rstrBreak);
		rstrPlace = getBool("restrict.block-place", rstrPlace);
		rstrDmgTaken = getBool("restrict.damage-taken", rstrDmgTaken);
		rstrDmgGiven = getBool("restrict.damage-given", rstrDmgGiven);
		rstrMobTarget = getBool("restrict.mob-target", rstrMobTarget);*/

		// authURL doesn't allow for registration, or password management, so automatically disable it
		if (authURLEnabled) {
			regEnabled = false;
			regForced = true;
			pwAllowChange = false;
			activation = false;
		}

		if (datasource.equals("mysql"))
			Database.setDBMS(Database.DBMS.MYSQL);
	}

	private static String getString(String key, String def) {
		return config.getString(key, def);
	}

	private static int getInt(String key, int def) {
		return config.getInt(key, def);
	}

	private static boolean getBool(String key, boolean def) {
		return config.getBoolean(key, def);
	}

	private static List<String> getStrList(String key, List<String> def) {
		return config.getStringList(key, def);
	}

	private static void update() {
		if (version > getInt("version", version)) {
			xAuthLog.info("Updating file: config.yml");
			Util.writeConfig(file, xAuthSettings.class);
		}
	}

	public static void saveChanges() {
		if (changed) {
			xAuthLog.info("Saving configuration changes..");
			Util.writeConfig(file, xAuthSettings.class);
		}
	}
}