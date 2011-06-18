package com.cypherx.xauth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cypherx.xauth.Account;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.xAuthMessages;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.xAuthSettings;

public class LoginCommand implements CommandExecutor {
	private final xAuth plugin;

	public LoginCommand(xAuth plugin) {
	    this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			xAuthPlayer xPlayer = plugin.getDataManager().getPlayerByName(player.getName());

			if (args.length < 1) {
				xAuthMessages.send("loginUsage", player);
				return true;
			} else if (!xPlayer.isRegistered()) {
				xAuthMessages.send("loginErrRegistered", player);
				return true;
			} else if (xPlayer.hasSession()) {
				xAuthMessages.send("loginErrLogged", player);
				return true;
			}

			Account account = xPlayer.getAccount();
			String password = args[0];

			if (!plugin.checkPassword(account, password)) {
				if (xAuthSettings.maxStrikes > 0) {
					xPlayer.setStrikes(xPlayer.getStrikes() + 1);
					if (xPlayer.getStrikes() >= xAuthSettings.maxStrikes)
						plugin.strikeout(xPlayer);
				}

				xAuthMessages.send("loginErrPassword", player);
				return true;
			} else if (xAuthSettings.activation && account.getActive() == 0) {
				xAuthMessages.send("loginErrActivate", player);
				return true;
			}

			plugin.login(xPlayer);
			xAuthMessages.send("loginSuccess", player);
			xAuthLog.info(player.getName() + " has logged in");
		}

		return true;
	}
}