package com.cypherx.xauth;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	public static String streamToString(InputStream in) {
		if (in != null) {
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
				} catch (IOException e) {}
		    }
		    return writer.toString();
		} else     
		    return "";
	}

	public static void downloadFile(File file, String location) {
		BufferedInputStream input = null;
		FileOutputStream output = null;

		try {
			URL url = new URL(location);
			input = new BufferedInputStream(url.openStream());
			output = new FileOutputStream(file);

			byte data[] = new byte[1024];
			int count;

			while ((count = input.read(data)) != -1)
				output.write(data, 0, count);
		} catch (IOException e) {
			xAuthLog.severe("Failed to download file: " + file.getName(), e);
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException e) {}

			try {
				if (output != null)
					output.close();
			} catch (IOException e) {}
		}
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
}