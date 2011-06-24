package com.cypherx.xauth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthMessages;
import com.cypherx.xauth.xAuthPlayer;

public class LogoutCommand implements CommandExecutor {
	private final xAuth plugin;

	public LogoutCommand(xAuth plugin) {
	    this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			xAuthPlayer xPlayer = plugin.getDataManager().getPlayer(player.getName());

			if (!xPlayer.hasSession()) {
				xAuthMessages.send("logoutErrLogged", player);
				return true;
			}

			plugin.createGuest(xPlayer);
			xAuthMessages.send("logoutSuccess", player);
		}

		return true;
	}
}