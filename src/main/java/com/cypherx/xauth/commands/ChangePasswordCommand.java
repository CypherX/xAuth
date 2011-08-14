package com.cypherx.xauth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cypherx.xauth.Account;
import com.cypherx.xauth.Util;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.xAuthMessages;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.xAuthSettings;

public class ChangePasswordCommand implements CommandExecutor {
	private final xAuth plugin;

	public ChangePasswordCommand(xAuth plugin) {
	    this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		args = Util.fixArgs(args);

		if (sender instanceof Player) {
			Player player = (Player)sender;
			xAuthPlayer xPlayer = plugin.getPlayer(player.getName());

			if (!xAuthSettings.pwAllowChange) {
				xAuthMessages.send("cpwErrDisabled", player);
				return true;
			} else if (!xPlayer.hasSession()) {
				xAuthMessages.send("cpwErrLogged", player);
				return true;
			} else if (args.length < 2) {
				xAuthMessages.send("cpwUsage", player);
				return true;
			}

			Account account = xPlayer.getAccount();
			String oldPassword = args[0];
			String newPassword = args[1];

			if (!plugin.checkPassword(account, oldPassword)) {
				xAuthMessages.send("cpwErrIncorrect", player);
				return true;
			} else if (!Util.isValidPass(newPassword)) {
				xAuthMessages.send("cpwErrInvalid", player);
				return true;
			}

			plugin.changePassword(account, newPassword);
			xAuthMessages.send("cpwSuccess", player);
			xAuthLog.info(player.getName() + " changed their password");
		}

		return true;
	}
}