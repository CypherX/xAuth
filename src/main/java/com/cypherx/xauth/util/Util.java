package com.cypherx.xauth.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.cypherx.xauth.Account;
import com.cypherx.xauth.Session;
import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.xAuthLog;
import com.martiansoftware.jsap.CommandLineTokenizer;

public class Util {
	public static void writeConfig(File file, Class<?> c) {
		String fileName = file.getName();
		String content = getResourceAsString("/res/" + fileName);

		for (Field field : c.getFields()) {
			try {
				content = content.replace("[" + field.getName() + "]", field.get(null).toString());
			} catch (IllegalAccessException e) {}
		}

		Writer out = null;

		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
			out.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {}
		}
	}

	public static String getResourceAsString(String resource) {
		InputStream input = xAuth.class.getResourceAsStream(resource);
		StringBuilder sb = new StringBuilder();

		if (input != null) {
			InputStreamReader isr = new InputStreamReader(input);
			BufferedReader br = new BufferedReader(isr);
			String newLine = System.getProperty("line.separator");
			String line;

			try {
				while ((line = br.readLine()) != null)
					sb.append(line).append(newLine);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (isr != null)
						isr.close();
				} catch (IOException e) {}
				try {
					if (br != null)
						br.close();
				} catch (IOException e) {}
			}
		}

		return sb.toString();
	}

	public static String getHostFromPlayer(Player player) {
		if (player == null)
			return null;

		return player.getAddress().getAddress().getHostAddress();
	}

	public static Timestamp getNow() {
		return new Timestamp(System.currentTimeMillis());
	}

	public static int[] stringToInt(String[] strArray) {
		int[] intArray = new int[strArray.length];

		for (int i = 0; i < strArray.length; i++)
			intArray[i] = Integer.parseInt(strArray[i]);

		return intArray;
	}

	public static Account buildAccount(ResultSet rs) {
		Account account = null;
		try {
			account = new Account();
			account.setId(rs.getInt("id"));
			account.setPlayerName(rs.getString("playername"));
			account.setPassword(rs.getString("password"));
			account.setEmail(rs.getString("email"));
			account.setRegisterDate(rs.getTimestamp("registerdate"));
			account.setRegisterHost(rs.getString("registerip"));
			account.setLastLoginDate(rs.getTimestamp("lastlogindate"));
			account.setLastLoginHost(rs.getString("lastloginip"));
			account.setActive(rs.getInt("active"));
		} catch (SQLException e) {
			xAuthLog.severe("Could not build Account from ResultSet!", e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {}
		}

		return account;
	}

	public static Session buildSession(ResultSet rs) {
		Session session = null;
		try {
			session = new Session();
			session.setAccountId(rs.getInt("accountid"));

			if (rs.wasNull()) // no session data in database
				return null;

			session.setHost(rs.getString("host"));
			session.setLoginTime(rs.getTimestamp("logintime"));
		} catch (SQLException e) {
			xAuthLog.severe("Could not build Session from ResultSet!", e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {}
		}

		return session;
	}

	public static boolean isUUID(String str) {
		try {
			UUID.fromString(str);
		} catch (IllegalArgumentException e) {
			return false;
		}

		return true;
	}

	public static String argsToString(String[] args) {
		if (args.length < 1)
			return null;

		StringBuilder sb = new StringBuilder(args[0]);
		for (int i = 1; i < args.length; i++)
			sb.append(" " + args[i]);

		return sb.toString();
	}

	// for lack of a better name..
	public static String[] fixArgs(String[] args) {
		return CommandLineTokenizer.tokenize(argsToString(args));
	}
}