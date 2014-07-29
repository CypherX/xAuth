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
package de.luricos.bukkit.xAuth.utils;

import de.luricos.bukkit.xAuth.filter.xAuthLogFilter;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class xAuthLog {
    private static final Logger logger = Bukkit.getServer().getLogger();
    private static Level logLevel;
    private static Level defaultLevel = Level.INFO;

    private static List<xAuthLogFeatures> logFeatures = new ArrayList<xAuthLogFeatures>();
    private static List<String> commandsFilterList = new ArrayList<String>();

    private static Filter logFilter;

    public enum xAuthLogFeatures {
        NONE,
        FILTER_COMMANDS
    }

    private static String loggerName = "xAuth";

    public static void initLogger() {
        setLevel(defaultLevel);
        enableFeature(xAuthLogFeatures.NONE);
    }

    /**
     * Check if a certain feature is enabled
     *
     * @param feature xAuthLogFeatures NONE, FILTER_COMMANDS
     * @return true if enabled
     */
    public static boolean isFeatureEnabled(final xAuthLogFeatures feature) {
        return logFeatures.contains(feature);
    }

    /**
     * Enable Log Feature
     * @param feature xAuthLogFeatures NONE, FILTER_COMMANDS
     */
    public static void enableFeature(final xAuthLogFeatures feature) {
        //info("Enable log feature: " + feature.toString());

        setFeature(feature);

        switch (feature) {
            case FILTER_COMMANDS:
                setFilterCommands();
                break;
        }
    }

    /**
     * Disable Log Feature
     * @param feature xAuthLogFeatures NONE, FILTER_COMMANDS
     */
    public static void disableFeature(final xAuthLogFeatures feature) {
        logFeatures.remove(feature);
    }

    /**
     * Disables all features
     */
    public static void disableFeatures() {
        logFeatures = new ArrayList<xAuthLogFeatures>();
        logFeatures.add(xAuthLogFeatures.NONE);
        restoreFilter();
    }

    /**
     * Set a feature when not already enabled
     * @param feature xAuthLogFeatures NONE, FILTER_COMMANDS
     */
    private static void setFeature(final xAuthLogFeatures feature) {
        if (isFeatureEnabled(feature))
            return;

        if (isFeatureEnabled(xAuthLogFeatures.NONE))
            disableFeature(xAuthLogFeatures.NONE);

        logFeatures.add(feature);
    }

    private static void setFilterCommands() {
        Map<String, Map<String, Object>> commandsMap = xAuth.getPlugin().getDescription().getCommands();

        commandsFilterList.addAll(commandsMap.keySet());
        for (String commandName: commandsMap.keySet()) {
            commandsFilterList.addAll(xAuth.getPlugin().getCommand(commandName).getAliases());
        }
    }

    public static List<xAuthLogFeatures> getFeatures() {
        return logFeatures;
    }

    private static void setFilterClass(final Filter cf) {
        logFilter = cf;
    }

    public static void restoreFilter() {
        info("Restoring xAuth default filter (NONE).");
        if (logger.getFilter() instanceof xAuthLogFilter)
            setFilterClass(((xAuthLogFilter) logger.getFilter()).getDelegate());

        logger.setFilter(logFilter);
    }

    public static void filterMessage(final String message) {
        if (!(isFeatureEnabled(xAuthLogFeatures.FILTER_COMMANDS)))
            return;

        // filter out implemented xAuth commands in server.log due to a new MC-1.3.2 feature
        for (String command: commandsFilterList) {
            if (!message.toLowerCase().startsWith(command, 1))
                continue;

            setFilterClass(new xAuthLogFilter(logger.getFilter(), message));
            break;
        }

        // set filter each message filtering when enabled via config
        logger.setFilter(logFilter);
    }

    public static String getLoggerName() {
        return loggerName;
    }

    public static void reset() {
        setLevel(defaultLevel);
    }

    public static void setLevel(final Level level) {
        logLevel = level;
        logger.setLevel(logLevel);
    }

    public static Level getLevel() {
        return logLevel;
    }

    public static void info(final String msg) {
        logger.log(Level.INFO, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void fine(final String msg) {
        logger.log(Level.FINE, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void finer(final String msg) {
        logger.log(Level.FINER, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void finest(final String msg) {
        logger.log(Level.FINEST, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void warning(final String msg) {
        logger.log(Level.WARNING, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void severe(final String msg) {
        logger.log(Level.SEVERE, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void info(final String msg, final Throwable e) {
        logger.log(Level.INFO, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg), e);
    }

    public static void warning(final String msg, final Throwable e) {
        logger.log(Level.WARNING, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg), e);
    }

    public static void severe(final String msg, final Throwable e) {
        logger.log(Level.SEVERE, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg), e);
    }
}