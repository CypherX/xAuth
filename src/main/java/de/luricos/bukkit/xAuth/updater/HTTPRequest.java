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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author lycano
 */
public class HTTPRequest {

    private URL url;
    private URLConnection connection;
    private BufferedReader inputStream;
    private String content;

    public HTTPRequest(final String url) {
        this(url, 30, 30);
    }

    public HTTPRequest(final String uri, final int connectTimeout, final int readTimeout) {
        try {
            this.url = new URL(uri);
        } catch (MalformedURLException e) {
            xAuthLog.severe("HTTPRequest: Malformed uri - " + e.getMessage());
            return;
        }

        try {
            this.connection = this.url.openConnection();
            this.getConnection().setConnectTimeout(connectTimeout);
            this.getConnection().setReadTimeout(readTimeout);
            this.inputStream = new BufferedReader(new InputStreamReader(this.getUrl().openStream()));

            String inputLine;
            StringBuilder contents = new StringBuilder();
            while ((inputLine = this.getStream().readLine()) != null) {
                contents.append(inputLine);
            }

            this.content = contents.toString();
        } catch (IOException e) {
            xAuthLog.warning("Error during HTTPRequest: " + e.getMessage());
        } finally {
            try {
                this.inputStream.close();
            } catch (IOException e) {
                xAuthLog.warning(String.format("HTTPRequest: Could not close stream - %s", e.getMessage()));
            }
        }
    }

    protected URL getUrl() {
        return url;
    }

    protected BufferedReader getStream() {
        return this.inputStream;
    }

    protected URLConnection getConnection() {
        return this.connection;
    }

    public String getContent() {
        return this.content;
    }
}
