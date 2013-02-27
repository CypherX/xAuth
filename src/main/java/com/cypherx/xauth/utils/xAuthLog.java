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

import com.cypherx.xauth.filter.CommandFilter;
import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.Logger;

public class xAuthLog {
    private static final Logger logger = Logger.getLogger(Bukkit.getServer().getLogger().getName() + ".xAuth");
    private static Level logLevel;
    private static Level defaultLevel = Level.INFO;
    private static String loggerName = "xAuth";
    private static CommandFilter commandFilter;

    public static void initLogger() {
        setLevel(defaultLevel);
    }

    public static void setFilterClass(CommandFilter cf) {
        commandFilter = cf;
    }

    public static void filterMessage(String message) {
        setFilterClass(new CommandFilter(message));

        // set filter each message filtering
        Logger.getLogger("Minecraft").setFilter(commandFilter);
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
        logger.log(Level.INFO, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void fine(String msg) {
        logger.log(Level.FINE, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void finer(String msg) {
        logger.log(Level.FINER, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void finest(String msg) {
        logger.log(Level.FINEST, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void warning(String msg) {
        logger.log(Level.WARNING, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void severe(String msg) {
        logger.log(Level.SEVERE, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void info(String msg, Throwable e) {
        logger.log(Level.INFO, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg), e);
    }

    public static void warning(String msg, Throwable e) {
        logger.log(Level.WARNING, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg), e);
    }

    public static void severe(String msg, Throwable e) {
        logger.log(Level.SEVERE, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg), e);
    }
}