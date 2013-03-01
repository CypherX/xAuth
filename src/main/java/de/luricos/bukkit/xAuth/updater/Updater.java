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
package de.luricos.bukkit.xAuth.updater;

import de.luricos.bukkit.xAuth.utils.xAuthLog;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Updater {
    private static final String VERSION_CHECK_URI = "http://ci.luricos.de/public/xAuth/?C=M;O=D";

    private final String currVer;
    private String latestVer;
    private UpdatePriority priority;

    public Updater(String currVer) {
        this.currVer = currVer;
        loadLatestVersion();
    }

    private void loadLatestVersion() {
        HTTPRequest request = new HTTPRequest(VERSION_CHECK_URI);
        String content = request.getContent();

        // die silently but set versionString to "version check failed"
        if (!(content.length() > 0)) {
            //xAuthLog.warning("HTTPRequest: Input stream was empty. Connection problems?");
            latestVer = "{Version check failed}";
            return;
        }

        try {
            Pattern versionPattern = Pattern.compile("<a[^>]+>xAuth-([0-9].+?)-bin.+</a>");
            Matcher versionMatch = versionPattern.matcher(content.toString());

            if (!versionMatch.find()) {
                throw new IOException("Could not check for newer version!");
            }

            latestVer = versionMatch.group(1);
            // @TODO modify package.xml to re-implement priority feature - low prio
            priority = UpdatePriority.getPriority(3);
        } catch (IOException e) {
            xAuthLog.warning(e.getMessage());
            latestVer = null;
        }

//
//
//
//        BufferedReader reader = null;
//
//        try {
//            URL url = new URL(VERSION_CHECK_URI);
//            reader = new BufferedReader(new InputStreamReader(url.openStream()));
//            String line = "";
//            StringBuilder contents = new StringBuilder();
//            while ((line = reader.readLine()) != null) {
//                contents.append(line);
//            }
//
//            Pattern versionPattern = Pattern.compile("<a[^>]+>xAuth-([0-9].+?)-bin.+</a>");
//            Matcher versionMatch = versionPattern.matcher(contents.toString());
//
//            if (!versionMatch.find()) {
//                throw new IOException("Could not check for newer version!");
//            }
//
//            latestVer = versionMatch.group(1);
//            // @TODO modify package.xml to re-implement priority feature - low prio
//            priority = UpdatePriority.getPriority(3);
//        } catch (IOException e) {
//            xAuthLog.warning(e.getMessage());
//            latestVer = null;
//        } finally {
//            try {
//                if (reader != null)
//                    reader.close();
//            } catch (IOException ignored) {
//            }
//        }
    }

    public boolean isUpdateAvailable() {
        return getLatestVersionString() != null && compareVer(getLatestVersionString(), getCurrentVer()) > 0;

    }

    private int compareVer(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;

        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i]))
            i++;

        if (i < vals1.length && i < vals2.length) {
            int diff = new Integer(vals1[i]).compareTo(new Integer(vals2[i]));
            return diff < 0 ? -1 : diff == 0 ? 0 : 1;
        }

        return vals1.length < vals2.length ? -1 : vals1.length == vals2.length ? 0 : 1;
    }

    public String getLatestVersionString() {
        return latestVer;
    }

    public String getCurrentVer() {
        return currVer;
    }

    public UpdatePriority getPriority() {
        return priority;
    }

    public void printMessage() {
        xAuthLog.warning("-------- xAuth Updater --------");
        xAuthLog.warning("This server appears to be running an older version");
        xAuthLog.warning(String.format("of xAuth. Version %s is now available.", getLatestVersionString()));
        xAuthLog.warning("");
        xAuthLog.warning(String.format("Priority: %s", getPriority().toString()));
        xAuthLog.warning("Details: http://github.com/lycano/xAuth/");
        xAuthLog.warning("Download: http://dev.bukkit.org/server-mods/xAuth/");
        xAuthLog.warning("-------------------------------");
    }
}