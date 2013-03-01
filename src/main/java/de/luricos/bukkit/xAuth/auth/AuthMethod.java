/*
 * xAuth for Bukkit
 * Copyright (C) 2012 Lycano <https://github.com/lycano/xAuth/>
 *
 * Copyright (C) 2011 CypherX <https://github.com/CypherX/xAuth/>
 * Copyright (C) 2011 Module Auth: moparisthebest <http://www.moparscape.org/smf/>
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

import de.luricos.bukkit.xAuth.xAuth;

public abstract class AuthMethod {
    protected xAuth plugin;
    protected String response = null;
    protected String group = null;

    /**
     * Returns human-readable response suitable for printing to users, or null if nothing should be printed.
     *
     * @return String
     */
    public String getResponse() {
        return response;
    }

    /**
     * Returns permissions group user should be a member of, or null if no group should be added.
     *
     * @return String
     */
    public String getGroup() {
        return group;
    }

    /**
     * Attempts to authenticate a user with a given password.  Sets response and group appropriately.
     * Also sets status to online if supported, so no need to call online() if this is successful.
     *
     * @param user Username
     * @param pass Password
     * @return success
     */
    public abstract boolean login(String user, String pass);

    /**
     * Attempts to register a username with the given password and email.  Sets response appropriately.
     *
     * @param user  Username
     * @param pass  Password
     * @param email Valid email address, can be null.
     * @return success
     */
    public abstract boolean register(String user, String pass, String email);

    /**
     * Attempts to register an account for the user specified.
     * This is reserved for the /xauth register command and skips most checks.
     *
     * @param user
     * @param pass
     * @param email
     * @return success
     */
    public abstract boolean adminRegister(String user, String pass, String email);

    /**
     * Attempts to change a user's password.
     *
     * @param user    Username
     * @param oldPass Old password
     * @param newPass New password
     * @return success
     */
    public abstract boolean changePassword(String user, String oldPass, String newPass);

    /**
     * Attempts to reset a user's password.
     *
     * @param user    String Username
     * @param newPass String New password
     * @return success
     */
    public abstract boolean resetPassword(String user, String newPass);

    /**
     * Attempts to change the specified user's password.
     * This is reserved for the /xauth changepw command skips most checks.
     *
     * @param user
     * @param newPass
     * @return success
     */
    public abstract boolean adminChangePassword(String user, String newPass, int pwType);

    /**
     * Attempts to reset the specified user's password.
     * This is reserved for the /xauth resetpw command skips most checks.
     *
     * @param user String
     * @param pwType int (See PasswordType for further details)
     * @return success
     */
    public abstract boolean adminResetPassword(String user, int pwType);

    /**
     * Sets resetMode of user to 0 if supported.
     *
     * @param user Username
     * @return success May be safely ignored
     */
    public abstract boolean unSetResetPw(String user);

    /**
     * Sets resetMode of user to 1 if supported.
     *
     * @param user Username
     * @return success May be safely ignored
     */
    public abstract boolean setResetPw(String user);

    /**
     * Sets status of user to online if supported.  This is for use when a user doesn't need to supply
     * a password due to a session that has not yet timed out.  Sets response appropriately.
     *
     * @param user Username
     * @return success May be safely ignored
     */
    public abstract boolean online(String user);

    /**
     * Sets status of user to offline if supported, to be called when a user logs out or is disconnected.
     * No response is set and none need be displayed.
     *
     * @param user Username
     * @return success May be safely ignored
     */
    public abstract boolean offline(String user);

}
