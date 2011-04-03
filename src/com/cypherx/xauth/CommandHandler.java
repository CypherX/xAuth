package com.cypherx.xauth;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.craftbukkit.CraftServer;

public class CommandHandler
{
	private final xAuth plugin;
	private final PluginDescriptionFile pdfFile;

	public CommandHandler(final xAuth instance)
	{
		plugin = instance;
		pdfFile = plugin.getDescription();
	}

	public void handlePlayerCommand(Player player, Command cmd, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("register"))
    	{
			if (args.length != 1)
				player.sendMessage(xAuth.strings.getString("register.usage"));
			else if (!xAuth.settings.getBool("registration.enabled"))
				player.sendMessage(xAuth.strings.getString("register.err.disabled"));
			else if (plugin.isRegistered(player.getName()))
				player.sendMessage(xAuth.strings.getString("register.err.registered"));
			else if (args[0].length() < xAuth.settings.getInt("security.password.min-length"))
				player.sendMessage(xAuth.strings.getString("register.err.password", xAuth.settings.getInt("security.password.min-length")));
			else
			{
				plugin.addAuth(player.getName(), args[0]);
				plugin.login(player);
				player.sendMessage(xAuth.strings.getString("register.success1"));
				player.sendMessage(xAuth.strings.getString("register.success2", args[0]));
				System.out.println("[" + pdfFile.getName() + "] Player '" + player.getName() + "' has registered");
			}
    	}
		else if (cmd.getName().equalsIgnoreCase("login"))
    	{
			if (args.length != 1)
				player.sendMessage(xAuth.strings.getString("login.usage"));
			else if (!plugin.isRegistered(player.getName()))
				player.sendMessage(xAuth.strings.getString("login.err.registered"));
			else if (plugin.sessionExists(player.getName()))
				player.sendMessage(xAuth.strings.getString("login.err.logged"));
			else
			{
				if (plugin.checkPass(player, args[0]))
				{
					if (xAuth.settings.getBool("login.strikes.enabled"))
						plugin.clearStrikes(player);

					plugin.login(player);
					player.sendMessage(xAuth.strings.getString("login.success"));
					System.out.println("[" + pdfFile.getName() + "] Player '" + player.getName() + "' has authenticated");
				}
				else
				{
					player.sendMessage(xAuth.strings.getString("login.err.password"));

					if (xAuth.settings.getBool("login.strikes.enabled"))
					{
						plugin.addStrike(player);

						if (plugin.getStrikes(player) >= xAuth.settings.getInt("login.strikes.amount"))
						{
							String addr = player.getAddress().getAddress().getHostAddress();
							Server server = plugin.getServer();
							server.dispatchCommand(((CraftServer)server).getServer().console, "ban-ip " + addr);
							//server.dispatchCommand(((CraftServer)server).getServer().console, "kick " + player.getName());
							player.kickPlayer(xAuth.strings.getString("login.err.kick"));
							plugin.clearStrikes(player);
							System.out.println("[" + pdfFile.getName() + "] " + addr + " banned by Strike system");
						}
					}
				}
			}
    	}
		else if (cmd.getName().equalsIgnoreCase("changepw"))
    	{
			if (plugin.canUseCommand(player, "xauth.admin.changepw"))
			{
				if (args.length == 1)
				{
					if (!plugin.sessionExists(player.getName()))
						player.sendMessage(xAuth.strings.getString("changepw.err.login"));
					else if (!xAuth.settings.getBool("misc.allow-changepw"))
						player.sendMessage(xAuth.strings.getString("changepw.err.disabled"));
					else if (args[0].length() < xAuth.settings.getInt("security.password.min-length"))
						player.sendMessage(xAuth.strings.getString("register.err.password", xAuth.settings.getInt("security.password.min-length")));
					else
					{
						plugin.changePass(player.getName(), args[0]);
						player.sendMessage(xAuth.strings.getString("changepw.success.self", args[0]));
						System.out.println("[" + pdfFile.getName() + "] Player '" + player.getName() + "' has changed their password");
					}
				}
				else if (args.length == 2)
				{
					if (!plugin.isRegistered(args[0]))
						player.sendMessage(xAuth.strings.getString("changepw.err.registered"));
					else
					{
						plugin.changePass(args[0], args[1]);
						player.sendMessage(xAuth.strings.getString("changepw.success.other"));
						System.out.println("[" + pdfFile.getName() + "] " + player.getName() + " has changed " + args[0] + "'s password");
					}
				}
				else
					player.sendMessage(xAuth.strings.getString("changepw.usage2"));
			}
			else
			{
				if (args.length != 1)
					player.sendMessage(xAuth.strings.getString("changepw.usage1"));
				else if (!plugin.sessionExists(player.getName()))
					player.sendMessage(xAuth.strings.getString("changepw.err.login"));
				else if (!xAuth.settings.getBool("misc.allow-changepw"))
					player.sendMessage(xAuth.strings.getString("changepw.err.disabled"));
				else if (args[0].length() < xAuth.settings.getInt("security.password.min-length"))
					player.sendMessage(xAuth.strings.getString("register.err.password", xAuth.settings.getInt("security.password.min-length")));
				else
				{
					plugin.changePass(player.getName(), args[0]);
					player.sendMessage(xAuth.strings.getString("changepw.success.self", args[0]));
					System.out.println("[" + pdfFile.getName() + "] Player '" + player.getName() + "' has changed their password");
				}
			}
    	}
		else if (cmd.getName().equalsIgnoreCase("unregister"))
		{
			if (plugin.canUseCommand(player, "xauth.admin.unregister"))
			{
				if (args.length != 1)
					player.sendMessage(xAuth.strings.getString("unregister.usage"));
				else if (!plugin.isRegistered(args[0]))
					player.sendMessage(xAuth.strings.getString("changepw.err.registered"));
				else
				{
					plugin.removeAuth(args[0]);
					Player target = plugin.getServer().getPlayer(args[0]);

					if (target != null)
					{
						if (xAuth.settings.getBool("registration.forced"))
							plugin.saveInventory(target);
						target.sendMessage(xAuth.strings.getString("unregister.target"));
					}

					player.sendMessage(xAuth.strings.getString("unregister.success", args[0]));
					System.out.println("[" + pdfFile.getName() + "] " + player.getName() + " has unregistered " + args[0]);
				}
			}
		}
		else if (cmd.getName().equalsIgnoreCase("authreload"))
    	{
			if (plugin.canUseCommand(player, "xauth.admin.reload"))
			{
				plugin.reload();
				player.sendMessage(xAuth.strings.getString("reload.success"));
			}
    	}
		else if (cmd.getName().equalsIgnoreCase("toggle"))
		{
			Boolean canToggleReg = plugin.canUseCommand(player, "xauth.admin.toggle.reg");
			Boolean canTogglePw = plugin.canUseCommand(player, "xauth.admin.toggle.changepw");
			Boolean canToggleSave = plugin.canUseCommand(player, "xauth.admin.toggle.autosave");

			if (canToggleReg || canTogglePw || canToggleSave)
			{
				if (args.length != 1)
					player.sendMessage(xAuth.strings.getString("toggle.usage"));
				else if (args[0].equalsIgnoreCase("reg"))
				{
					if (!canToggleReg)
						player.sendMessage(xAuth.strings.getString("toggle.err.permission"));
					else
					{
						Boolean b = xAuth.settings.getBool("registration.enabled");
						xAuth.settings.updateValue("registration.enabled", (b ? false : true));
						player.sendMessage(xAuth.strings.getString("toggle.success.reg",
								(b ? xAuth.strings.getString("misc.disabled") : xAuth.strings.getString("misc.enabled"))));
						System.out.println("[" + pdfFile.getName() + "] " + player.getName() + " has " + (b ? "disabled" : "enabled") + " registrations");
					}
						
				}
				else if (args[0].equalsIgnoreCase("changepw"))
				{
					if (!canTogglePw)
						player.sendMessage(xAuth.strings.getString("toggle.err.permission"));
					else
					{
						Boolean b = xAuth.settings.getBool("misc.allow-changepw");
						xAuth.settings.updateValue("misc.allow-changepw", (b ? false : true));
						player.sendMessage(xAuth.strings.getString("toggle.success.pw",
								(b ? xAuth.strings.getString("misc.disabled") : xAuth.strings.getString("misc.enabled"))));
						System.out.println("[" + pdfFile.getName() + "] " + player.getName() + " has " + (b ? "disabled" : "enabled") + " password changes");
					}
				}
				else if (args[0].equalsIgnoreCase("autosave"))
				{
					if (!canToggleSave)
						player.sendMessage(xAuth.strings.getString("toggle.err.permission"));
					else
					{
						Boolean b = xAuth.settings.getBool("misc.autosave");
						xAuth.settings.updateValue("misc.autosave", (b ? false : true));
						player.sendMessage(xAuth.strings.getString("toggle.success.save",
								(b ? xAuth.strings.getString("misc.disabled") : xAuth.strings.getString("misc.enabled"))));
						System.out.println("[" + pdfFile.getName() + "] " + player.getName() + " has " + (b ? "disabled" : "enabled") + " autosave");
					}
				}
				else
					player.sendMessage(xAuth.strings.getString("toggle.usage"));
			}
		}
		else if (cmd.getName().equalsIgnoreCase("logout"))
		{
			//logout current player
			if (args.length == 0)
				plugin.killSession(player);
			//logout other player
			else
			{
				if (plugin.canUseCommand(player, "xauth.admin.logout"))
				{
					String target = buildName(args);
					if (!plugin.sessionExists(target))
						player.sendMessage(xAuth.strings.getString("logout.err.session"));
					else
					{
						Player pTarget = plugin.getServer().getPlayer(target);
						plugin.removeSession(target);
						
						if (pTarget != null)
						{
							plugin.saveInventory(pTarget);
							pTarget.sendMessage(xAuth.strings.getString("logout.success.ended"));
						}

						player.sendMessage(xAuth.strings.getString("logout.success.other", target));
					}
				}
			}
		}
	}

	public void handleConsoleCommand(Command cmd, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("register"))
    	{
			if (args.length != 2)
				System.out.println("Correct Usage: /register <player> <password>");
			else if (plugin.isRegistered(args[0]))
				System.out.println("Player '" + args[0] + "' is already registered");
			else
			{
				plugin.addAuth(args[0], args[1]);
				System.out.println(args[0] + " has been registered with password: " + args[1]);
			}
    	}
		else if (cmd.getName().equalsIgnoreCase("changepw"))
    	{
			if (args.length != 2)
				System.out.println("Correct Usage: /changepw <player> <newpassword>");
			else if (!plugin.isRegistered(args[0]))
				System.out.println("Player '" + args[0] + "' is not registered");
			else
			{
				plugin.changePass(args[0], args[1]);
				System.out.println(args[0] + "'s password has been changed to: " + args[1]);
			}
    	}
		else if (cmd.getName().equalsIgnoreCase("unregister"))
		{
			if (args.length != 1)
				System.out.println("Correct Usage: /unregister <player>");
			else if (!plugin.isRegistered(args[0]))
				System.out.println("Player '" + args[0] + "' is not registered");
			else
			{
				plugin.removeAuth(args[0]);
				Player target = plugin.getServer().getPlayer(args[0]);

				if (target != null)
				{
					if (xAuth.settings.getBool("registration.forced"))
						plugin.saveInventory(target);
					target.sendMessage(xAuth.strings.getString("unregister.target"));
				}

				System.out.println(args[0] + " has been unregistered");
			}
		}
		else if (cmd.getName().equalsIgnoreCase("authreload"))
			plugin.reload();
		else if (cmd.getName().equalsIgnoreCase("toggle"))
		{
			if (args.length != 1)
				System.out.println("Correct Usage: /toggle <reg|changepw|autosave>");
			else if (args[0].equalsIgnoreCase("reg"))
			{
				Boolean b = xAuth.settings.getBool("registration.enabled");
				xAuth.settings.updateValue("registration.enabled", (b ? false : true));
				System.out.println("[" + pdfFile.getName() + "] Registrations are now " + (b ? "disabled" : "enabled"));
			}
			else if (args[0].equalsIgnoreCase("changepw"))
			{
				Boolean b = xAuth.settings.getBool("misc.allow-changepw");
				xAuth.settings.updateValue("misc.allow-changepw", (b ? false : true));
				System.out.println("[" + pdfFile.getName() + "] Password changes are now " + (b ? "disabled" : "enabled"));
			}
			else if (args[0].equalsIgnoreCase("autosave"))
			{
				Boolean b = xAuth.settings.getBool("misc.autosave");
				xAuth.settings.updateValue("misc.autosave", (b ? false : true));
				System.out.println("[" + pdfFile.getName() + "] Autosaving of account modifications is now " + (b ? "disabled" : "enabled"));
			}
			else
				System.out.println("Correct Usage: /toggle <reg|changepw|autosave>");
		}
		else if (cmd.getName().equalsIgnoreCase("logout"))
		{
			if (args.length < 1)
				System.out.println("Correct Usage: /logout <player>");
			else
			{
				String target = buildName(args);
				if (!plugin.sessionExists(target))
					System.out.println("[" + pdfFile.getName() + "] This player does not have an active session.");
				else
				{
					Player pTarget = plugin.getServer().getPlayer(target);
					plugin.removeSession(target);
					
					if (pTarget != null)
					{
						plugin.saveInventory(pTarget);
						pTarget.sendMessage(xAuth.strings.getString("logout.success.ended"));
					}
					
					System.out.println("[" + pdfFile.getName() + "] " + target + "'s session has been terminated");
				}
			}
		}
	}

	private String buildName(String[] args)
	{
		String s = "";

		for (int i = 0; i < args.length; i++)
		{
			if (args[i] == null)
				continue;

			s += args[i] + " ";
		}
		
		return s.trim();
	}
}