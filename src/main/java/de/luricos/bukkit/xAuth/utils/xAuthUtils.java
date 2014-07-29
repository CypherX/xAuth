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

import org.bukkit.ChatColor;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class xAuthUtils {

    public static String streamToString(InputStream in) {
        if (in == null)
            return "";


        Writer writer = new StringWriter();

        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1)
                writer.write(buffer, 0, n);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }
        return writer.toString();
    }

    public static boolean isIPAddress(String ipAddress) {
        if (ipAddress == null)
            return false;

        Pattern pattern = Pattern.compile(
                "\\b" +
                        "((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
                        "((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
                        "((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
                        "((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])" +
                        "\\b");
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    public static boolean isWhitespace(String str) {
        if (str == null)
            return false;

        for (int i = 0; i < str.length(); i++)
            if (!Character.isWhitespace(str.charAt(i)))
                return false;

        return true;
    }

    public static String replaceColors(String s) {
        s = s.replace("{BLACK}", "&0");
        s = s.replace("{DARKBLUE}", "&1");
        s = s.replace("{DARKGREEN}", "&2");
        s = s.replace("{DARKTEAL}", "&3");
        s = s.replace("{DARKRED}", "&4");
        s = s.replace("{PURPLE}", "&5");
        s = s.replace("{GOLD}", "&6");
        s = s.replace("{GRAY}", "&7");
        s = s.replace("{DARKGRAY}", "&8");
        s = s.replace("{BLUE}", "&9");
        s = s.replace("{BRIGHTGREEN}", "&a");
        s = s.replace("{TEAL}", "&b");
        s = s.replace("{RED}", "&c");
        s = s.replace("{PINK}", "&d");
        s = s.replace("{YELLOW}", "&e");
        s = s.replace("{WHITE}", "&f");

        return switchToColorChar(s);
    }

    public static String switchToColorChar(String s) {
        return s.replace('&', ChatColor.COLOR_CHAR);
    }

    public static String switchToAlternateColorChar(String s) {
        return s.replace(ChatColor.COLOR_CHAR, '&');
    }

    public static String join(Object[] collection) {
        return join(collection, ",");
    }

    public static String join(Object[] collection, String with) {
        StringBuilder sb = new StringBuilder("");
        String delim = "";
        for (Object el: collection) {
            sb.append(delim).append(el.toString());
            delim = with;
        }

        return sb.toString();
    }
}