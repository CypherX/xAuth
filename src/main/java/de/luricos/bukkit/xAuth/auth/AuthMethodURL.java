/*
 * xAuth for Bukkit
 * Copyright (C) 2012 Lycano <https://github.com/lycano/xAuth/>
 *
 * Copyright (C) 2011 CypherX <https://github.com/CypherX/xAuth/>
 * Copyright (C) 2011 Module AuthURL: moparisthebest <http://www.moparscape.org/smf/>
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

import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


/**
 * This class is made to interact with authURL scripts on servers.  It is backwards compatible with the original
 * version that just supported login authentication, but now has more features.
 * <p/>
 * A new instance of this class is meant to be created for each transaction.
 * <p/>
 * If you wish to implement this functionality into your program in addition to other methods,
 * I encourage you to implement the included 'AuthMethod' interface and then use a single helper method to return an
 * appropriate instance of AuthMethod for you to use.
 * author: moparisthebest
 */
public class AuthMethodURL extends AuthMethod {

    // increment this any time something is changed that requires a change in the protocol script-side
    private static final int version = 1;

    private String ipAddress = null;

    public AuthMethodURL(final xAuth plugin) {
        this(plugin, null);
    }

    public AuthMethodURL(final xAuth plugin, final String ipAddress) {
        this.plugin = plugin;
        this.ipAddress = ipAddress;
    }

    /**
     * Attempts to authenticate a user with a given password.  Sets response and group appropriately.
     * Also sets status to online if supported, so no need to call online() if this is successful.
     *
     * @param user Username
     * @param pass Password
     * @return success
     */
    public boolean login(String user, String pass) {
        return checkAuthURL("login", "user", user, "pass", pass);
    }

    /**
     * Attempts to register a username with the given password and email.  Sets response appropriately.
     *
     * @param user  Username
     * @param pass  Password
     * @param email Valid email address, can be null.
     * @return success
     */
    public boolean register(String user, String pass, String email) {
        if (!plugin.getConfig().getBoolean("authurl.registration")) {
            response = "authurl.registration";
            return false;
        }
        return checkAuthURL("register", "user", user, "pass", pass, "email", email);
    }

    public boolean adminRegister(String user, String pass, String email) {
        return false;
    }

    public boolean changePassword(String user, String oldPass, String newPass) {
        response = "authurl.changepw";
        return false;
    }

    public boolean resetPassword(String user, String newPass) {
        response = "authurl.resetpw";
        return false;
    }

    public boolean adminChangePassword(String user, String newPass, int pwType) {
        response = "authurl.changepw";
        return false;
    }

    public boolean adminResetPassword(String user, int pwType) {
        response = "authurl.resetpw";
        return false;
    }

    public boolean setResetPw(String user) {
        response = "authurl.resetpw";
        return false;
    }

    public boolean unSetResetPw(String user) {
        response = "authurl.resetpw";
        return false;
    }

    /**
     * Sets status of user to online if supported.  This is for use when a user doesn't need to supply
     * a password due to a session that has not yet timed out.  Sets response appropriately.
     *
     * @param user Username
     * @return success May be safely ignored
     */
    public boolean online(String user) {
        if (!plugin.getConfig().getBoolean("authurl.status")) {
            //response = "AuthMethodURL online is disabled.";
            return false;
        }
        return checkAuthURL("online", "user", user);
    }

    /**
     * Sets status of user to offline if supported, to be called when a user logs out or is disconnected.
     * No response is set and none need be displayed.
     *
     * @param user Username
     * @return success May be safely ignored
     */
    public boolean offline(String user) {
        if (!plugin.getConfig().getBoolean("authurl.status")) {
            //response = "AuthMethodURL offline is disabled.";
            return false;
        }
        return checkAuthURL("offline", "user", user);
    }

    private void writeParam(DataOutputStream out, String name, String value) throws Exception {
        if (value == null)
            return;
        String param = "&" + name + "=" + URLEncoder.encode(value, "UTF-8");
        out.writeBytes(param);
    }

    private boolean checkAuthURL(String action, String... params) {
        // if there isn't at least one parameter OR
        // if params length is not an even number
        if (params.length < 2 || ((params.length % 2) != 0))
            return false;
        try {
            //HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection uc = (HttpURLConnection) new URL(plugin.getConfig().getString("authurl.url")).openConnection();

            uc.setRequestMethod("POST");
            uc.setDoInput(true);
            uc.setDoOutput(true);
            uc.setUseCaches(false);
            uc.setAllowUserInteraction(false);
            uc.setInstanceFollowRedirects(false);
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 authURL/" + version);
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            DataOutputStream out = new DataOutputStream(uc.getOutputStream());
            // first lets write the authurl_version we are working with
            out.writeBytes("authurl_version=" + version);
            // now write the IP if it is set
            writeParam(out, "ip", this.ipAddress);
            writeParam(out, "action", action);
            for (int x = 0; x < params.length; ++x)
                writeParam(out, params[x], params[++x]);
            out.flush();
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String line = in.readLine();
            boolean success = line != null && line.equals("YES");
            response = in.readLine();
            if (plugin.getConfig().getBoolean("authurl.groups"))
                group = in.readLine();
            in.close();
            return success;
        } catch (Exception e) {
            //response = e.getMessage();
            xAuthLog.severe("Failed to process AuthMethodURL script during action: " + action, e);
            return false;
        }
    }
}
