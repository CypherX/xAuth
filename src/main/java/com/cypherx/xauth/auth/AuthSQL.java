package com.cypherx.xauth.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.avaje.ebean.validation.factory.EmailValidatorFactory;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.cypherx.xauth.xAuthPlayer;
import com.cypherx.xauth.xAuthPlayer.Status;

public class AuthSQL extends Auth {
	private final xAuthPlayer player;

	public AuthSQL(final xAuth plugin, final xAuthPlayer player) {
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
		} else if (!plugin.getPwdHndlr().checkPassword(player.getAccountId(), pass)) {
			int strikes = plugin.getStrkMngr().getRecord(player.getIPAddress()).addStrike(player.getPlayerName());
			if (strikes >= plugin.getConfig().getInt("strikes.amount"))
				plugin.getStrkMngr().strikeout(player.getPlayer());

			response = "login.error.password";
			return false;
		} else if (!isActive(player.getAccountId())) {
			response = "login.error.active";
			return false;
		}

		return true;

		/*Connection conn = plugin.getDbCtrl().getConnection();
		PreparedStatement ps = null;
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		String ipAddress = player.getIPAddress();

		try {
			String sql = String.format("UPDATE `%s` SET `lastlogindate` = ?, `lastloginip` = ? WHERE `id` = ?",
					plugin.getConfig().getString("mysql.tables.account"));
			ps = conn.prepareStatement(sql);
			ps.setTimestamp(1, currentTime);
			ps.setString(2, ipAddress);
			ps.setInt(3, player.getAccountId());
			ps.executeUpdate();

			// clear strikes
			plugin.getStrkMngr().getRecord(ipAddress).clearStrikes(player.getPlayerName());

			player.setLoginTime(currentTime);
			response = "login.success";
			return true;
		} catch (SQLException e) {
			xAuthLog.severe("Failed to complete log in process for player: " + user, e);
			response = "login.error.general";
			return false;
		} finally {
			plugin.getDbCtrl().close(conn, ps);
		}*/
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
			int accId = plugin.getPlyrMngr().createAccount(user, pass, email, player.getIPAddress());
			if (accId > 0) {
				player.setAccountId(accId);
				player.setStatus(Status.Registered);
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
		} else if (!plugin.getPwdHndlr().checkPassword(player.getAccountId(), oldPass)) {
			response = "changepw.error.incorrect";
			return false;
		} else if (!isValidPass(newPass)) {
			response = "changepw.error.invalid";
			return false;
		}

		return execCpwQuery(user, newPass, false);
	}

	public boolean adminChangePassword(String user, String newPass) {
		if (!player.isRegistered()) {
			response = "admin.changepw.registered";
			return false;
		}

		return execCpwQuery(user, newPass, true);
	}

	public boolean execCpwQuery(String user, String newPass, boolean admin) {
		Connection conn = plugin.getDbCtrl().getConnection();
		PreparedStatement ps = null;

		try {
			String sql = String.format("UPDATE `%s` SET `password` = ? WHERE `id` = ?",
					plugin.getConfig().getString("mysql.tables.account"));
			ps = conn.prepareStatement(sql);
			ps.setString(1, plugin.getPwdHndlr().hash(newPass));
			ps.setInt(2, player.getAccountId());
			ps.executeUpdate();
			response = admin ? "admin.changepw.success" : "changepw.success";
			return true;
		} catch (SQLException e) {
			xAuthLog.severe("Failed to change password for player: " + user, e);
			response = admin ? "admin.changepw.error.general" : "changepw.error.general";
			return false;
		} finally {
			plugin.getDbCtrl().close(conn, ps);
		}
	}

	public boolean online(String user) {
		// nothing for AuthSQL
		return true;
	}

	public boolean offline(String user) {
		// nothing for AuthSQL
		return true;
	}

	private boolean isActive(int id) {
		if (!plugin.getConfig().getBoolean("registration.activation"))
			return true;

		Connection conn = plugin.getDbCtrl().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = String.format("SELECT `active` FROM `%s` WHERE `id` = ?",
					plugin.getConfig().getString("mysql.tables.account"));
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (!rs.next())
				return false;

			return rs.getBoolean("active");
		} catch (SQLException e) {
			xAuthLog.severe("Failed to check active status of account: " + id, e);
			return false;
		} finally {
			plugin.getDbCtrl().close(conn, ps, rs);
		}
	}

	private boolean isWithinAccLimit(String ipaddress) {
		int limit = plugin.getConfig().getInt("registration.account-limit");
		if (limit < 1)
			return true;

		int count = 0;
		Connection conn = plugin.getDbCtrl().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = String.format("SELECT COUNT(`id`) FROM `%s` WHERE `registerip` = ?",
					plugin.getConfig().getString("mysql.tables.account"));
			ps = conn.prepareStatement(sql);
			ps.setString(1, ipaddress);
			rs = ps.executeQuery();
			if (rs.next())
				count = rs.getInt(1);
		} catch (SQLException e) {
			xAuthLog.severe("Could not check account count for ip: " + ipaddress, e);
		} finally {
			plugin.getDbCtrl().close(conn, ps, rs);
		}

		return limit >= count;
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
		if (!plugin.getConfig().getBoolean("registration.validate-email"))
			return true;

		return EmailValidatorFactory.EMAIL.isValid(email);
	}
}