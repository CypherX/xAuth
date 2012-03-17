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

public class RegisterCommand implements CommandExecutor {
	private final xAuth plugin;

	public RegisterCommand(final xAuth plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		args = CommandLineTokenizer.tokenize(args);

		if (sender instanceof Player) {
			xAuthPlayer p = plugin.getPlyrMngr().getPlayer((Player) sender);

			if ((plugin.getConfig().getBoolean("registration.require-email") && args.length < 2) || args.length < 1) {
				plugin.getMsgHndlr().sendMessage("register.usage", p.getPlayer());
				return true;
			}

			String playerName = p.getPlayerName();
			String password = args[0];
			String email = args.length > 1 ? args[1] : null;

			Auth a = plugin.getAuthClass(p);
			boolean success = a.register(playerName, password, email);

			String response = a.getResponse();
			if (response != null)
				plugin.getMsgHndlr().sendMessage(response, p.getPlayer());

			if (success)
				xAuthLog.info(playerName + " has registered");

			return true;
		}

		return false;
	}	
}