package com.cypherx.xauth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import com.avaje.ebean.validation.factory.EmailValidatorFactory;

/*
 * Miscellaneous methods
 */

public class Util {
	public static void writeConfig(File file, Class<?> c) {
		String fileName = file.getName();
		String content = getResourceAsString(fileName);

		for (Field field : c.getFields()) {
			try {
				content = content.replace("[" + field.getName() + "]", field.get(null).toString());
			} catch (IllegalAccessException e) {}
		}

		BufferedWriter out = null;

		try {
			out = new BufferedWriter(new FileWriter(file));
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

	private static String getResourceAsString(String resource) {
		InputStream input = xAuth.class.getResourceAsStream("/res/" + resource);
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

	public static boolean getOnlineMode() {
		BufferedReader br = null;
		String value = null;
		String line;

		try {
			br = new BufferedReader(new FileReader("server.properties"));
			while ((line = br.readLine()).indexOf("online-mode") == -1);
			value = line.split("=")[1];
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {}
		}

		return Boolean.parseBoolean(value);
	}

	public static boolean isValidName(Player player) {
		String playerName = player.getName().toLowerCase();
		if (playerName.length() < xAuthSettings.filterMinLength)
			return false;

		String allowed = xAuthSettings.filterAllowed;
		if (!allowed.equals("*")) {
			for(int i = 0; i < playerName.length(); i++) {
				if (allowed.indexOf(playerName.charAt(i)) == -1)
					return false;
			}
		}

		if (xAuthSettings.filterBlank && playerName.trim().equals(""))
			return false;

		return true;
	}

	public static boolean isValidPass(String pass) {
		String pattern = "(";

		if (xAuthSettings.pwCompLower)
			pattern += "(?=.*[a-z])";

		if (xAuthSettings.pwCompUpper)
			pattern += "(?=.*[A-Z])";

		if (xAuthSettings.pwCompNumber)
			pattern += "(?=.*\\d)";

		if (xAuthSettings.pwCompSymbol)
			pattern += "(?=.*\\W)";

		pattern += ".{" + xAuthSettings.pwMinLength + ",})";
		Pattern p = Pattern.compile(pattern);
		Matcher matcher = p.matcher(pass);
		return matcher.matches();
	}

	public static boolean isValidEmail(String email) {
		return EmailValidatorFactory.EMAIL.isValid(email);
	}

	public static String encrypt(String pass) {
		Whirlpool w = new Whirlpool();
		byte[] digest = new byte[Whirlpool.DIGESTBYTES];

		w.NESSIEinit();
		w.NESSIEadd(UUID.randomUUID().toString());
		w.NESSIEfinalize(digest);
		String salt = Whirlpool.display(digest).substring(0, 12);

		w.NESSIEinit();
		w.NESSIEadd(salt + pass);
		w.NESSIEfinalize(digest);
		String hash = Whirlpool.display(digest);

		int saltPos = (pass.length() >= hash.length() ? hash.length() : pass.length());
		return hash.substring(0, saltPos) + salt + hash.substring(saltPos);
	}

	public static String whirlpool(String pass) {
		Whirlpool w = new Whirlpool();
		byte[] digest = new byte[Whirlpool.DIGESTBYTES];
		w.NESSIEinit();
		w.NESSIEadd(pass);
		w.NESSIEfinalize(digest);
		return Whirlpool.display(digest);
	}

	public static String md5(String pass) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(pass.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String hashtext = number.toString(16);
			while (hashtext.length() < 32)
				hashtext = "0" + hashtext;

			return hashtext;
		} catch (Exception e) {
			xAuthLog.severe("Could not create MD5 hash!", e);
		}

		return null;
	}

	public static String getHostFromPlayer(Player player) {
		if (player == null)
			return null;

		return player.getAddress().getAddress().getHostAddress();
	}

	public static Timestamp getNow() {
		return new Timestamp(System.currentTimeMillis());
	}
}