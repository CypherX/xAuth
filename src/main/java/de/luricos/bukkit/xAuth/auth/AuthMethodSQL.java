/*
 * xAuth for Bukkit
 * Copyright (C) 2012 Lycano <https://github.com/lycano/xAuth/>
 *
 * Copyright (C) 2011 CypherX <https://github.com/CypherX/xAuth/>
 * Copyright (C) 2011 Module AuthSQL: moparisthebest <http://www.moparscape.org/smf/>
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
package de.luricos.bukkit.xAuth.auth;

import com.avaje.ebean.validation.factory.EmailValidatorFactory;
import de.luricos.bukkit.xAuth.database.Table;
import de.luricos.bukkit.xAuth.password.PasswordType;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthMethodSQL extends AuthMethod {
    private final xAuthPlayer player;

    public AuthMethodSQL(final xAuth plugin, final xAuthPlayer player) {
        this.plugin = plugin;
        this.player = player;
    }

    public boolean login(String user, String pass) {
        if (!player.isRegistered()) {
            response = "login.error.registered";
            return false;
        } else if (player.isAuthenticated()) {
            response = "login.error.authenticated";
            return false;
        } else if (!plugin.getPlayerManager().isActive(player.getAccountId())) {
            response = "login.error.active";
            return false;
        } else if (!plugin.getPasswordHandler().checkPassword(player.getAccountId(), pass)) {
            int strikes = plugin.getStrikeManager().getRecord(player.getIPAddress()).addStrike(player.getName());
            if (strikes >= plugin.getConfig().getInt("strikes.amount"))
                plugin.getStrikeManager().strikeout(player.getPlayer());

            response = "login.error.password";
            return false;
        }

        return true;
    }

    public boolean register(String user, String pass, String email) {
        if (!plugin.getConfig().getBoolean("registration.enabled")) {
            response = "register.error.disabled";
            return false;
        } else if (player.isRegistered()) {
            response = "register.error.registered";
            return false;
        } else if (!isWithinAccLimit(player.getIPAddress())) {
            response = "register.error.limit";
            return false;
        } else if (!isValidPass(pass)) {
            response = "register.error.password";
            return false;
        } else if (!isValidEmail(email)) {
            response = "register.error.email";
            return false;
        }

        return execRegQuery(user, pass, email, false);
    }

    public boolean adminRegister(String user, String pass, String email) {
        if (player.isRegistered()) {
            response = "admin.register.error.registered";
            return false;
        }

        return execRegQuery(user, pass, email, true);
    }

    private boolean execRegQuery(String user, String pass, String email, boolean admin) {
        try {
            int accId = plugin.getPlayerManager().createAccount(user, pass, email, player.getIPAddress());
            if (accId > 0) {
                player.setAccountId(accId);
                player.setStatus(xAuthPlayer.Status.REGISTERED);
                response = admin ? "admin.register.success" : "register.success";
                return true;
            } else
                throw new SQLException();
        } catch (SQLException e) {
            xAuthLog.severe("Something went wrong while creating account for player: " + user, e);
            response = admin ? "admin.register.error.general" : "register.error.general";
            return false;
        }
    }

    public boolean changePassword(String user, String oldPass, String newPass) {
        if (!plugin.getConfig().getBoolean("password.allow-change")) {
            response = "changepw.error.disabled";
            return false;
        } else if (!player.isAuthenticated()) {
            response = "changepw.error.logged";
            return false;
        } else if (!plugin.getPasswordHandler().checkPassword(player.getAccountId(), oldPass)) {
            response = "changepw.error.incorrect";
            return false;
        } else if (!isValidPass(newPass)) {
            response = "changepw.error.invalid";
            return false;
        }

        return execCpwQuery(user, newPass, false, player.getPasswordType().getTypeId());
    }

    public boolean resetPassword(String user, String newPass) {
        if (!isValidPass(newPass)) {
            response = "changepw.error.invalid";
            return false;
        }

        int pwType = player.getPasswordType().getTypeId();

        return execRpwQuery(user, pwType, 0, "admin.resetpw.success.player") && execCpwQuery(user, newPass, false, pwType);
    }

    public boolean adminChangePassword(String user, String newPass, int pwType) {
        if (!player.isRegistered()) {
            response = "admin.changepw.error.registered";
            return false;
        }

        return execCpwQuery(user, newPass, true, pwType);
    }

    public boolean adminResetPassword(String user, int pwType) {
        if (!player.isRegistered()) {
            response = "admin.resetpw.error.registered";
            return false;
        }

        if (!player.isOnline()) {
            response = "admin.resetpw.error.not-online";
            return false;
        }

        return execRpwQuery(user, pwType, 1, "admin.resetpw.success.command");
    }

    public boolean unSetResetPw(String user) {
        return execRpwQuery(user, player.getPasswordType().getTypeId(), 0, null);
    }

    public boolean setResetPw(String user) {
        return execRpwQuery(user, player.getPasswordType().getTypeId(), 1, null);
    }

    public boolean execRpwQuery(String user, int pwType, int resetPw, String response) {
        Connection conn = plugin.getDatabaseController().getConnection();
        PreparedStatement ps = null;

        try {
            String sql = String.format("UPDATE `%s` SET `pwtype` = ?, `resetpw` = ? WHERE `id` = ?",
                    plugin.getDatabaseController().getTable(Table.ACCOUNT));
            ps = conn.prepareStatement(sql);
            ps.setInt(1, pwType);
            ps.setInt(2, resetPw);
            ps.setInt(3, player.getAccountId());
            ps.executeUpdate();
            player.setReset(false);
            this.response = response;
            return true;
        } catch (SQLException e) {
            xAuthLog.severe("Failed to reset password for player: " + user, e);
            this.response = "admin.resetpw.error.general";
            return false;
        } finally {
            plugin.getDatabaseController().close(conn, ps);
        }
    }

    public boolean execCpwQuery(String user, String newPass, boolean admin, int pwType) {
        Connection conn = plugin.getDatabaseController().getConnection();
        PreparedStatement ps = null;

        try {
            String sql = String.format("UPDATE `%s` SET `password` = ?, `pwtype`= ? WHERE `id` = ?",
                    plugin.getDatabaseController().getTable(Table.ACCOUNT));
            ps = conn.prepareStatement(sql);
            ps.setString(1, plugin.getPasswordHandler().hash(newPass, PasswordType.getType(pwType)));
            ps.setInt(2, pwType);
            ps.setInt(3, player.getAccountId());
            ps.executeUpdate();
            response = admin ? "admin.changepw.success" : "changepw.success";
            return true;
        } catch (SQLException e) {
            xAuthLog.severe("Failed to change password for player: " + user, e);
            response = admin ? "admin.changepw.error.general" : "changepw.error.general";
            return false;
        } finally {
            plugin.getDatabaseController().close(conn, ps);
        }
    }

    public boolean online(String user) {
        // nothing for AuthMethodSQL
        return true;
    }

    public boolean offline(String user) {
        // nothing for AuthMethodSQL
        return true;
    }

    private boolean isWithinAccLimit(String ipaddress) {
        int limit = plugin.getConfig().getInt("registration.account-limit");
        if (limit < 1 || xAuth.getPermissionManager().has(player.getPlayer(), "xauth.allow.player.account.bypass.limit"))
            return true;

        int count = 0;
        Connection conn = plugin.getDatabaseController().getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String sql = String.format("SELECT COUNT(`id`) FROM `%s` WHERE `registerip` = ?",
                    plugin.getDatabaseController().getTable(Table.ACCOUNT));
            ps = conn.prepareStatement(sql);
            ps.setString(1, ipaddress);
            rs = ps.executeQuery();
            if (rs.next())
                count = rs.getInt(1);
        } catch (SQLException e) {
            xAuthLog.severe("Could not check account count for ip: " + ipaddress, e);
        } finally {
            plugin.getDatabaseController().close(conn, ps, rs);
        }

        return limit > count;
    }

    private boolean isValidPass(String pass) {
        String pattern = "(";

        if (plugin.getConfig().getBoolean("password.complexity.lowercase"))
            pattern += "(?=.*[a-z])";

        if (plugin.getConfig().getBoolean("password.complexity.uppercase"))
            pattern += "(?=.*[A-Z])";

        if (plugin.getConfig().getBoolean("password.complexity.number"))
            pattern += "(?=.*\\d)";

        if (plugin.getConfig().getBoolean("password.complexity.symbol"))
            pattern += "(?=.*\\W)";

        pattern += ".{" + plugin.getConfig().getInt("password.min-length") + ",})";
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(pass);
        return matcher.matches();
    }

    private boolean isValidEmail(String email) {
        return !plugin.getConfig().getBoolean("registration.validate-email") || EmailValidatorFactory.EMAIL.isValid(email);
    }
}