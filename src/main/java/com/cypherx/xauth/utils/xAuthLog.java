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
package com.cypherx.xauth.utils;

import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.Logger;

public class xAuthLog {
    private static final Logger logger = Logger.getLogger(Bukkit.getServer().getLogger().getName() + ".xAuth");
    private static Level logLevel;
    private static Level defaultLevel = Level.INFO;
    private static String loggerName = "xAuth";

    public static void initLogger() {
        setLevel(defaultLevel);
    }

    public static String getLoggerName() {
        return loggerName;
    }

    public static void reset() {
        setLevel(defaultLevel);
    }

    public static void setLevel(Level level) {
        logLevel = level;
        logger.setLevel(logLevel);
    }

    public static Level getLevel() {
        return logLevel;
    }

    public static void info(String msg) {
        logger.log(Level.INFO, "[" + getLoggerName() + "] " + Utils.replaceColors(msg));
    }

    public static void fine(String msg) {
        logger.log(Level.FINE, "[" + getLoggerName() + "] " + Utils.replaceColors(msg));
    }

    public static void finer(String msg) {
        logger.log(Level.FINER, "[" + getLoggerName() + "] " + Utils.replaceColors(msg));
    }

    public static void finest(String msg) {
        logger.log(Level.FINEST, "[" + getLoggerName() + "] " + Utils.replaceColors(msg));
    }

    public static void warning(String msg) {
        logger.log(Level.WARNING, "[" + getLoggerName() + "] " + Utils.replaceColors(msg));
    }

    public static void severe(String msg) {
        logger.log(Level.SEVERE, "[" + getLoggerName() + "] " + Utils.replaceColors(msg));
    }

    public static void info(String msg, Throwable e) {
        logger.log(Level.INFO, "[" + getLoggerName() + "] " + Utils.replaceColors(msg), e);
    }

    public static void warning(String msg, Throwable e) {
        logger.log(Level.WARNING, "[" + getLoggerName() + "] " + Utils.replaceColors(msg), e);
    }

    public static void severe(String msg, Throwable e) {
        logger.log(Level.SEVERE, "[" + getLoggerName() + "] " + Utils.replaceColors(msg), e);
    }
}