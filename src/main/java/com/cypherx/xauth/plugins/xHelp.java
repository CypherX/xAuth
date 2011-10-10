package com.cypherx.xauth.plugins;

import me.taylorkelly.help.Help;
import org.bukkit.plugin.Plugin;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;

public class xHelp {
	public static void setup(xAuth plugin) {
		Plugin test = plugin.getServer().getPluginManager().getPlugin("Help");
		if (test != null) {
			Help helpPlugin = ((Help) test);
			String[] permissions = new String[]{"xauth.admin.register", "xauth.admin.changepw", "xauth.admin.logout", "xauth.admin.unregister", "xauth.admin.location", "xauth.admin.config", "xauth.admin.reload"};
			helpPlugin.registerCommand("register [password] (email)", "Create an in-game account linked to your player name", plugin, true);
			helpPlugin.registerCommand("login [password]", "Authenticate yourself as the account owner", plugin, true);
			helpPlugin.registerCommand("changepw [old password] [new password]", "Change your password", plugin);
			helpPlugin.registerCommand("logout", "Terminate your session", plugin);
			helpPlugin.registerCommand("xauth register [player] [password] (email)", "Create an account for [player]", plugin, permissions[0]);
			helpPlugin.registerCommand("xauth changepw [player] [new password]", "Change [player]'s password to [new password]", plugin, permissions[1]);
			helpPlugin.registerCommand("xauth logout [player]", "Terminate [player]'s session", plugin, permissions[2]);
			helpPlugin.registerCommand("xauth unregister [player]", "Remove [player]'s account", plugin, permissions[3]);
			helpPlugin.registerCommand("xauth location set", "Set your current location as this worlds teleport location", plugin, permissions[4]);
			helpPlugin.registerCommand("xauth location remove", "Remove this worlds teleport location", plugin, permissions[4]);
			helpPlugin.registerCommand("xauth config [setting] (new value)", "View info about or change a setting", plugin, permissions[5]);
			helpPlugin.registerCommand("xauth reload", "Reload the xAuth configuration and accounts", plugin, permissions[6]);
			xAuthLog.info("'Help' support enabled");
		}
	}
}