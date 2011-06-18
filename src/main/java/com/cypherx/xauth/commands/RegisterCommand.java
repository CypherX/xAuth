package com.cypherx.xauth.commands;

import com.cypherx.xauth.Account;
import com.cypherx.xauth.Util;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.xAuthMessages;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.xAuthSettings;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {
	private final xAuth plugin;

	public RegisterCommand(xAuth plugin) {
	    this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			xAuthPlayer xPlayer = plugin.getDataManager().getPlayerByName(player.getName());

			if (!xAuthSettings.regEnabled) {
				xAuthMessages.send("regErrDisabled", player);
				return true;
			} else if ((xAuthSettings.requireEmail && args.length < 2) || args.length < 1) {
				xAuthMessages.send("regUsage", player);
				return true;
			} else if (xPlayer.isRegistered()) {
				xAuthMessages.send("regErrRegistered", player);
				return true;
			} else if (!xAuthSettings.allowMultiple && plugin.getDataManager().isHostUsed(Util.getHostFromPlayer(player))) {
				xAuthMessages.send("regErrMultiple", player);
				return true;
			}

			String password = args[0];
			String email = (args.length > 1 ? args[1] : null);

			if (!Util.isValidPass(password)) {
				xAuthMessages.send("regErrPassword", player);
				return true;
			} else if (xAuthSettings.validateEmail && !Util.isValidEmail(email)) {
				xAuthMessages.send("regErrEmail", player);
				return true;
			}

			xPlayer.setAccount(new Account(player.getName(), Util.encrypt(password), email));
			plugin.login(xPlayer);

			xAuthMessages.send("regSuccess", player);
			xAuthLog.info(player.getName() + " has registered!");
		}

		return true;
	}
}
