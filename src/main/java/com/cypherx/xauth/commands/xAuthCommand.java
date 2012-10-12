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
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.logging.Level;

public class xAuthCommand implements CommandExecutor {

    public xAuthCommand(xAuth plugin) {
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
            else if (subCommand.equals("lock"))
                return lockCommand(sender, args);
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
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        } else if (args.length < 3) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.register.usage", sender);
            return true;
        }

        String targetName = args[1];
        String password = args[2];
        String email = args.length > 3 ? args[3] : null;
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);

        Auth a = xAuth.getPlugin().getAuthClass(xp);
        boolean success = a.adminRegister(targetName, password, email);

        String response = a.getResponse();
        if (response != null)
            xAuth.getPlugin().getMessageHandler().sendMessage(response, sender, targetName);

        if (success)
            xAuthLog.info(sender.getName() + " has registered an account for " + targetName);

        return true;
    }

    private boolean changePwCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.changepw")) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        } else if (args.length < 3) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.changepw.usage", sender);
            return true;
        }

        String targetName = args[1];
        String newPassword = args[2];
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);

        Auth a = xAuth.getPlugin().getAuthClass(xp);
        boolean success = a.adminChangePassword(targetName, newPassword);

        String response = a.getResponse();
        if (response != null)
            xAuth.getPlugin().getMessageHandler().sendMessage(response, sender, targetName);

        if (success)
            xAuthLog.info(sender.getName() + " changed " + targetName + "'s password");

        return true;
    }

    private boolean logoutCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.logout")) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        } else if (args.length < 2) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.logout.usage", sender);
            return true;
        }

        String targetName = args[1];
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);

        if (!xp.isAuthenticated()) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.logout.error.logged", sender, targetName);
            return true;
        }

        boolean success = xAuth.getPlugin().getPlayerManager().deleteSession(xp.getAccountId());
        if (success) {
            xp.setStatus(Status.Registered);
            xAuth.getPlugin().getAuthClass(xp).offline(xp.getPlayerName());
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.logout.success.player", sender, targetName);

            Player target = xp.getPlayer();
            if (target != null) {
                xAuth.getPlugin().getPlayerManager().protect(xp);
                xAuth.getPlugin().getMessageHandler().sendMessage("admin.logout.success.target", target);
            }
        } else
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.logout.error.general", sender);

        return true;
    }

    private boolean unregisterCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.unregister")) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        } else if (args.length < 2) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.unregister.usage", sender);
            return true;
        }

        String targetName = args[1];
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);

        if (!xp.isRegistered()) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.unregister.error.registered", sender, targetName);
            return true;
        }

        boolean success = xAuth.getPlugin().getPlayerManager().deleteAccount(xp.getAccountId());
        if (success) {
            xp.setStatus(Status.Guest);
            xAuth.getPlugin().getAuthClass(xp).offline(xp.getPlayerName());
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.unregister.success.player", sender, targetName);

            Player target = xp.getPlayer();
            if (target != null) {
                xAuth.getPlugin().getPlayerManager().protect(xp);
                xAuth.getPlugin().getMessageHandler().sendMessage("admin.unregister.success.target", target);
            }

            xAuth.getPlugin().getPlayerManager().initAccount(xp.getAccountId());
        } else
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.unregister.error.general", sender);

        return true;
    }

    private boolean locationCommand(CommandSender sender, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            xAuthLog.info("This command cannot be executed from the console!");
            return true;
        }

        Player player = (Player) sender;
        if (!xAuth.getPermissionManager().has(player, "xauth.admin.location")) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.permission", player);
            return true;
        } else if (args.length < 2 || !(args[1].equals("set") || args[1].equals("remove"))) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.location.usage", player);
            return true;
        }

        String action = args[1];
        boolean global = args.length > 2 && args[2].equals("global");
        String response;

        if (action.equals("set")) {
            if (!global && player.getWorld().getUID().equals(xAuth.getPlugin().getLocationManager().getGlobalUID())) {
                xAuth.getPlugin().getMessageHandler().sendMessage("admin.location.set.error.global", player);
                return true;
            }

            boolean success = xAuth.getPlugin().getLocationManager().setLocation(player.getLocation(), global);
            if (success)
                response = "admin.location.set.success." + (global ? "global" : "regular");
            else
                response = "admin.location.set.error.general";
        } else {
            if (global) {
                if (xAuth.getPlugin().getLocationManager().getGlobalUID() == null) {
                    xAuth.getPlugin().getMessageHandler().sendMessage("admin.location.remove.error.noglobal", player);
                    return true;
                }
            } else {
                if (!xAuth.getPlugin().getLocationManager().isLocationSet(player.getWorld())) {
                    xAuth.getPlugin().getMessageHandler().sendMessage("admin.location.remove.error.notset", player);
                    return true;
                } else if (player.getWorld().getUID().equals(xAuth.getPlugin().getLocationManager().getGlobalUID())) {
                    xAuth.getPlugin().getMessageHandler().sendMessage("admin.location.remove.error.global", player);
                    return true;
                }
            }

            boolean success = xAuth.getPlugin().getLocationManager().removeLocation(player.getWorld());
            if (success)
                response = "admin.location.remove.success." + (global ? "global" : "regular");
            else
                response = "admin.location.remove.error.general";
        }

        xAuth.getPlugin().getMessageHandler().sendMessage(response, player);
        return true;
    }

    private boolean reloadCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.reload")) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        }

        xAuth.getPlugin().reload();
        xAuth.getPlugin().getMessageHandler().sendMessage("admin.reload", sender);
        return true;
    }

    private boolean activateCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.activate")) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        } else if (args.length < 2) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.activate.usage", sender);
            return true;
        }

        String targetName = args[1];
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);

        if (!xp.isRegistered()) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.activate.error.registered", sender);
            return true;
        } else if (xAuth.getPlugin().getPlayerManager().isActive(xp.getAccountId())) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.activate.error.active", sender);
            return true;
        }

        boolean success = xAuth.getPlugin().getPlayerManager().activateAcc(xp.getAccountId());
        xAuth.getPlugin().getMessageHandler().sendMessage(success ? "admin.activate.success" : "admin.activate.error.general", sender, targetName);

        return true;
    }

    private boolean lockCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.lock")) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        } else if (args.length < 2) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.lock.usage", sender);
            return true;
        }

        String targetName = args[1];
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);

        if (!xp.isRegistered()) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.lock.error.registered", sender);
            return true;
        } else if (!xAuth.getPlugin().getPlayerManager().isActive(xp.getAccountId())) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.lock.error.locked", sender);
            return true;
        }

        boolean success = xAuth.getPlugin().getPlayerManager().lockAcc(xp.getAccountId());
        xAuth.getPlugin().getMessageHandler().sendMessage(success ? "admin.lock.success" : "admin.lock.error.general", sender, targetName);

        return true;
    }

    private boolean configCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.config")) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        } else if (args.length < 2) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.config.usage", sender);
            return true;
        }

        String node = args[1];
        Object defVal = xAuth.getPlugin().getConfig().getDefaults().get(node);

        if (defVal == null) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.config.error.exist", sender);
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
                    nodeVal = xAuth.getPlugin().getConfig().get(node);
                } else {
                    xAuth.getPlugin().getConfig().set(node, value);
                }
            } else if (defVal instanceof Integer) {
                if (getVal) {
                    nodeVal = xAuth.getPlugin().getConfig().get(node);
                } else {
                    xAuth.getPlugin().getConfig().set(node, Integer.parseInt(value));
                }
            } else if (defVal instanceof Boolean) {
                if (getVal) {
                    nodeVal = xAuth.getPlugin().getConfig().get(node);
                } else {
                    xAuth.getPlugin().getConfig().set(node, Boolean.parseBoolean(value));
                }
            } else {
                throw new IllegalArgumentException();
            }
        } catch (NumberFormatException e) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.config.error.int", sender);
            return true;
        } catch (IllegalArgumentException e) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.config.error.invalid", sender);
            return true;
        }

        if (!getVal) {
            xAuth.getPlugin().saveConfig();
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.config.success", sender);
        } else {
            xAuth.getPlugin().getMessageHandler().sendMessage(String.format(xAuth.getPlugin().getMessageHandler().getNode("admin.config.value"), node, nodeVal), sender);
        }
        return true;
    }

    private boolean profileCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.profile")) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        } else if (args.length > 2) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.profile.usage", sender);
            return true;
        }

        if ((!(sender instanceof Player)) && (args.length < 2)) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.profile.error.console", sender);
            return true;
        }

        String targetName = (args.length > 1) ? args[1] : sender.getName();
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);

        StringBuilder sb = new StringBuilder("------ xAuth Profile ------").append("\n");
        String message = "";

        sb.append(ChatColor.WHITE + "Account-Id : ").append(xp.getAccountId()).append("\n");
        sb.append(ChatColor.WHITE + "Registered : ").append(((xp.isRegistered()) ? "{true}" : "{false}"));

        if (xp.isRegistered()) {
            sb.append("\n");
            sb.append(ChatColor.WHITE + "Name : ").append(xp.getPlayerName()).append("\n");
            if ((xp.isOnline()) && xp.isAuthenticated()) {
                sb.append(ChatColor.WHITE + "DisplayName : ").append(((xp.isAuthenticated()) ? xp.getPlayer().getDisplayName() : xp.getPlayerName())).append("\n");
            }
            sb.append(ChatColor.WHITE + "Authenticated : ").append(((xp.isAuthenticated()) ? "{true}" : "{false}")).append("\n");

            if (xp.getLoginTime() != null) {
                sb.append(ChatColor.WHITE + "Last login: ").append(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(xp.getLoginTime())).append("\n");
            }
            sb.append(ChatColor.WHITE + "Locked : ").append(((xp.isLocked()) ? "{true}" : "{false}"));
        }

        message = sb.toString()
                .replace("{true}", ChatColor.GREEN + "true")
                .replace("{false}", ChatColor.RED + "false");

        sender.sendMessage(message);

        return true;
    }

    private boolean debugCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.admin.config")) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        }

        if (args.length == 1) {
            xAuth.getPlugin().getMessageHandler().sendMessage(String.format(xAuth.getPlugin().getMessageHandler().getNode("admin.debug"), xAuthLog.getLevel().toString()), sender);
            return true;
        }

        Level toLevel = Level.INFO;
        if  ((!(args[1] == null)) || (!(args[1].isEmpty()))) {
            toLevel = Level.parse(args[1].toUpperCase());
            xAuthLog.setLevel(toLevel);
        }

        xAuth.getPlugin().getMessageHandler().sendMessage(String.format(xAuth.getPlugin().getMessageHandler().getNode("admin.debug"), toLevel.toString()), sender);
        return true;
    }

    private boolean versionCommand(CommandSender sender, String[] args) {
        if (!xAuth.getPermissionManager().has(sender, "xauth.version")) {
            xAuth.getPlugin().getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        }

        xAuthLog.info("Version command executed... checking for latest version");
        xAuth.getPlugin().getMessageHandler().sendMessage(String.format(xAuth.getPlugin().getMessageHandler().getNode("version.current-version"), xAuth.getPlugin().getDescription().getVersion()), sender);

        if (xAuth.getPlugin().getConfig().getBoolean("main.check-for-updates")) {
            Updater updater = new Updater(xAuth.getPlugin().getDescription().getVersion());
            if (updater.isUpdateAvailable()) {
                updater.printMessage();
                xAuth.getPlugin().getMessageHandler().sendMessage(String.format(xAuth.getPlugin().getMessageHandler().getNode("version.update-available"), updater.getLatestVersionString()), sender);
            } else {
                xAuthLog.info(String.format(xAuth.getPlugin().getMessageHandler().getNode("version.no-update-needed"), xAuth.getPlugin().getDescription().getVersion()));
                xAuth.getPlugin().getMessageHandler().sendMessage(String.format(xAuth.getPlugin().getMessageHandler().getNode("version.no-update-needed"), updater.getLatestVersionString()), sender);
            }
        }
        return true;
    }
}