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
package de.luricos.bukkit.xAuth.commands;

import de.luricos.bukkit.xAuth.auth.AuthMethod;
import de.luricos.bukkit.xAuth.events.xAuthRegisterEvent;
import de.luricos.bukkit.xAuth.password.PasswordType;
import de.luricos.bukkit.xAuth.updater.Updater;
import de.luricos.bukkit.xAuth.utils.CommandLineTokenizer;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;

public class xAuthAdminCommands extends xAuthCommand implements CommandExecutor {

    public xAuthAdminCommands() {
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
            else if (subCommand.equals("resetpw"))
                return resetPasswordCommand(sender, args);
            else if (subCommand.equals("logout"))
                return logoutCommand(sender, args);
            else if (subCommand.equals("unregister") || subCommand.equals("unreg"))
                return unregisterCommand(sender, args);
            else if (subCommand.equals("location") || subCommand.equals("loc"))
                return locationCommand(sender, args);
            else if (subCommand.equals("reload"))
                return reloadCommand(sender, args);
            else if ((subCommand.equals("activate")) || (subCommand.equals("unlock")))
                return activateCommand(sender, args);
            else if (subCommand.equals("lock"))
                return lockCommand(sender, args);
            else if (subCommand.equals("count"))
                return countCommand(sender, args);
            else if (subCommand.equals("config") || subCommand.equals("conf"))
                return configCommand(sender, args);
            else if (subCommand.equals("profile") || subCommand.equals("info"))
                return profileCommand(sender, args);
            else if (subCommand.equals("debug"))
                return debugCommand(sender, args);
            else if (subCommand.equals("version"))
                return versionCommand(sender, args);
            else if (subCommand.equals("update"))
                return updateCommand(sender, args);
            else if (subCommand.equals("upgrade"))
                return upgradeCommand(sender, args);

            return true;
        }

        return false;
    }

    private boolean registerCommand(CommandSender sender, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.register"))
            return true;

        if (args.length < 3) {
            this.getMessageHandler().sendMessage("admin.register.usage", sender);
            return true;
        }

        String targetName = args[1];
        String password = args[2];
        String email = args.length > 3 ? args[3] : null;
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);

        AuthMethod a = xAuth.getPlugin().getAuthClass(xp);
        boolean success = a.adminRegister(targetName, password, email);

        String response = a.getResponse();
        if (response != null)
            this.getMessageHandler().sendMessage(response, sender, targetName);

        if (success) {
            // set registered user to target group
            boolean autoAssignGroup = xAuth.getPlugin().getConfig().getBoolean("groups.auto-assign", false);
            String joinGroupName = xAuth.getPlugin().getConfig().getString("groups.move-on-register", null);
            if ((autoAssignGroup) && (joinGroupName != null)) {
                xAuth.getPermissionManager().joinGroup(targetName, joinGroupName);

                this.callEvent(xAuthRegisterEvent.Action.PLAYER_GROUP_CHANGED, xp.getStatus());
            }

            xAuthLog.info(sender.getName() + " has registered an account for " + targetName);
        }
        return true;
    }

    private boolean changePwCommand(CommandSender sender, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.changepw"))
            return true;

        if (args.length < 3) {
            this.getMessageHandler().sendMessage("admin.changepw.usage", sender);
            return true;
        }

        String targetName = args[1];
        String newPassword = args[2];
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);
        int pwType = xp.getPasswordType().getTypeId();
        if (args.length > 3)
            pwType = Integer.parseInt(args[3]);

        AuthMethod a = xAuth.getPlugin().getAuthClass(xp);
        boolean success = a.adminChangePassword(targetName, newPassword, pwType);

        String response = a.getResponse();
        if (response != null)
            this.getMessageHandler().sendMessage(response, sender, targetName);

        if (success)
            xAuthLog.info(sender.getName() + " changed " + targetName + "'s password");

        return true;
    }

    private boolean resetPasswordCommand(CommandSender sender, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.resetpw"))
            return true;

        if (args.length > 3) {
            this.getMessageHandler().sendMessage("admin.resetpw.usage", sender);
            return true;
        } else if (args.length < 2) {
            this.getMessageHandler().sendMessage("admin.resetpw.usage", sender);
            return true;
        }

        String targetName = args[1];
        int pwType = PasswordType.DEFAULT.getTypeId();
        if (args.length > 2)
            pwType = Integer.parseInt(args[2]);

        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);

        AuthMethod a = xAuth.getPlugin().getAuthClass(xp);
        boolean success = a.adminResetPassword(targetName, pwType);

        String response = a.getResponse();
        if (response != null)
            this.getMessageHandler().sendMessage(response, sender, targetName);

        if (success) {
            xAuthLog.info(sender.getName() + " reset " + targetName + "'s password");
            this.getMessageHandler().sendMessage("admin.resetpw.success.target", xp.getPlayer());
            this.getMessageHandler().sendMessage("resetpw.reset-usage", xp.getPlayer());
            xp.setReset(true);
        }

        return true;
    }

    private boolean logoutCommand(CommandSender sender, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.logout"))
            return true;

        if (args.length < 2) {
            this.getMessageHandler().sendMessage("admin.logout.usage", sender);
            return true;
        }

        String targetName = args[1];
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);

        if (!xp.isAuthenticated()) {
            this.getMessageHandler().sendMessage("admin.logout.error.logged", sender, targetName);
            return true;
        }

        boolean success = xAuth.getPlugin().getPlayerManager().deleteSession(xp.getAccountId());
        if (success) {
            xp.setStatus(xAuthPlayer.Status.REGISTERED);

            // a forced logout will set resetMode to false as the user does not had any chance to reset his password
            if (xp.isReset()) {
                xp.setReset(false);
                xAuth.getPlugin().getPlayerManager().unSetReset(xp.getAccountId());
            }

            xAuth.getPlugin().getAuthClass(xp).offline(xp.getName());
            this.getMessageHandler().sendMessage("admin.logout.success.player", sender, targetName);

            Player target = xp.getPlayer();
            if (target != null) {
                xAuth.getPlugin().getPlayerManager().protect(xp);
                this.getMessageHandler().sendMessage("admin.logout.success.target", target);
            }
        } else
            this.getMessageHandler().sendMessage("admin.logout.error.general", sender);

        return true;
    }

    private boolean unregisterCommand(CommandSender sender, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.unregister"))
            return true;

        if (args.length < 2) {
            this.getMessageHandler().sendMessage("admin.unregister.usage", sender);
            return true;
        }

        String targetName = args[1];
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);

        if (!xp.isRegistered()) {
            this.getMessageHandler().sendMessage("admin.unregister.error.registered", sender, targetName);
            return true;
        }

        boolean success = xAuth.getPlugin().getPlayerManager().deleteAccount(xp.getAccountId());
        if (success) {
            xp.setStatus(xAuthPlayer.Status.GUEST);
            xAuth.getPlugin().getAuthClass(xp).offline(xp.getName());
            this.getMessageHandler().sendMessage("admin.unregister.success.player", sender, targetName);

            Player target = xp.getPlayer();
            if (target != null) {
                xAuth.getPlugin().getPlayerManager().protect(xp);
                this.getMessageHandler().sendMessage("admin.unregister.success.target", target);
            }

            xAuth.getPlugin().getPlayerManager().initAccount(xp.getAccountId());
        } else
            this.getMessageHandler().sendMessage("admin.unregister.error.general", sender);

        return true;
    }

    private boolean locationCommand(CommandSender sender, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            xAuthLog.info("This command cannot be executed from the console!");
            return true;
        }

        Player player = (Player) sender;
        if (!this.isAllowedCommand(player, "admin.permission", "xauth.location"))
            return true;

        if (args.length < 2 || !(args[1].equals("set") || args[1].equals("remove"))) {
            this.getMessageHandler().sendMessage("admin.location.usage", player);
            return true;
        }

        String action = args[1];
        boolean global = args.length > 2 && args[2].equals("global");
        String response;

        if (action.equals("set")) {
            if (!global && player.getWorld().getUID().equals(xAuth.getPlugin().getLocationManager().getGlobalUID())) {
                this.getMessageHandler().sendMessage("admin.location.set.error.global", player);
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
                    this.getMessageHandler().sendMessage("admin.location.remove.error.noglobal", player);
                    return true;
                }
            } else {
                if (!xAuth.getPlugin().getLocationManager().isLocationSet(player.getWorld())) {
                    this.getMessageHandler().sendMessage("admin.location.remove.error.notset", player);
                    return true;
                } else if (player.getWorld().getUID().equals(xAuth.getPlugin().getLocationManager().getGlobalUID())) {
                    this.getMessageHandler().sendMessage("admin.location.remove.error.global", player);
                    return true;
                }
            }

            boolean success = xAuth.getPlugin().getLocationManager().removeLocation(player.getWorld());
            if (success)
                response = "admin.location.remove.success." + (global ? "global" : "regular");
            else
                response = "admin.location.remove.error.general";
        }

        this.getMessageHandler().sendMessage(response, player);
        return true;
    }

    private boolean reloadCommand(CommandSender sender, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.reload"))
            return true;

        xAuth.getPlugin().reload();
        this.getMessageHandler().sendMessage("admin.reload", sender);
        return true;
    }

    private boolean activateCommand(CommandSender sender, String[] args) {
        return activateCommand(sender, args, false);
    }

    private boolean activateCommand(CommandSender sender, String[] args, boolean force) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.activate"))
            return true;

        if (args.length < 2) {
            this.getMessageHandler().sendMessage("admin.activate.usage", sender);
            return true;
        }

        String targetName = args[1];
        force = ((args.length > 2) && (args[2].equals("force")));
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);
        if (targetName.equals("*")) {
            Integer countState = xAuth.getPlugin().getPlayerManager().countLocked();
            boolean success = xAuth.getPlugin().getPlayerManager().setAllActiveStates(true, null);

            this.getMessageHandler().sendMessage(success ? "admin.activate.successM" : "admin.activate.error.generalM", sender, countState.toString());
            return true;
        }

        if (!xp.isRegistered()) {
            this.getMessageHandler().sendMessage("admin.activate.error.registered", sender, targetName);
            return true;
        } else if ((!force) && (xAuth.getPlugin().getPlayerManager().isActive(xp.getAccountId()))) {
            this.getMessageHandler().sendMessage("admin.activate.error.active", sender, targetName);
            return true;
        }

        boolean success = xAuth.getPlugin().getPlayerManager().activateAcc(xp.getAccountId());
        this.getMessageHandler().sendMessage(success ? "admin.activate.success" : "admin.activate.error.general", sender, targetName);

        return true;
    }

    private boolean lockCommand(CommandSender sender, String[] args) {
        return lockCommand(sender, args, false);
    }

    private boolean lockCommand(CommandSender sender, String[] args, boolean force) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.lock"))
            return true;

        if (args.length < 2) {
            this.getMessageHandler().sendMessage("admin.lock.usage", sender);
            return true;
        }

        String targetName = args[1];
        force = ((args.length > 2) && (args[2].equals("force")));
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);
        if (targetName.equals("*")) {
            Integer countState = xAuth.getPlugin().getPlayerManager().countActive();
            if (sender instanceof Player) {
                xp = xAuth.getPlugin().getPlayerManager().getPlayer(sender.getName());
                if (countState > 0)
                    countState--;
            }

            boolean success = xAuth.getPlugin().getPlayerManager().setAllActiveStates(false, new Integer[]{xp.getAccountId()});

            this.getMessageHandler().sendMessage(success ? "admin.lock.successM" : "admin.lock.error.generalM", sender, countState.toString());
            return true;
        }

        if (!xp.isRegistered()) {
            this.getMessageHandler().sendMessage("admin.lock.error.registered", sender);
            return true;
        } else if ((!force) && (!xAuth.getPlugin().getPlayerManager().isActive(xp.getAccountId()))) {
            this.getMessageHandler().sendMessage("admin.lock.error.locked", sender);
            return true;
        }

        boolean success = xAuth.getPlugin().getPlayerManager().lockAcc(xp.getAccountId());
        this.getMessageHandler().sendMessage(success ? "admin.lock.success" : "admin.lock.error.general", sender, targetName);

        return true;
    }

    private boolean countCommand(CommandSender sender, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.count"))
            return true;

        if (args.length < 2) {
            this.getMessageHandler().sendMessage("admin.count.usage", sender);
            return true;
        }

        Integer count = 0;
        String modeName = args[1].replace("-", "_");

        xAuthPlayerCountType playerCountType = xAuthPlayerCountType.getType(modeName);
        if (playerCountType == null) {
            this.getMessageHandler().sendMessage("admin.count.usage", sender);
            return true;
        }

        switch(playerCountType) {
            case ALL:
                count = xAuth.getPlugin().getPlayerManager().countAll();
                this.getMessageHandler().sendMessage("admin.count.success.all", sender, count.toString());
                break;
            case ACTIVE:
                count = xAuth.getPlugin().getPlayerManager().countActive();
                this.getMessageHandler().sendMessage("admin.count.success.active", sender, count.toString());
                break;
            case LOCKED:
                count = xAuth.getPlugin().getPlayerManager().countLocked();
                this.getMessageHandler().sendMessage("admin.count.success.locked", sender, count.toString());
                break;
            case PREMIUM:
                count = xAuth.getPlugin().getPlayerManager().countPremium();
                this.getMessageHandler().sendMessage("admin.count.success.premium", sender, count.toString());
                break;
            case NON_PREMIUM:
                count = xAuth.getPlugin().getPlayerManager().countNonPremium();
                this.getMessageHandler().sendMessage("admin.count.success.non-premium", sender, count.toString());
                break;
            default:
                this.getMessageHandler().sendMessage("admin.count.usage", sender);
                break;
        }

        return true;
    }

    private boolean configCommand(CommandSender sender, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.config"))
            return true;

        if (args.length < 2) {
            this.getMessageHandler().sendMessage("admin.config.usage", sender);
            return true;
        }

        String node = args[1];
        Object defVal = xAuth.getPlugin().getConfig().getDefaults().get(node);

        if (defVal == null) {
            this.getMessageHandler().sendMessage("admin.config.error.exist", sender);
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
            } else if (defVal instanceof List<?>) {
                if (getVal) {
                    nodeVal = xAuth.getPlugin().getConfig().get(node).toString();
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                throw new IllegalArgumentException();
            }
        } catch (NumberFormatException e) {
            this.getMessageHandler().sendMessage("admin.config.error.int", sender);
            return true;
        } catch (IllegalArgumentException e) {
            this.getMessageHandler().sendMessage("admin.config.error.invalid", sender);
            return true;
        }

        if (!getVal) {
            xAuth.getPlugin().saveConfig();
            this.getMessageHandler().sendMessage("admin.config.success", sender);
        } else {
            this.getMessageHandler().sendMessage(String.format(this.getMessageHandler().getNode("admin.config.value"), node, nodeVal), sender);
        }
        return true;
    }

    private boolean profileCommand(CommandSender sender, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.profile"))
            return true;

        if (args.length > 2) {
            this.getMessageHandler().sendMessage("admin.profile.usage", sender);
            return true;
        }

        if ((!(sender instanceof Player)) && (args.length < 2)) {
            this.getMessageHandler().sendMessage("admin.profile.error.console", sender);
            return true;
        }

        String targetName = (args.length > 1) ? args[1] : sender.getName();

        xAuthPlayer xp;
        try {
            Integer accountId = Integer.parseInt(targetName);
            xp = xAuth.getPlugin().getPlayerManager().getPlayerById(accountId);
        } catch (Exception e) {
            xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);
        }

        StringBuilder sb = new StringBuilder("------ xAuth Profile ------").append("\n");
        String message = "";

        sb.append(ChatColor.WHITE + "Account-Id : ").append(xp.getAccountId()).append("\n");
        sb.append(ChatColor.WHITE + "Registered : ").append(((xp.isRegistered()) ? "{true}" : "{false}"));

        if (xp.isRegistered()) {
            sb.append("\n");
            sb.append(ChatColor.WHITE + "Name : ").append(xp.getName()).append("\n");
            if ((xp.isOnline()) && xp.isAuthenticated()) {
                sb.append(ChatColor.WHITE + "DisplayName : ").append(((xp.isAuthenticated()) ? xp.getPlayer().getDisplayName() : xp.getName())).append("\n");
            }

            sb.append(ChatColor.WHITE + "Authenticated : ").append(((xp.isAuthenticated()) ? "{true}" : "{false}")).append("\n");
            sb.append(ChatColor.WHITE + "Premium : ").append(((xp.isPremium()) ? "{true}" : ChatColor.RED + "{false}")).append("\n");
            sb.append(ChatColor.WHITE + "Locked : ").append(((xp.isLocked()) ? "{true}" : "{false}")).append("\n");
            sb.append(ChatColor.WHITE + "ResetPw : ").append(((xp.isReset()) ? "{true}" : "{false}")).append("\n");
            sb.append(ChatColor.WHITE + "PWType : ").append(xp.getPasswordType().getName()).append("\n");

            if (xp.isOnline())
                sb.append(ChatColor.WHITE + "GameMode : ").append(xp.getGameMode()).append("\n");

            if (xp.getLoginTime() != null)
                sb.append(ChatColor.WHITE + "Last login: ").append(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(xp.getLoginTime()));
        }

        message = sb.toString()
                .replace("{true}", ChatColor.GREEN + "true")
                .replace("{false}", ChatColor.RED + "false");

        sender.sendMessage(message);

        return true;
    }

    private boolean debugCommand(CommandSender sender, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.config"))
            return true;

        if (!xAuth.getPermissionManager().has(sender, "xauth.allow.player.command.xauth.config")) {
            this.getMessageHandler().sendMessage("admin.permission", sender);
            return true;
        }

        if (args.length == 1) {
            this.getMessageHandler().sendMessage(String.format(this.getMessageHandler().getNode("admin.debug"), xAuthLog.getLevel().toString()), sender);
            return true;
        }

        Level toLevel = Level.INFO;
        if  ((!(args[1] == null)) || (!(args[1].isEmpty()))) {
            toLevel = Level.parse(args[1].toUpperCase());
            xAuthLog.setLevel(toLevel);
        }

        this.getMessageHandler().sendMessage(String.format(this.getMessageHandler().getNode("admin.debug"), toLevel.toString()), sender);
        return true;
    }

    private boolean versionCommand(CommandSender sender, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.version"))
            return true;

        this.getMessageHandler().sendMessage(String.format(this.getMessageHandler().getNode("version.current-version"), xAuth.getPlugin().getDescription().getVersion()), sender);
        return true;
    }

    private boolean updateCommand(CommandSender sender, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.update"))
            return true;

        xAuthLog.info("Update command executed... checking for existing update");

        Updater updater = xAuth.getUpdater();
        updater.setType(Updater.UpdateType.NO_DOWNLOAD);
        updater.setAnnounce(true);
        updater.run();

        String[] messages = updater.getResultMessages().split("\n");
        for (String message: messages) {
            this.getMessageHandler().sendMessage(message, sender);
        }

        return true;
    }

    private boolean upgradeCommand(CommandSender sender, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.upgrade"))
            return true;

        Updater updater = xAuth.getUpdater();
        updater.setType(Updater.UpdateType.NO_VERSION_CHECK);
        updater.setAnnounce(true);
        updater.run();

        String[] messages = updater.getResultMessages().split("\n");
        for (String message: messages) {
            this.getMessageHandler().sendMessage(message, sender);
        }

        return true;
    }
}