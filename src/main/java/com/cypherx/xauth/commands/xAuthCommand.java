package com.cypherx.xauth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.auth.Auth;
import com.cypherx.xauth.plugins.xPermissions;
import com.martiansoftware.jsap.CommandLineTokenizer;

public class xAuthCommand implements CommandExecutor {
	private final xAuth plugin;

	public xAuthCommand(xAuth plugin) {
	    this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
			if (args.length < 1)
				return false;

			args = CommandLineTokenizer.tokenize(args);
			String subCommand = args[0];
			if (subCommand.equals("register"))
				return registerCommand(sender, args);
			else if (subCommand.equals("changepw") || subCommand.equals("cpw") || subCommand.equals("changepassword") || subCommand.equals("changepass"))
				return changePwCommand(sender, args);

			return true;
		}

		return false;
	}

	private boolean registerCommand(CommandSender sender, String[] args) {
		if (!xPermissions.has(sender, "xauth.admin.register")) {
			plugin.getMsgHndlr().sendMessage("admin.permission", sender);
			return true;
		} else if (args.length < 3) {
			plugin.getMsgHndlr().sendMessage("admin.register.usage", sender);
			return true;
		}

		String targetName = args[1];
		String password = args[2];
		String email = args.length > 3 ? args[3] : null;
		xAuthPlayer xp = plugin.getPlyrMngr().getPlayer(targetName);

		Auth a = plugin.getAuthClass(xp);
		boolean success = a.adminRegister(targetName, password, email);

		String response = a.getResponse();
		if (response != null)
			plugin.getMsgHndlr().sendMessage(response, sender, targetName);

		if (success)
			xAuthLog.info(sender.getName() + " has registered an account for " + targetName);

		/*if (xp.isRegistered()) {
			plugin.getMsgHndlr().sendMessage("admin.register.registered", sender);
			return true;
		}

		Auth a = plugin.getAuthClass(xp, true);
		boolean success = a.register(targetName, password, email);

		if (success) {
			plugin.getMsgHndlr().sendMessage("admin.register.success", sender, targetName);
			//Player target = xp.getPlayer();
			//if (target != null)
				//plugin.getMsgHndlr().sendMessage(a.getResponse(), target);
		} else {
			plugin.getMsgHndlr().sendMessage(a.getResponse(), sender, targetName);
		}*/

		return true;
	}

	private boolean changePwCommand(CommandSender sender, String[] args) {
		if (!xPermissions.has(sender, "xauth.admin.changepw")) {
			plugin.getMsgHndlr().sendMessage("admin.permission", sender);
			return true;
		} else if (args.length < 3) {
			plugin.getMsgHndlr().sendMessage("admin.changepw.usage", sender);
			return true;
		}

		String targetName = args[1];
		String newPassword = args[2];
		xAuthPlayer xp = plugin.getPlyrMngr().getPlayer(targetName);

		Auth a = plugin.getAuthClass(xp);
		boolean success = a.adminChangePassword(targetName, newPassword);

		String response = a.getResponse();
		if (response != null)
			plugin.getMsgHndlr().sendMessage(response, sender, targetName);

		if (success)
			xAuthLog.info(sender.getName() + " changed " + targetName + "'s password");

		return true;
	}
}