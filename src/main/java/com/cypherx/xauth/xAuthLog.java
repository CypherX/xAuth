package com.cypherx.xauth;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

public class xAuthLog {
	private static final Logger logger = Bukkit.getLogger();

	public static void info(String msg) {
		logger.log(Level.INFO, "[xAuth] " + msg);
	}

	public static void warning(String msg) {
		logger.log(Level.WARNING, "[xAuth] " + msg);
	}

	public static void severe(String msg) {
		logger.log(Level.SEVERE, "[xAuth] " + msg);
	}

	public static void info(String msg, Throwable e) {
		logger.log(Level.INFO, "[xAuth] " + msg, e);
	}

	public static void warning(String msg, Throwable e) {
		logger.log(Level.WARNING, "[xAuth] " + msg, e);
	}

	public static void severe(String msg, Throwable e) {
		logger.log(Level.SEVERE, "[xAuth] " + msg, e);
	}
}