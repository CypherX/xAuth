package com.cypherx.xauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
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

	public static boolean isIPAddress(String ipAddress) {
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