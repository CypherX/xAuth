package com.cypherx.xauth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.auth.Auth;
import com.cypherx.xauth.xAuthPlayer.Status;
import com.martiansoftware.jsap.CommandLineTokenizer;

public class LoginCommand implements CommandExecutor {
	private final xAuth plugin;

	public LoginCommand(final xAuth plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		args = CommandLineTokenizer.tokenize(args);

		if (sender instanceof Player) {
			xAuthPlayer p = plugin.getPlyrMngr().getPlayer((Player) sender);

			if (args.length < 1) {
				plugin.getMsgHndlr().sendMessage("login.usage", p.getPlayer());
				return true;
			}

			String playerName = p.getPlayerName();
			String password = args[0];

			Auth a = plugin.getAuthClass(p);
			boolean success = a.login(playerName, password);

			String response = a.getResponse();
			if (response != null)
				plugin.getMsgHndlr().sendMessage(response, p.getPlayer());

			if (success) {
				plugin.getPlyrMngr().unprotect(p);
				p.setStatus(Status.Authenticated);
				plugin.getAuthClass(p).online(p.getPlayerName());
				xAuthLog.info(playerName + " has logged in");
			}

			return true;
		}

		return false;
	}
}