package com.cypherx.xauth.password;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;

public class PasswordHandler {
	private final xAuth plugin;

	public PasswordHandler(final xAuth plugin) {
		this.plugin = plugin;
	}

	public boolean checkPassword(int accountId, String checkPass) {
		String realPass = "";
		PasswordType type = PasswordType.DEFAULT;

		Connection conn = plugin.getDbCtrl().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = String.format("SELECT `password`, `pwtype` FROM `%s` WHERE `id` = ?",
					plugin.getConfig().getString("mysql.tables.account"));
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
			plugin.getDbCtrl().close(conn, ps, rs);
		}

		String checkPassHash = "";
		if (type == PasswordType.DEFAULT) {
			int saltPos = (checkPass.length() >= realPass.length() ? realPass.length() - 1 : checkPass.length());
			String salt = realPass.substring(saltPos, saltPos + 12);
			String hash = whirlpool(salt + checkPass);
			checkPassHash = hash.substring(0, saltPos) + salt + hash.substring(saltPos);
		} else
			checkPassHash = hash(checkPass, type.getAlgorithm());

		if (checkPassHash.equals(realPass)) {
			// update hash in database to use xAuth's hashing method
			String newHash = hash(checkPass);
			conn = plugin.getDbCtrl().getConnection();

			try {
				String sql = String.format("UPDATE `%s` SET `password` = ?, `pwtype` = ? WHERE `id` = ?",
						plugin.getConfig().getString("mysql.tables.account"));
				ps = conn.prepareStatement(sql);
				ps.setString(1, newHash);
				ps.setInt(2, 0);
				ps.setInt(3, accountId);
				ps.executeUpdate();
			} catch (SQLException e) {
				xAuthLog.severe("Failed to update password hash for account: " + accountId, e);
			} finally {
				plugin.getDbCtrl().close(conn, ps);
			}

			return true;
		} else
			return false;
	}

	// xAuth's custom hashing technique
	public String hash(String toHash) {
		String salt = whirlpool(UUID.randomUUID().toString()).substring(0, 12);
		String hash = whirlpool(salt + toHash);
		int saltPos = (toHash.length() >= hash.length() ? hash.length() - 1 : toHash.length());
		return hash.substring(0, saltPos) + salt + hash.substring(saltPos);
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
}