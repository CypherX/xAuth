package com.cypherx.xauth;

import java.io.File;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public class xAuthMessages {
	private static File file;
	private static Configuration config;

	// join
	public static String joinRegister = "{RED}You are not registered.{NEWLINE}{RED}Please register using /register <password>.";
	public static String joinLogin = "{RED}Please log in using /login <password>.";
	public static String joinErrBanned = "You have are banned for exceeding the incorrect password threshold.";
	public static String joinErrOnline = "You are already online!";
	public static String joinErrName = "Your name contains one or more illegal characters.";

	// register
	public static String regUsage = "{RED}Correct Usage: /register <password> [email]";
	public static String regSuccess = "{BRIGHTGREEN}You have successfully registered!";
	public static String regErrDisabled = "{RED}Registrations are currently disabled!";
	public static String regErrRegistered = "{RED}You are already registered!";
	public static String regErrMultiple = "{RED}You may not register any more accounts!";
	public static String regErrPassword = "{RED}Your password must be at least {PWMINLENGTH} characters long!";
	public static String regErrEmail = "{RED}Please use a valid email address when registering!";

	// login
	public static String loginUsage = "{RED}Correct Usage: /login <password>";
	public static String loginSuccess = "{BRIGHTGREEN}You are now logged in!";
	public static String loginErrRegistered = "{RED}You are not registered!";
	public static String loginErrLogged = "{RED}You are already logged in!";
	public static String loginErrPassword = "{RED}Incorrect password!";
	public static String loginErrActivate = "{RED}Your account is not activated!";

	// logout
	public static String logoutSuccess = "{BRIGHTGREEN}You have been logged out!";
	public static String logoutErrLogged = "{RED}You are not logged in!";

	// change password
	public static String cpwUsage = "{RED}Correct Usage: /changepw <old password> <new password>";
	public static String cpwSuccess = "{BRIGHTGREEN}Password changed!";
	public static String cpwErrDisabled = "{RED}Password changes are currently disabled!";
	public static String cpwErrLogged = "{RED}You are not logged in!";
	public static String cpwErrIncorrect = "{RED}Incorrect old password!";
	public static String cpwErrInvalid = "{RED}Your new password must be at least {PWMINLENGTH} characters long!";

	// admin
	public static String admnUnknown = "Unknown subcommand, try /xauth for more information";
	public static String admnPermission = "You do not have permission to use this command!";
	public static String admnRegUsage = "{RED}Correct Usage: /xauth register <player> <password> [email]";
	public static String admnRegRegistered = "{RED}This player is already registered!";
	public static String admnRegSuccess = "{BRIGHTGREEN}Account successfully created for: {WHITE}{TARGET}";
	public static String admnCpwUsage = "{RED}Correct Usage: /xauth changepw <player> <new password>";
	public static String admnCpwRegistered = "{RED}This player is not registered!";
	public static String admnCpwSuccess = "{TARGET}'s {BRIGHTGREEN}password has been changed!";
	public static String admnLogoutUsage = "{RED}Correct Usage: /xauth logout <player>";
	public static String admnLogoutLogged = "{TARGET} {RED}is not logged in!";
	public static String admnLogoutSuccess = "{TARGET} {BRIGHTGREEN}has been logged out!";
	public static String admnUnregUsage = "{RED}Correct Usage: /xauth unregister <player>";
	public static String admnUnregRegistered = "{RED}This player is not registered!";
	public static String admnUnregSuccessTgt = "{RED}You have been unregistered and logged out!";
	public static String admnUnregSuccessPlyr = "{TARGET} {BRIGHTGREEN}has been unregistered!";
	public static String admnLocUsage = "{RED}Correct Usage: /xauth location set|remove [global]";
	public static String admnLocSetErrGlobal = "{YELLOW}{PLUGIN} {RED}Global teleport location is set to this world.{NEWLINE}{YELLOW}{PLUGIN} {RED}Please remove it first.";
	public static String admnLocSetSuccess = "{YELLOW}{PLUGIN} {BRIGHTGREEN}Teleport location for this world set to your location!";
	public static String admnLocSetGlobalSuccess = "{YELLOW}{PLUGIN} {BRIGHTGREEN}Global teleport location set to your location!";
	public static String admnLocRmvNo = "{YELLOW}{PLUGIN} {BRIGHTGREEN}This world does not have a teleport location!";
	public static String admnLocRmvErrGlobal = "{YELLOW}{PLUGIN} {RED}Global teleport location is set to this world.{NEWLINE}{YELLOW}{PLUGIN} {RED}Please use /xauth location remove global";
	public static String admnLocRmvGlobalNo = "{YELLOW}{PLUGIN} {BRIGHTGREEN}A global teleport location is not set!";
	public static String admnLocRmvSuccess = "{YELLOW}{PLUGIN} {BRIGHTGREEN}Teleport location for this world has been removed!";
	public static String admnLocRmvGlobalSuccess = "{YELLOW}{PLUGIN} {BRIGHTGREEN}Global teleport location has been removed!";
	public static String admnConfUsage = "{RED}Correct Usage: /xauth config <setting> [new value]";
	public static String admnConfNo = "{YELLOW}{PLUGIN} {RED}No such setting!";
	public static String admnConfDesc = "Setting: {SETTING}{NEWLINE}Type: {TYPE}{NEWLINE}Value: {VALUE}";
	public static String admnConfInvalid = "{RED}Invalid value type!";
	public static String admnConfSuccess = "{YELLOW}{PLUGIN} {BRIGHTGREEN}Setting changed!";
	public static String admnReloadSuccess = "{YELLOW}{PLUGIN} {BRIGHTGREEN}Reload complete!";

	// misc
	public static String miscIllegal = "{GRAY}You must be logged in to do that!";
	public static String miscKickTimeout = "You have taken too long to log in!";
	public static String miscKickStrike = "You have entered too many invalid passwords!";
	public static String miscReloaded = "{RED}Server reloaded, please log in.";

	/*
	 * REMEMBER TO CHANGE VERSION AFTER MODIFYING DEFAULT STRINGS
	 */
	public static int version = 2;	

	public static void setup() {
		file = new File(xAuth.dataFolder, "messages.yml");

		if (!file.exists()) {
			xAuthLog.info("Creating file: messages.yml");
			Util.writeConfig(file, xAuthMessages.class);
		} else {
			config = new Configuration(file);
			config.load();
			loadMessages();
			update();
		}
	}

	public static void loadMessages() {
		joinRegister = getString("join.register", joinRegister);
		joinLogin = getString("join.login", joinLogin);
		joinErrBanned = getString("join.error.banned", joinErrBanned);
		joinErrOnline = getString("join.error.online", joinErrOnline);
		joinErrName = getString("join.error.name", joinErrOnline);

		regUsage = getString("register.usage", regUsage);
		regSuccess = getString("register.success", regSuccess);
		regErrDisabled = getString("register.error.disabled", regErrDisabled);
		regErrRegistered = getString("register.error.registered", regErrRegistered);
		regErrMultiple = getString("register.error.multiple", regErrMultiple);
		regErrPassword = getString("register.error.password", regErrPassword);
		regErrEmail = getString("register.error.email", regErrEmail);

		loginUsage = getString("login.usage", loginUsage);
		loginSuccess = getString("login.success", loginSuccess);
		loginErrRegistered = getString("login.error.registered", loginErrRegistered);
		loginErrLogged = getString("login.error.logged", loginErrLogged);
		loginErrPassword = getString("login.error.password", loginErrPassword);
		loginErrActivate = getString("login.error.activate", loginErrActivate);

		logoutSuccess = getString("logout.success", logoutSuccess);
		logoutErrLogged = getString("logout.error.logged", logoutErrLogged);

		cpwUsage = getString("changepw.usage", cpwUsage);
		cpwSuccess = getString("changepw.success", cpwSuccess);
		cpwErrDisabled = getString("changepw.error.disabled", cpwErrDisabled);
		cpwErrLogged = getString("changepw.error.logged", cpwErrLogged);
		cpwErrIncorrect = getString("changepw.error.incorrect", cpwErrIncorrect);
		cpwErrInvalid = getString("changepw.error.invalid", cpwErrInvalid);

		admnUnknown = getString("admin.unknown", admnUnknown);
		admnPermission = getString("admin.permission", admnPermission);
		admnRegUsage = getString("admin.register.usage", admnRegUsage);
		admnRegRegistered = getString("admin.register.registered", admnRegRegistered);
		admnRegSuccess = getString("admin.register.success", admnRegSuccess);
		admnCpwUsage = getString("admin.changepw.usage", admnCpwUsage);
		admnCpwRegistered = getString("admin.changepw.registered", admnCpwRegistered);
		admnCpwSuccess = getString("admin.changepw.success", admnCpwSuccess);
		admnLogoutUsage = getString("admin.logout.usage", admnLogoutUsage);
		admnLogoutLogged = getString("admin.logout.logged", admnLogoutLogged);
		admnLogoutSuccess = getString("admin.logout.success", admnLogoutSuccess);
		admnUnregUsage = getString("admin.unregister.usage", admnUnregUsage);
		admnUnregRegistered = getString("admin.unregister.registered", admnUnregRegistered);
		admnUnregSuccessTgt = getString("admin.unregister.success.target", admnUnregSuccessTgt);
		admnUnregSuccessPlyr = getString("admin.unregister.success.player", admnUnregSuccessPlyr);
		admnLocUsage = getString("admin.location.usage", admnLocUsage);
		admnLocSetErrGlobal = getString("admin.location.set.isglobal", admnLocSetErrGlobal);
		admnLocSetSuccess = getString("admin.location.set.success", admnLocSetSuccess);
		admnLocSetGlobalSuccess = getString("admin.location.set.global.success", admnLocSetGlobalSuccess);
		admnLocRmvNo = getString("admin.location.remove.no", admnLocRmvNo);
		admnLocRmvErrGlobal = getString("admin.location.remove.isglobal", admnLocRmvErrGlobal);
		admnLocRmvSuccess = getString("admin.location.remove.success", admnLocRmvSuccess);
		admnLocRmvGlobalNo = getString("admin.location.remove.global.no", admnLocRmvGlobalNo);
		admnLocRmvGlobalSuccess = getString("admin.location.remove.global.success", admnLocRmvGlobalSuccess);
		admnConfUsage = getString("admin.config.usage", admnConfUsage);
		admnConfNo = getString("admin.config.no", admnConfNo);
		admnConfDesc = getString("admin.config.desc", admnConfDesc);
		admnConfInvalid = getString("admin.config.invalid", admnConfInvalid);
		admnConfSuccess = getString("admin.config.success", admnConfSuccess);
		admnReloadSuccess = getString("admin.reload.success", admnReloadSuccess);

		miscIllegal = getString("misc.illegal", miscIllegal);
		miscKickTimeout = getString("misc.kick.timeout", miscKickTimeout);
		miscKickStrike = getString("misc.kick.strike", miscKickStrike);
		miscReloaded = getString("misc.reloaded", miscReloaded);
	}

	private static String getString(String key, String def) {
		return config.getString(key, def);
	}

	private static int getInt(String key, int def) {
		return config.getInt(key, def);
	}

	private static void update() {
		if (version > getInt("version", version)) {
			xAuthLog.info("Updating file: messages.yml");
			Util.writeConfig(file, xAuthMessages.class);
		}
	}

	public static void send(String fieldName, Player player) {
		String message = get(fieldName, player, null);

		for (String line : message.split("\n"))
			player.sendMessage(line);
	}

	public static void send(String fieldName, Player player, String target) {
		String message = get(fieldName, player, target);

		for (String line : message.split("\n"))
			player.sendMessage(line);
	}

	public static String get(String fieldName, Player player, String target) {
		String message = null;
		try {
			message = xAuthMessages.class.getField(fieldName).get(null).toString();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return replace(message, player, target);
	}

	private static String replace(String message, Player player, String target) {
		// colors
		message = message.replace("{BLACK}", "&0");
		message = message.replace("{DARKBLUE}", "&1");
		message = message.replace("{DARKGREEN}", "&2");
		message = message.replace("{DARKTEAL}", "&3");
		message = message.replace("{DARKRED}", "&4");
		message = message.replace("{PURPLE}", "&5");
		message = message.replace("{GOLD}", "&6");
		message = message.replace("{GRAY}", "&7");
		message = message.replace("{DARKGRAY}", "&8");
		message = message.replace("{BLUE}", "&9");
		message = message.replace("{BRIGHTGREEN}", "&a");
		message = message.replace("{TEAL}", "&b");
		message = message.replace("{RED}", "&c");
		message = message.replace("{PINK}", "&d");
		message = message.replace("{YELLOW}", "&e");
		message = message.replace("{WHITE}", "&f");
		message = message.replace("&", "\u00a7");


		// plugin
		message = message.replace("{PLUGIN}", "[" + xAuth.desc.getName() + "]");
		message = message.replace("{VERSION}", xAuth.desc.getVersion());

		// player
		if (player != null) {
			message = message.replace("{PLAYER}", player.getName());
			message = message.replace("{IP}", Util.getHostFromPlayer(player));
		}

		// target
		if (target != null)
			message = message.replace("{TARGET}", target);

		// settings
		message = message.replace("{PWMINLENGTH}", Integer.toString(xAuthSettings.pwMinLength));
		message = message.replace("{NAMEMINLENGTH}", Integer.toString(xAuthSettings.filterMinLength));
		message = message.replace("{MAXSTRIKES}", Integer.toString(xAuthSettings.maxStrikes));
		message = message.replace("{ACCOUNTLIMIT}", Integer.toString(xAuthSettings.accountLimit));

		// misc
		message = message.replace("{NEWLINE}", "\n");

		return message;
	}

	// separate method for this because I'm lazy
	public static void sendConfigDesc(Player player, String setting, String type, Object value) {
		String message = null;
		try {
			message = xAuthMessages.class.getField("admnConfDesc").get(null).toString();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		if (message == null)
			return;

		message = message.replace("{SETTING}", setting);
		message = message.replace("{TYPE}", type);
		message = message.replace("{VALUE}", value.toString());
		message = message.replace("{NEWLINE}", "\n");

		for (String line : message.split("\n"))
			player.sendMessage(line);
	}
}