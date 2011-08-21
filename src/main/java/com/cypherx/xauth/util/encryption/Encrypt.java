package com.cypherx.xauth.util.encryption;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class Encrypt {
	public static String custom(String toEncrypt) {
		String salt = whirlpool(UUID.randomUUID().toString()).substring(0, 12);
		String hash = whirlpool(salt + toEncrypt);
		int saltPos = (toEncrypt.length() >= hash.length() ? hash.length() - 1 : toEncrypt.length());
		return hash.substring(0, saltPos) + salt + hash.substring(saltPos);
	}

	public static String whirlpool(String toEncrypt) {
		Whirlpool w = new Whirlpool();
		byte[] digest = new byte[Whirlpool.DIGESTBYTES];
		w.NESSIEinit();
		w.NESSIEadd(toEncrypt);
		w.NESSIEfinalize(digest);
		return Whirlpool.display(digest);
	}

	public static String md5(String toEncrypt) {
		String hash = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(toEncrypt.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			hash = number.toString(16);
			while (hash.length() < 32)
				hash = "0" + hash;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return hash;
	}
}