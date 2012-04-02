package com.cypherx.xauth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.auth.Auth;
import com.cypherx.xauth.plugins.xPermissions;
import com.cypherx.xauth.xAuthPlayer.Status;
import com.martiansoftware.jsap.CommandLineTokenizer;

public class xAuthCommand implements CommandExecutor {
	private final xAuth plugin;

	public xAuthCommand(xAuth plugin) {
	    this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
			if (args.length < 1)
				return false;

			args = CommandLineTokenizer.tokenize(args);
			String subCommand = args[0];
			if (subCommand.equals("register"))
				return registerCommand(sender, args);
			else if (subCommand.equals("changepw") || subCommand.equals("cpw") || subCommand.equals("changepassword") || subCommand.equals("changepass"))
				return changePwCommand(sender, args);
			else if (subCommand.equals("logout"))
				return logoutCommand(sender, args);
			else if (subCommand.equals("unregister") || subCommand.equals("unreg"))
				return unregisterCommand(sender, args);
			else if (subCommand.equals("location") || subCommand.equals("loc"))
				return locationCommand(sender, args);
			else if (subCommand.equals("reload"))
				return reloadCommand(sender, args);
			else if (subCommand.equals("activate"))
				return activateCommand(sender, args);
			else if (subCommand.equals("config") || subCommand.equals("conf"))
				return configCommand(sender, args);

			return true;
		}

		return false;
	}

	private boolean registerCommand(CommandSender sender, String[] args) {
		if (!xPermissions.has(sender, "xauth.admin.register")) {
			plugin.getMsgHndlr().sendMessage("admin.permission", sender);
			return true;
		} else if (args.length < 3) {
			plugin.getMsgHndlr().sendMessage("admin.register.usage", sender);
			return true;
		}

		String targetName = args[1];
		String password = args[2];
		String email = args.length > 3 ? args[3] : null;
		xAuthPlayer xp = plugin.getPlyrMngr().getPlayer(targetName);

		Auth a = plugin.getAuthClass(xp);
		boolean success = a.adminRegister(targetName, password, email);

		String response = a.getResponse();
		if (response != null)
			plugin.getMsgHndlr().sendMessage(response, sender, targetName);

		if (success)
			xAuthLog.info(sender.getName() + " has registered an account for " + targetName);

		return true;
	}

	private boolean changePwCommand(CommandSender sender, String[] args) {
		if (!xPermissions.has(sender, "xauth.admin.changepw")) {
			plugin.getMsgHndlr().sendMessage("admin.permission", sender);
			return true;
		} else if (args.length < 3) {
			plugin.getMsgHndlr().sendMessage("admin.changepw.usage", sender);
			return true;
		}

		String targetName = args[1];
		String newPassword = args[2];
		xAuthPlayer xp = plugin.getPlyrMngr().getPlayer(targetName);

		Auth a = plugin.getAuthClass(xp);
		boolean success = a.adminChangePassword(targetName, newPassword);

		String response = a.getResponse();
		if (response != null)
			plugin.getMsgHndlr().sendMessage(response, sender, targetName);

		if (success)
			xAuthLog.info(sender.getName() + " changed " + targetName + "'s password");

		return true;
	}

	private boolean logoutCommand(CommandSender sender, String[] args) {
		if (!xPermissions.has(sender, "xauth.admin.logout")) {
			plugin.getMsgHndlr().sendMessage("admin.permission", sender);
			return true;
		} else if (args.length < 2) {
			plugin.getMsgHndlr().sendMessage("admin.logout.usage", sender);
			return true;
		}

		String targetName = args[1];
		xAuthPlayer xp = plugin.getPlyrMngr().getPlayer(targetName);

		if (!xp.isAuthenticated()) {
			plugin.getMsgHndlr().sendMessage("admin.logout.error.logged", sender, targetName);
			return true;
		}

		boolean success = plugin.getPlyrMngr().deleteSession(xp.getAccountId());
		if (success) {
			xp.setStatus(Status.Registered);
			plugin.getAuthClass(xp).offline(xp.getPlayerName());
			plugin.getMsgHndlr().sendMessage("admin.logout.success.player", sender, targetName);

			Player target = xp.getPlayer();
			if (target != null) {
				plugin.getPlyrMngr().protect(xp);
				plugin.getMsgHndlr().sendMessage("admin.logout.success.target", target);
			}
		} else
			plugin.getMsgHndlr().sendMessage("admin.logout.error.general", sender);

		return true;
	}

	private boolean unregisterCommand(CommandSender sender, String[] args) {
		if (!xPermissions.has(sender, "xauth.admin.unregister")) {
			plugin.getMsgHndlr().sendMessage("admin.permission", sender);
			return true;
		} else if (args.length < 2) {
			plugin.getMsgHndlr().sendMessage("admin.unregister.usage", sender);
			return true;
		}

		String targetName = args[1];
		xAuthPlayer xp = plugin.getPlyrMngr().getPlayer(targetName);

		if (!xp.isRegistered()) {
			plugin.getMsgHndlr().sendMessage("admin.unregister.error.registered", sender, targetName);
			return true;
		}

		boolean success = plugin.getPlyrMngr().deleteAccount(xp.getAccountId());
		if (success) {
			xp.setStatus(Status.Guest);
			plugin.getAuthClass(xp).offline(xp.getPlayerName());
			plugin.getMsgHndlr().sendMessage("admin.unregister.success.player", sender, targetName);

			Player target = xp.getPlayer();
			if (target != null) {
				plugin.getPlyrMngr().protect(xp);
				plugin.getMsgHndlr().sendMessage("admin.unregister.success.target", target);
			}
		} else
			plugin.getMsgHndlr().sendMessage("admin.unregister.error.general", sender);

		return true;
	}

	private boolean locationCommand(CommandSender sender, String[] args) {
		if (sender instanceof ConsoleCommandSender) {
			xAuthLog.info("This command cannot be executed from the console!");
			return true;
		}

		Player player = (Player) sender;
		if (!xPermissions.has(player, "xauth.admin.location")) {
			plugin.getMsgHndlr().sendMessage("admin.permission", player);
			return true;
		} else if (args.length < 2 || !(args[1].equals("set") || args[1].equals("remove"))) {
			plugin.getMsgHndlr().sendMessage("admin.location.usage", player);
			return true;
		}

		String action = args[1];
		boolean global = args.length > 2 && args[2].equals("global") ? true : false;
		String response;

		if (action.equals("set")) {
			if (!global && player.getWorld().getUID().equals(plugin.getLocMngr().getGlobalUID())) {
				plugin.getMsgHndlr().sendMessage("admin.location.set.error.global", player);
				return true;
			}

			boolean success = plugin.getLocMngr().setLocation(player.getLocation(), global);
			if (success)
				response = "admin.location.set.success." + (global ? "global" : "regular");
			else
				response = "admin.location.set.error.general";
		} else {
			if (global) {
				if (plugin.getLocMngr().getGlobalUID() == null) {
					plugin.getMsgHndlr().sendMessage("admin.location.remove.error.noglobal", player);
					return true;
				}
			} else {
				if (!plugin.getLocMngr().isLocationSet(player.getWorld())) {
					plugin.getMsgHndlr().sendMessage("admin.location.remove.error.notset", player);
					return true;
				} else if (player.getWorld().getUID().equals(plugin.getLocMngr().getGlobalUID())) {
					plugin.getMsgHndlr().sendMessage("admin.location.remove.error.global", player);
					return true;
				}
			}

			boolean success = plugin.getLocMngr().removeLocation(player.getWorld());
			if (success)
				response = "admin.location.remove.success." + (global ? "global" : "regular");
			else
				response = "admin.location.remove.error.general";
		}

		plugin.getMsgHndlr().sendMessage(response, player);
		return true;
	}

	private boolean reloadCommand(CommandSender sender, String[] args) {
		if (!xPermissions.has(sender, "xauth.admin.reload")) {
			plugin.getMsgHndlr().sendMessage("admin.permission", sender);
			return true;
		}

		plugin.reload();
		plugin.getMsgHndlr().sendMessage("admin.reload", sender);
		return true;
	}

	private boolean activateCommand(CommandSender sender, String[] args) {
		if (!xPermissions.has(sender, "xauth.admin.activate")) {
			plugin.getMsgHndlr().sendMessage("admin.permission", sender);
			return true;
		} else if (args.length < 2) {
			plugin.getMsgHndlr().sendMessage("admin.activate.usage", sender);
			return true;
		}

		String targetName = args[1];
		xAuthPlayer xp = plugin.getPlyrMngr().getPlayer(targetName);

		if (!xp.isRegistered()) {
			plugin.getMsgHndlr().sendMessage("admin.activate.error.registered", sender);
			return true;
		} else if (plugin.getPlyrMngr().isActive(xp.getAccountId())) {
			plugin.getMsgHndlr().sendMessage("admin.activate.error.active", sender);
			return true;
		}

		boolean success = plugin.getPlyrMngr().activateAcc(xp.getAccountId());
		plugin.getMsgHndlr().sendMessage(success ? "admin.activate.success" : "admin.activate.error.general", sender, targetName);

		return true;
	}

	private boolean configCommand(CommandSender sender, String[] args) {
		if (!xPermissions.has(sender, "xauth.admin.config")) {
			plugin.getMsgHndlr().sendMessage("admin.permission", sender);
			return true;
		} else if (args.length < 3) {
			plugin.getMsgHndlr().sendMessage("admin.config.usage", sender);
			return true;
		}

		String node = args[1];
		Object defVal = plugin.getConfig().getDefaults().get(node);

		if (defVal == null) {
			plugin.getMsgHndlr().sendMessage("admin.config.error.exist", sender);
			return true;
		}

		String value = args[2];

		try {
			if (defVal instanceof String)
				plugin.getConfig().set(node, value);
			else if (defVal instanceof Integer)
				plugin.getConfig().set(node, Integer.parseInt(value));
			else if (defVal instanceof Boolean)
				plugin.getConfig().set(node, Boolean.parseBoolean(value));
			else
				throw new IllegalArgumentException();
		} catch (NumberFormatException e) {
			plugin.getMsgHndlr().sendMessage("admin.config.error.int", sender);
			return true;
		} catch (IllegalArgumentException e) {
			plugin.getMsgHndlr().sendMessage("admin.config.error.invalid", sender);
			return true;
		}

		plugin.saveConfig();
		plugin.getMsgHndlr().sendMessage("admin.config.success", sender);
		return true;
	}
}