package com.cypherx.xauth.updater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.cypherx.xauth.xAuthLog;

public class Updater {
	private static final String VERSION_FILE = "http://love-despite.com/cypher/bukkit/xAuth/version.txt";

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
			URL url = new URL(VERSION_FILE);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String str = reader.readLine();
			String[] split = str.split("\\|");
			latestVer = split[0];
			priority =  UpdatePriority.getPriority(Integer.parseInt(split[1]));
		} catch (IOException e) {
			xAuthLog.warning("Could not check for newer version!");
			latestVer = null;
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {}
		}
	}

	public boolean isUpdateAvailable() {
		if (latestVer == null)
			return false;

		return compareVer(latestVer, currVer) > 0;
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

	public void printMessage() {
		xAuthLog.warning("-------- xAuth Updater --------");
		xAuthLog.warning("This server appears to be running an older version");
		xAuthLog.warning(String.format("of xAuth. Version %s is now available.", latestVer));
		xAuthLog.warning("");
		xAuthLog.warning(String.format("Priority: %s", priority));
		xAuthLog.warning("Details: http://bit.ly/I5nFne");
		xAuthLog.warning("Download: http://bit.ly/IbRYP2");
		xAuthLog.warning("-------------------------------");
	}
}