package com.cypherx.xauth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.xAuthPlayer.Status;
import com.martiansoftware.jsap.CommandLineTokenizer;

public class LogoutCommand implements CommandExecutor {
	private final xAuth plugin;

	public LogoutCommand(final xAuth plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		args = CommandLineTokenizer.tokenize(args);

		if (sender instanceof Player) {
			xAuthPlayer p = plugin.getPlyrMngr().getPlayer((Player) sender);
			String response = null;

			if (p.isAuthenticated()) {
				plugin.getPlyrMngr().protect(p);
				p.setStatus(Status.Registered);
				plugin.getAuthClass(p).offline(p.getPlayerName());
				response = "logout.success";
			} else
				response = "logout.error";

			plugin.getMsgHndlr().sendMessage(response, p.getPlayer());
			return true;
		}

		return false;
	}
}