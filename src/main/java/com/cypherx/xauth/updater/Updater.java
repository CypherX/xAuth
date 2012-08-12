package com.cypherx.xauth.updater;

import com.cypherx.xauth.utils.xAuthLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Updater {
    private static final String VERSION_PATH = "http://ci.luricos.de/public/xAuth/?C=M;O=D";

    private final String currVer;
    private String latestVer;
    private UpdatePriority priority;

    public Updater(String currVer) {
        this.currVer = currVer;
        loadLatestVersion();
    }

    private void loadLatestVersion() {
        BufferedReader reader = null;

        try {
            URL url = new URL(VERSION_PATH);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = "";
            StringBuilder contents = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                contents.append(line);
            }

            Pattern versionPattern = Pattern.compile("<a[^>]+>xAuth-([0-9].+?)-bin.+</a>");
            Matcher versionMatch = versionPattern.matcher(contents.toString());

            if (!versionMatch.find()) {
                throw new IOException("Could not check for newer version!");
            }

            latestVer = versionMatch.group(1);
            // @TODO modify package.xml to re-implement priority feature - low prio
            priority = UpdatePriority.getPriority(3);
        } catch (IOException e) {
            xAuthLog.warning(e.getMessage());
            latestVer = null;
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
            }
        }
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
        xAuthLog.warning("Download: http://ci.luricos.de/public/xAuth/");
        xAuthLog.warning("-------------------------------");
    }
}