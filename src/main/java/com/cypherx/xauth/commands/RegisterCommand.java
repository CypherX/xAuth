package com.cypherx.xauth.commands;

import com.cypherx.xauth.Account;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.xAuthMessages;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.xAuthSettings;
import com.cypherx.xauth.database.DbUtil;
import com.cypherx.xauth.util.Util;
import com.cypherx.xauth.util.Validator;
import com.cypherx.xauth.util.encryption.Encrypt;

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
		args = Util.fixArgs(args);

		if (sender instanceof Player) {
			Player player = (Player)sender;
			xAuthPlayer xPlayer = plugin.getPlayer(player.getName());

			if (!xAuthSettings.regEnabled) {
				xAuthMessages.send("regErrDisabled", player);
				return true;
			} else if ((xAuthSettings.requireEmail && args.length < 2) || args.length < 1) {
				xAuthMessages.send("regUsage", player);
				return true;
			} else if (xPlayer.isRegistered()) {
				xAuthMessages.send("regErrRegistered", player);
				return true;
			} else if (xAuthSettings.accountLimit > 0 && DbUtil.getAccountCount(Util.getHostFromPlayer(player)) >= xAuthSettings.accountLimit) {
				xAuthMessages.send("regErrMultiple", player);
				return true;
			}

			String password = args[0];
			String email = (args.length > 1 ? args[1] : null);

			if (!Validator.isValidPass(password)) {
				xAuthMessages.send("regErrPassword", player);
				return true;
			} else if (xAuthSettings.validateEmail && !Validator.isValidEmail(email)) {
				xAuthMessages.send("regErrEmail", player);
				return true;
			}

			xPlayer.setAccount(new Account(player.getName(), Encrypt.custom(password), email));
			plugin.login(xPlayer);

			xAuthMessages.send("regSuccess", player);
			xAuthLog.info(player.getName() + " has registered!");
		}

		return true;
	}
}
