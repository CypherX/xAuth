package com.cypherx.xauth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.auth.Auth;
import com.martiansoftware.jsap.CommandLineTokenizer;

public class ChangePwdCommand implements CommandExecutor {
	private final xAuth plugin;

	public ChangePwdCommand(final xAuth plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		args = CommandLineTokenizer.tokenize(args);

		if (sender instanceof Player) {
			xAuthPlayer p = plugin.getPlyrMngr().getPlayer((Player) sender);

			if (args.length < 2) {
				plugin.getMsgHndlr().sendMessage("changepw.usage", p.getPlayer());
				return true;
			}

			String oldPassword = args[0];
			String newPassword = args[1];

			Auth a = plugin.getAuthClass(p);
			boolean success = a.changePassword(p.getPlayerName(), oldPassword, newPassword);

			String response = a.getResponse();
			if (response != null)
				plugin.getMsgHndlr().sendMessage(response, p.getPlayer());

			if (success)
				xAuthLog.info(p.getPlayerName() + " has changed their password");

			return true;
		}

		return false;
	}
}