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
import com.cypherx.xauth.database.DbUtil;
import com.cypherx.xauth.util.Util;

public class LoginCommand implements CommandExecutor {
	private final xAuth plugin;

	public LoginCommand(xAuth plugin) {
	    this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		args = Util.fixArgs(args);

		if (sender instanceof Player) {
			Player player = (Player)sender;
			xAuthPlayer xPlayer = plugin.getPlayer(player.getName());

			if (args.length < 1) {
				xAuthMessages.send("loginUsage", player);
				return true;
			} else if (!xAuthSettings.authURLEnabled && !xPlayer.isRegistered()) {
				xAuthMessages.send("loginErrRegistered", player);
				return true;
			} else if (xPlayer.hasSession()) {
				xAuthMessages.send("loginErrLogged", player);
				return true;
			}

			Account account = xPlayer.getAccount();
			if(xAuthSettings.authURLEnabled && account == null){
				account = new Account(player.getName(), "authURL", null);
				xPlayer.setAccount(account);
			}
			String password = args[0];

			if (!plugin.checkPassword(account, password)) {
				if (xAuthSettings.maxStrikes > 0) {
					String host = Util.getHostFromPlayer(player);
					DbUtil.insertStrike(host, player.getName());
					int strikes = DbUtil.getStrikeCount(host);

					if (strikes >= xAuthSettings.maxStrikes) {
						player.kickPlayer(xAuthMessages.get("miscKickStrike", player, null));
						xAuthLog.info(player.getName() + " has exceeded the incorrect password threshold.");
					}
				}

				xAuthMessages.send("loginErrPassword", player);
				return true;
			}

			int active = DbUtil.getActive(player.getName());
			account.setActive(active);

			if (xAuthSettings.activation && active == 0) {
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