package com.cypherx.xauth.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import com.avaje.ebean.validation.factory.EmailValidatorFactory;
import com.cypherx.xauth.xAuthSettings;

public class Validator {
	public static boolean isValidName(Player player) {
		String playerName = player.getName();
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
}