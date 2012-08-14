/*
 * xAuth for Bukkit
 * Copyright (C) 2012 Lycano <https://github.com/lycano/xAuth/>
 *
 * Copyright (C) 2011 CypherX <https://github.com/CypherX/xAuth/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cypherx.xauth.commands;

import com.cypherx.xauth.auth.Auth;
import com.cypherx.xauth.updater.Updater;
import com.cypherx.xauth.utils.xAuthLog;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.xAuthPlayer.Status;
import com.martiansoftware.jsap.CommandLineTokenizer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class xAuthCommand implements CommandExecutor {
    private final xAuth plugin;

    public xAuthCommand(xAuth plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player || sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) {
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
            else if (subCommand.equals("profile") || subCommand.equals("info"))
                return profileCommand(sender, args);
            else if (subCommand.equals("debug"))
                return debugCommand(sender, args);
            else if (subCommand.equals("version"))
                return versionCommand(sender, args);

            return true;
        }

        return false;
    }

    private boolean registerCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.register")) {
            plugin.getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        } else if (args.length < 3) {
            plugin.getMessageHandler().sendMessage("admin.register.usage", sender);
            return true;
        }

        String targetName = args[1];
        String password = args[2];
        String email = args.length > 3 ? args[3] : null;
        xAuthPlayer xp = plugin.getPlayerManager().getPlayer(targetName);

        Auth a = plugin.getAuthClass(xp);
        boolean success = a.adminRegister(targetName, password, email);

        String response = a.getResponse();
        if (response != null)
            plugin.getMessageHandler().sendMessage(response, sender, targetName);

        if (success)
            xAuthLog.info(sender.getName() + " has registered an account for " + targetName);

        return true;
    }

    private boolean changePwCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.changepw")) {
            plugin.getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        } else if (args.length < 3) {
            plugin.getMessageHandler().sendMessage("admin.changepw.usage", sender);
            return true;
        }

        String targetName = args[1];
        String newPassword = args[2];
        xAuthPlayer xp = plugin.getPlayerManager().getPlayer(targetName);

        Auth a = plugin.getAuthClass(xp);
        boolean success = a.adminChangePassword(targetName, newPassword);

        String response = a.getResponse();
        if (response != null)
            plugin.getMessageHandler().sendMessage(response, sender, targetName);

        if (success)
            xAuthLog.info(sender.getName() + " changed " + targetName + "'s password");

        return true;
    }

    private boolean logoutCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.logout")) {
            plugin.getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        } else if (args.length < 2) {
            plugin.getMessageHandler().sendMessage("admin.logout.usage", sender);
            return true;
        }

        String targetName = args[1];
        xAuthPlayer xp = plugin.getPlayerManager().getPlayer(targetName);

        if (!xp.isAuthenticated()) {
            plugin.getMessageHandler().sendMessage("admin.logout.error.logged", sender, targetName);
            return true;
        }

        boolean success = plugin.getPlayerManager().deleteSession(xp.getAccountId());
        if (success) {
            xp.setStatus(Status.Registered);
            plugin.getAuthClass(xp).offline(xp.getPlayerName());
            plugin.getMessageHandler().sendMessage("admin.logout.success.player", sender, targetName);

            Player target = xp.getPlayer();
            if (target != null) {
                plugin.getPlayerManager().protect(xp);
                plugin.getMessageHandler().sendMessage("admin.logout.success.target", target);
            }
        } else
            plugin.getMessageHandler().sendMessage("admin.logout.error.general", sender);

        return true;
    }

    private boolean unregisterCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.unregister")) {
            plugin.getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        } else if (args.length < 2) {
            plugin.getMessageHandler().sendMessage("admin.unregister.usage", sender);
            return true;
        }

        String targetName = args[1];
        xAuthPlayer xp = plugin.getPlayerManager().getPlayer(targetName);

        if (!xp.isRegistered()) {
            plugin.getMessageHandler().sendMessage("admin.unregister.error.registered", sender, targetName);
            return true;
        }

        boolean success = plugin.getPlayerManager().deleteAccount(xp.getAccountId());
        if (success) {
            xp.setStatus(Status.Guest);
            plugin.getAuthClass(xp).offline(xp.getPlayerName());
            plugin.getMessageHandler().sendMessage("admin.unregister.success.player", sender, targetName);

            Player target = xp.getPlayer();
            if (target != null) {
                plugin.getPlayerManager().protect(xp);
                plugin.getMessageHandler().sendMessage("admin.unregister.success.target", target);
            }
        } else
            plugin.getMessageHandler().sendMessage("admin.unregister.error.general", sender);

        return true;
    }

    private boolean locationCommand(CommandSender sender, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            xAuthLog.info("This command cannot be executed from the console!");
            return true;
        }

        Player player = (Player) sender;
        if (!xAuth.getPermissionManager().has(player, "xauth.admin.location")) {
            plugin.getMessageHandler().sendMessage("admin.permission", player);
            return true;
        } else if (args.length < 2 || !(args[1].equals("set") || args[1].equals("remove"))) {
            plugin.getMessageHandler().sendMessage("admin.location.usage", player);
            return true;
        }

        String action = args[1];
        boolean global = args.length > 2 && args[2].equals("global");
        String response;

        if (action.equals("set")) {
            if (!global && player.getWorld().getUID().equals(plugin.getLocationManager().getGlobalUID())) {
                plugin.getMessageHandler().sendMessage("admin.location.set.error.global", player);
                return true;
            }

            boolean success = plugin.getLocationManager().setLocation(player.getLocation(), global);
            if (success)
                response = "admin.location.set.success." + (global ? "global" : "regular");
            else
                response = "admin.location.set.error.general";
        } else {
            if (global) {
                if (plugin.getLocationManager().getGlobalUID() == null) {
                    plugin.getMessageHandler().sendMessage("admin.location.remove.error.noglobal", player);
                    return true;
                }
            } else {
                if (!plugin.getLocationManager().isLocationSet(player.getWorld())) {
                    plugin.getMessageHandler().sendMessage("admin.location.remove.error.notset", player);
                    return true;
                } else if (player.getWorld().getUID().equals(plugin.getLocationManager().getGlobalUID())) {
                    plugin.getMessageHandler().sendMessage("admin.location.remove.error.global", player);
                    return true;
                }
            }

            boolean success = plugin.getLocationManager().removeLocation(player.getWorld());
            if (success)
                response = "admin.location.remove.success." + (global ? "global" : "regular");
            else
                response = "admin.location.remove.error.general";
        }

        plugin.getMessageHandler().sendMessage(response, player);
        return true;
    }

    private boolean reloadCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.reload")) {
            plugin.getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        }

        plugin.reload();
        plugin.getMessageHandler().sendMessage("admin.reload", sender);
        return true;
    }

    private boolean activateCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.activate")) {
            plugin.getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        } else if (args.length < 2) {
            plugin.getMessageHandler().sendMessage("admin.activate.usage", sender);
            return true;
        }

        String targetName = args[1];
        xAuthPlayer xp = plugin.getPlayerManager().getPlayer(targetName);

        if (!xp.isRegistered()) {
            plugin.getMessageHandler().sendMessage("admin.activate.error.registered", sender);
            return true;
        } else if (plugin.getPlayerManager().isActive(xp.getAccountId())) {
            plugin.getMessageHandler().sendMessage("admin.activate.error.active", sender);
            return true;
        }

        boolean success = plugin.getPlayerManager().activateAcc(xp.getAccountId());
        plugin.getMessageHandler().sendMessage(success ? "admin.activate.success" : "admin.activate.error.general", sender, targetName);

        return true;
    }

    private boolean configCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.config")) {
            plugin.getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        } else if (args.length < 2) {
            plugin.getMessageHandler().sendMessage("admin.config.usage", sender);
            return true;
        }

        String node = args[1];
        Object defVal = plugin.getConfig().getDefaults().get(node);

        if (defVal == null) {
            plugin.getMessageHandler().sendMessage("admin.config.error.exist", sender);
            return true;
        }

        boolean getVal = false;
        Object nodeVal = null;
        String value = null;
        if (args.length > 2) {
            value = args[2];
        }

        if ((value == null) || value.isEmpty())
            getVal = true;

        try {
            if (defVal instanceof String) {
                if (getVal) {
                    nodeVal = plugin.getConfig().get(node);
                } else {
                    plugin.getConfig().set(node, value);
                }
            } else if (defVal instanceof Integer) {
                if (getVal) {
                    nodeVal = plugin.getConfig().get(node);
                } else {
                    plugin.getConfig().set(node, Integer.parseInt(value));
                }
            } else if (defVal instanceof Boolean) {
                if (getVal) {
                    nodeVal = plugin.getConfig().get(node);
                } else {
                    plugin.getConfig().set(node, Boolean.parseBoolean(value));
                }
            } else {
                throw new IllegalArgumentException();
            }
        } catch (NumberFormatException e) {
            plugin.getMessageHandler().sendMessage("admin.config.error.int", sender);
            return true;
        } catch (IllegalArgumentException e) {
            plugin.getMessageHandler().sendMessage("admin.config.error.invalid", sender);
            return true;
        }

        if (!getVal) {
            plugin.saveConfig();
            plugin.getMessageHandler().sendMessage("admin.config.success", sender);
        } else {
            plugin.getMessageHandler().sendMessage(String.format(plugin.getMessageHandler().getNode("admin.config.value"), node, nodeVal), sender);
        }
        return true;
    }

    private boolean profileCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.profile")) {
            plugin.getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        } else if (args.length < 2) {
            plugin.getMessageHandler().sendMessage("admin.profile.usage", sender);
            return true;
        }

        String targetName = args[1];
        xAuthPlayer xp = plugin.getPlayerManager().getPlayer(targetName);

        StringBuilder sb = new StringBuilder("------ xAuth Profile ------");
        sb.append("Player: ").append(xp.getPlayerName()).append("\tTest");
        if (xp.isAuthenticated())
            sb.append("This player is registered and logged in.");
        else if (xp.isRegistered())
            sb.append("This player is registered but not logged in.");

        return true;
    }

    private boolean debugCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.config")) {
            plugin.getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        }

        if (args.length == 1) {
            plugin.getMessageHandler().sendMessage(String.format(plugin.getMessageHandler().getNode("admin.debug"), xAuthLog.getLevel().toString()), sender);
            return true;
        }

        Level toLevel = Level.INFO;
        if  ((!(args[1] == null)) || (!(args[1].isEmpty()))) {
            toLevel = Level.parse(args[1].toUpperCase());
            xAuthLog.setLevel(toLevel);
        }

        plugin.getMessageHandler().sendMessage(String.format(plugin.getMessageHandler().getNode("admin.debug"), toLevel.toString()), sender);
        return true;
    }

    private boolean versionCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.version")) {
            plugin.getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        }

        xAuthLog.info("Version command executed... checking for latest version");
        plugin.getMessageHandler().sendMessage(String.format(plugin.getMessageHandler().getNode("version.current-version"), plugin.getDescription().getVersion()), sender);

        if (xAuth.getPlugin().getConfig().getBoolean("main.check-for-updates")) {
            Updater updater = new Updater(xAuth.getPlugin().getDescription().getVersion());
            if (updater.isUpdateAvailable()) {
                updater.printMessage();
                plugin.getMessageHandler().sendMessage(String.format(plugin.getMessageHandler().getNode("version.update-available"), updater.getLatestVersionString()), sender);
            } else {
                xAuthLog.info(String.format(plugin.getMessageHandler().getNode("version.no-update-needed"), plugin.getDescription().getVersion()));
                plugin.getMessageHandler().sendMessage(String.format(plugin.getMessageHandler().getNode("version.no-update-needed"), updater.getLatestVersionString()), sender);
            }
        }
        return true;
    }
}