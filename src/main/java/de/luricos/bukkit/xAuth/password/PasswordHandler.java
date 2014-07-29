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
package de.luricos.bukkit.xAuth.password;

import de.luricos.bukkit.xAuth.database.Table;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PasswordHandler {

    private final xAuth plugin;
    private boolean convertPassword = false;

    public PasswordHandler(final xAuth plugin) {
        this.plugin = plugin;
    }

    public boolean checkPassword(int accountId, String checkPass) {
        String realPass = "";
        PasswordType type = PasswordType.DEFAULT;

        Connection conn = plugin.getDatabaseController().getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String sql = String.format("SELECT `password`, `pwtype` FROM `%s` WHERE `id` = ?",
                    plugin.getDatabaseController().getTable(Table.ACCOUNT));
            ps = conn.prepareStatement(sql);
            ps.setInt(1, accountId);
            rs = ps.executeQuery();
            if (!rs.next())
                return false;

            realPass = rs.getString("password");
            type = PasswordType.getType(rs.getInt("pwtype"));
        } catch (SQLException e) {
            xAuthLog.severe("Failed to retrieve password hash for account: " + accountId, e);
            return false;
        } finally {
            plugin.getDatabaseController().close(conn, ps, rs);
        }

        String checkPassHash = "";
        String salt = "";
        switch (type) {
            case DEFAULT:
                int saltPos = (checkPass.length() >= realPass.length() ? realPass.length() - 1 : checkPass.length());
                salt = realPass.substring(saltPos, saltPos + 12);
                String hash = whirlpool(salt + checkPass);
                checkPassHash = hash.substring(0, saltPos) + salt + hash.substring(saltPos);
                break;
            case WHIRLPOOL:
                checkPassHash = whirlpool(checkPass);
                break;
            case AUTHME_SHA256:
                checkPassHash = authme_sha256(checkPass);
                break;
            default:
                checkPassHash = hash(checkPass, type.getAlgorithm());
                break;
        }

        // update hash in database to use xAuth's hashing method if configured
        if (xAuth.getPlugin().getConfig().getBoolean("password.convert-password", this.convertPassword)) {
            String newHash = hash(checkPass);
            conn = plugin.getDatabaseController().getConnection();

            try {
                String sql = String.format("UPDATE `%s` SET `password` = ?, `pwtype` = ? WHERE `id` = ?",
                        plugin.getDatabaseController().getTable(Table.ACCOUNT));
                ps = conn.prepareStatement(sql);
                ps.setString(1, newHash);
                ps.setInt(2, 0);
                ps.setInt(3, accountId);
                ps.executeUpdate();
            } catch (SQLException e) {
                xAuthLog.severe("Failed to update password hash for account: " + accountId, e);
            } finally {
                plugin.getDatabaseController().close(conn, ps);
            }
        }

        return checkPassHash.equals(realPass);
    }

    // xAuth's custom hashing technique
    public String hash(String toHash) {
        String salt = whirlpool(UUID.randomUUID().toString()).substring(0, 12);
        String hash = whirlpool(salt + toHash);
        int saltPos = (toHash.length() >= hash.length() ? hash.length() - 1 : toHash.length());
        return hash.substring(0, saltPos) + salt + hash.substring(saltPos);
    }

    public String hash(String toHash, PasswordType pwType) {
        String result;
        switch (pwType) {
            case DEFAULT:
                result = hash(toHash);
                break;
            case WHIRLPOOL:
                result = whirlpool(toHash);
                break;
            case AUTHME_SHA256:
                result = authme_sha256(toHash);
                break;
            default:
                result = hash(toHash, pwType.getAlgorithm());
                break;
        }

        return result;
    }

    private String hash(String toHash, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(toHash.getBytes());
            byte[] digest = md.digest();
            return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String whirlpool(String toHash) {
        Whirlpool w = new Whirlpool();
        byte[] digest = new byte[Whirlpool.DIGESTBYTES];
        w.NESSIEinit();
        w.NESSIEadd(toHash);
        w.NESSIEfinalize(digest);
        return Whirlpool.display(digest);
    }

    private String authme_sha256(String toHash) {
        String salt = toHash.split("\\$")[2];
        return "$SHA$" + salt + "$" + hash(hash(toHash, "SHA-256") + salt, "SHA-256");
    }
}