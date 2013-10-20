/*
 * xAuth for Bukkit
 * Copyright (C) 2012 Lycano <https://github.com/lycano/xAuth/>
 *
 * Copyright (C) 2011 CypherX <https://github.com/CypherX/xAuth/>
 *
 * This file is a modified version of gravitylow's Updater <https://github.com/gravitylow/Updater>
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


/*
 * Updater for Bukkit.
 *
 * This class provides the means to safely and easily update a plugin, or check to see if it is updated using dev.bukkit.org
 */

package de.luricos.bukkit.xAuth.updater;

import de.luricos.bukkit.xAuth.MessageHandler;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Check dev.bukkit.org to find updates for a given plugin, and download the updates if needed.
 * <p/>
 * <b>VERY, VERY IMPORTANT</b>: Because there are no standards for adding auto-update toggles in your plugin's config, this system provides NO CHECK WITH YOUR CONFIG to make sure the user has allowed auto-updating.
 * <br>
 * It is a <b>BUKKIT POLICY</b> that you include a boolean value in your config that prevents the auto-updater from running <b>AT ALL</b>.
 * <br>
 * If you fail to include this option in your config, your plugin will be <b>REJECTED</b> when you attempt to submit it to dev.bukkit.org.
 * <p/>
 * An example of a good configuration option would be something similar to 'auto-update: true' - if this value is set to false you may NOT run the auto-updater.
 * <br>
 * If you are unsure about these rules, please read the plugin submission guidelines: http://goo.gl/8iU5l
 *
 * @author Gravity
 * @version 2.0
 */

public class Updater {

    private Plugin plugin;
    private UpdateType type;
    private String versionName;
    private String versionLink;
    private String versionType;
    private String versionGameVersion;

    private String remoteName;
    private String remoteVersion;

    private boolean announce; // Whether to announce file downloads

    private URL url; // Connecting to RSS
    private File file; // The plugin's file
    private Thread thread; // Updater thread

    private int id = -1; // Project's Curse ID
    private String apiKey = null; // BukkitDev ServerMods API key
    private String userAgent = "xAuth-Updater (by luricos)";
    private String updaterConfigFileName = "updater.yml";

    private static final String TITLE_VALUE = "name"; // Gets remote file's title
    private static final String LINK_VALUE = "downloadUrl"; // Gets remote file's download link
    private static final String TYPE_VALUE = "releaseType"; // Gets remote file's release type
    private static final String VERSION_VALUE = "gameVersion"; // Gets remote file's build version
    private static final String QUERY = "/servermods/files?projectIds="; // Path to GET
    private static final String HOST = "https://api.curseforge.com"; // Slugs will be appended to this to get to the project's RSS feed

    private static final String[] NO_UPDATE_TAG = { "-DEV", "-PRE", "-SNAPSHOT" }; // If the version number contains one of these, don't update.
    private static final int BYTE_SIZE = 1024; // Used for downloading files
    private YamlConfiguration config; // Config file
    private String updateFolder;// The folder that downloads will be placed in
    private Updater.UpdateResult result = Updater.UpdateResult.SUCCESS; // Used for determining the outcome of the update process

    private MessageHandler messageHandler = xAuth.getPlugin().getMessageHandler();

    /**
     * Gives the dev the result of the update process. Can be obtained by called getResult().
     */
    public enum UpdateResult {
        /**
         * The updater found an update, and has readied it to be loaded the next time the server restarts/reloads.
         */
        SUCCESS,
        /**
         * The updater did not find an update, and nothing was downloaded.
         */
        NO_UPDATE,
        /**
         * The server administrator has disabled the updating system
         */
        DISABLED,
        /**
         * The updater found an update, but was unable to download it.
         */
        FAIL_DOWNLOAD,
        /**
         * For some reason, the updater was unable to contact dev.bukkit.org to download the file.
         */
        FAIL_DBO,
        /**
         * When running the version check, the file on DBO did not contain the a version in the format 'vVersion' such as 'v1.0'.
         */
        FAIL_NOVERSION,
        /**
         * The id provided by the plugin running the updater was invalid and doesn't exist on DBO.
         */
        FAIL_BADID,
        /**
         * The server administrator has improperly configured their API key in the configuration
         */
        FAIL_APIKEY,
        /**
         * The updater found an update, but because of the UpdateType being set to NO_DOWNLOAD, it wasn't downloaded.
         */
        UPDATE_AVAILABLE
    }

    /**
     * Allows the dev to specify the type of update that will be run.
     */
    public enum UpdateType {
        /**
         * Run a version check, and then if the file is out of date, download the newest version.
         */
        DEFAULT,
        /**
         * Don't run a version check, just find the latest update and download it.
         */
        NO_VERSION_CHECK,
        /**
         * Get information about the version and the download size, but don't actually download anything.
         */
        NO_DOWNLOAD
    }

    /**
     * Initialize the updater
     *
     * @param plugin   The plugin that is checking for an update.
     * @param id       The dev.bukkit.org id of the project
     * @param file     The file that the plugin is running from, get this by doing this.getFile() from within your main class.
     * @param type     Specify the type of update this will be. See {@link UpdateType}
     * @param announce True if the program should announce the progress of new updates in console
     */
    public Updater(Plugin plugin, int id, File file, UpdateType type, boolean announce) {
        this.plugin = plugin;
        this.type = type;
        this.announce = announce;
        this.file = file;
        this.id = id;

        this.updateFolder = plugin.getServer().getUpdateFolder();

        this.init();
    }

    private void init() {
        final File updaterFile = plugin.getDataFolder();
        final File updaterConfigFile = new File(updaterFile, this.updaterConfigFileName);

        if (!updaterFile.exists()) {
            updaterFile.mkdir();
        }
        if (!updaterConfigFile.exists()) {
            try {
                updaterConfigFile.createNewFile();
            } catch (final IOException e) {
                plugin.getLogger().severe("The updater could not create a configuration in " + updaterFile.getAbsolutePath());
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(updaterConfigFile);

        this.config.options().header("This configuration file affects all plugins using the Updater system (version 2+ - http://forums.bukkit.org/threads/96681/ )" + '\n'
                + "If you wish to use your API key, read http://wiki.bukkit.org/ServerMods_API and place it below." + '\n'
                + "Some updating systems will not adhere to the disabled value, but these may be turned off in their plugin's configuration.");
        this.config.addDefault("api-key", "PUT_API_KEY_HERE");
        this.config.addDefault("disable", false);

        if (this.config.get("api-key", null) == null) {
            this.config.options().copyDefaults(true);
            try {
                this.config.save(updaterConfigFile);
            } catch (final IOException e) {
                plugin.getLogger().severe("The updater could not save the configuration in " + updaterFile.getAbsolutePath());
                e.printStackTrace();
            }
        }

        if (this.config.getBoolean("disable")) {
            this.result = UpdateResult.DISABLED;
            return;
        }

        String key = this.config.getString("api-key");
        if (key.equalsIgnoreCase("PUT_API_KEY_HERE") || key.equals("")) {
            key = null;
        }

        this.apiKey = key;

        try {
            this.url = new URL(Updater.HOST + Updater.QUERY + this.id);
        } catch (final MalformedURLException e) {
            plugin.getLogger().severe("The project ID provided for updating, " + this.id + " is invalid.");
            this.result = UpdateResult.FAIL_BADID;
            e.printStackTrace();
        }
    }

    public void run() {
        this.thread = new Thread(new UpdateRunnable());
        this.thread.start();
    }

    public void setType(UpdateType type) {
        this.type = type;
    }

    public void setAnnounce(boolean announce) {
        this.announce = announce;
    }

    /**
     * Get the result of the update process.
     */
    public Updater.UpdateResult getResult() {
        this.waitForThread();
        return this.result;
    }

    /**
     * Get the latest version's release type (release, beta, or alpha).
     */
    public String getLatestType() {
        this.waitForThread();
        return this.versionType;
    }

    /**
     * Get the latest version's game version.
     */
    public String getLatestGameVersion() {
        this.waitForThread();
        return this.versionGameVersion;
    }

    /**
     * Get the latest version's name.
     */
    public String getLatestName() {
        this.waitForThread();
        return this.versionName;
    }

    /**
     * Get the latest version's file link.
     */
    public String getLatestFileLink() {
        this.waitForThread();
        return this.versionLink;
    }

    /**
     * Get the latest remote name
     */
    public String getRemoteName() {
        this.waitForThread();
        return this.remoteName;
    }

    /**
     * Get the latest remote version
     */
    public String getRemoteVersion() {
        this.waitForThread();
        return this.remoteVersion;
    }

    /**
     * As the result of Updater output depends on the thread's completion, it is necessary to wait for the thread to finish
     * before allowing anyone to check the result.
     */
    private void waitForThread() {
        if ((this.thread != null) && this.thread.isAlive()) {
            try {
                this.thread.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save an update from dev.bukkit.org into the server's update folder.
     */
    private void saveFile(File folder, String file, String u) {
        //cleanup update working directory before downloading the file
        if (folder.exists()) {
            try {
                File[] currList;
                Stack<File> stack = new Stack<File>();
                stack.push(folder);
                while (! stack.isEmpty()) {
                    if (stack.lastElement().isDirectory()) {
                        currList = stack.lastElement().listFiles();
                        if (currList.length > 0) {
                            for (File curr: currList) {
                                stack.push(curr);
                            }
                        } else {
                            stack.pop().delete();
                        }
                    } else {
                        stack.pop().delete();
                    }
                }
            } catch (SecurityException e) {
            }
        } else {
            folder.mkdir();
        }

        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            // Download the file
            final URL url = new URL(u);
            final int fileLength = url.openConnection().getContentLength();
            in = new BufferedInputStream(url.openStream());
            fout = new FileOutputStream(folder.getAbsolutePath() + File.separator + file);

            final byte[] data = new byte[Updater.BYTE_SIZE];
            int count;

            if (this.announce) {
                xAuthLog.info("About to download a new update: " + this.versionName);
            }

            // download file and show progress if this.announce is set to true
            long downloaded = 0;
            while ((count = in.read(data, 0, Updater.BYTE_SIZE)) != -1) {
                downloaded += count;
                fout.write(data, 0, count);
                final int percent = (int) ((downloaded * 100) / fileLength);
                if (this.announce && ((percent % 10) == 0)) {
                    xAuthLog.info("Downloading update: " + percent + "% of " + fileLength + " bytes.");
                }
            }

            // Check to see if it's a zip file, if it is, unzip it.
            final File dFile = new File(folder.getAbsolutePath() + File.separator + file);
            if (dFile.getName().endsWith(".zip")) {
                // Unzip
                this.unzip(dFile.getCanonicalPath());
            }

            if (this.announce) {
                xAuthLog.info("Update process completed.");
            }
        } catch (final Exception ex) {
            xAuthLog.warning("The auto-updater tried to download a new update, but was unsuccessful.");
            this.result = Updater.UpdateResult.FAIL_DOWNLOAD;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (fout != null) {
                    fout.close();
                }
            } catch (final Exception ex) {
            }
        }
    }

    /**
     * Part of Zip-File-Extractor, modified by Gravity for use with Bukkit
     */
    private void unzip(String file) {
        try {
            final File fSourceZip = new File(file);
            final String zipPath = file.substring(0, file.length() - 4);
            ZipFile zipFile = new ZipFile(fSourceZip, ZipFile.OPEN_READ);

            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                File destinationFilePath = new File(zipPath, entry.getName());
                destinationFilePath.getParentFile().mkdirs();
                if (entry.isDirectory()) {
                    continue;
                } else {
                    final BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                    int b;
                    final byte buffer[] = new byte[Updater.BYTE_SIZE];
                    final FileOutputStream fos = new FileOutputStream(destinationFilePath);
                    final BufferedOutputStream bos = new BufferedOutputStream(fos, Updater.BYTE_SIZE);
                    while ((b = bis.read(buffer, 0, Updater.BYTE_SIZE)) != -1) {
                        bos.write(buffer, 0, b);
                    }
                    bos.flush();
                    bos.close();
                    bis.close();
                    final String name = destinationFilePath.getName();
                    if (name.endsWith(".jar") && this.pluginFile(name)) {
                        destinationFilePath.renameTo(new File(this.plugin.getDataFolder().getParent(), this.updateFolder + File.separator + name));
                    }
                }
                entry = null;
                destinationFilePath = null;
            }
            e = null;
            zipFile.close();
            zipFile = null;

            // Move any plugin data folders that were included to the right place, Bukkit won't do this for us.
            for (final File dFile : new File(zipPath).listFiles()) {
                if (dFile.isDirectory()) {
                    if (this.pluginFile(dFile.getName())) {
                        final File oFile = new File(this.plugin.getDataFolder().getParent(), dFile.getName()); // Get current dir
                        final File[] contents = oFile.listFiles(); // List of existing files in the current dir
                        for (final File cFile : dFile.listFiles()) { // Loop through all the files in the new dir
                            boolean found = false;
                            for (final File xFile : contents) { // Loop through contents to see if it exists
                                if (xFile.getName().equals(cFile.getName())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                // Move the new file into the current dir
                                cFile.renameTo(new File(oFile.getCanonicalFile() + File.separator + cFile.getName()));
                            } else {
                                // This file already exists, so we don't need it anymore.
                                cFile.delete();
                            }
                        }
                    }
                }
                dFile.delete();
            }
            new File(zipPath).delete();
            fSourceZip.delete();
        } catch (final IOException ex) {
            xAuthLog.warning("The auto-updater tried to unzip a new update file, but was unsuccessful.");
            this.result = Updater.UpdateResult.FAIL_DOWNLOAD;
            ex.printStackTrace();
        }
        new File(file).delete();
    }

    /**
     * Check if the name of a jar is one of the plugins currently installed, used for extracting the correct files out of a zip.
     */
    private boolean pluginFile(String name) {
        for (final File file : new File("plugins").listFiles()) {
            if (file.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check to see if the program should continue by evaluation whether the plugin is already updated, or shouldn't be updated
     */
    private boolean versionCheck(String title) {
        if (this.type != UpdateType.NO_VERSION_CHECK) {
            final String version = this.plugin.getDescription().getVersion();
            if (title.split(" v").length == 2) {
                String[] titleParts = title.split(" v");
                this.remoteVersion = titleParts[1].split(" ")[0]; // Get the newest file's version number
                this.remoteName = titleParts[0];

                if (this.hasTag(version) || version.equalsIgnoreCase(this.remoteVersion)) {
                    // We already have the latest version, or this build is tagged for no-update
                    this.result = Updater.UpdateResult.NO_UPDATE;
                    return false;
                }
            } else {
                // The file's name did not contain the string 'vVersion'
                final String authorInfo = this.plugin.getDescription().getAuthors().size() == 0 ? "" : " (" + this.plugin.getDescription().getAuthors().get(0) + ")";
                xAuthLog.warning("The author of this plugin" + authorInfo + " has misconfigured their Auto Update system");
                xAuthLog.warning("File versions should follow the format 'PluginName vVERSION'");
                xAuthLog.warning("Please notify the author of this error.");
                this.result = Updater.UpdateResult.FAIL_NOVERSION;
                return false;
            }
        }
        return true;
    }

    public String getResultMessages() {
        String message = "";
        switch (this.getResult()) {
            case DISABLED:
                message = messageHandler.getNode("version.updater.disabled");
                break;
            case SUCCESS:
                message = String.format(messageHandler.getNode("version.updater.update-finished"), this.getLatestGameVersion());
                break;
            case NO_UPDATE:
                message = String.format(messageHandler.getNode("version.updater.no-update-needed"), this.getLatestGameVersion());
                break;
            case FAIL_DOWNLOAD:
                message = messageHandler.getNode("version.updater.error.download");
                break;
            case FAIL_DBO:
                message = messageHandler.getNode("version.updater.error.dbo");
                break;
            case FAIL_NOVERSION:
                message = messageHandler.getNode("version.updater.error.noversion");
                break;
            case FAIL_BADID:
                message = messageHandler.getNode("version.updater.error.badid");
                break;
            case FAIL_APIKEY:
                message = messageHandler.getNode("version.updater.error.apikey");
                break;
            case UPDATE_AVAILABLE:
                message = String.format(messageHandler.getNode("version.updater.update-available"), this.getLatestName(), this.getLatestType(), this.getLatestGameVersion(), this.getLatestFileLink());
                message += "\n" + messageHandler.getNode("version.updater.autoupdate");
                break;
        }

        return message;
    }

    /**
     * Evaluate whether the version number is marked showing that it should not be updated by this program
     */
    private boolean hasTag(String version) {
        for (final String string : Updater.NO_UPDATE_TAG) {
            if (version.contains(string)) {
                return true;
            }
        }
        return false;
    }

    private boolean read() {
        try {
            final URLConnection conn = this.url.openConnection();
            conn.setConnectTimeout(5000);

            if (this.apiKey != null) {
                conn.addRequestProperty("X-API-Key", this.apiKey);
            }
            conn.addRequestProperty("User-Agent", this.userAgent);

            conn.setDoOutput(true);

            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();

            final JSONArray array = (JSONArray) JSONValue.parse(response);

            if (array.size() == 0) {
                xAuthLog.warning("The updater could not find any files for the project id " + this.id);
                this.result = UpdateResult.FAIL_BADID;
                return false;
            }

            this.versionName = (String) ((JSONObject) array.get(array.size() - 1)).get(Updater.TITLE_VALUE);
            this.versionLink = (String) ((JSONObject) array.get(array.size() - 1)).get(Updater.LINK_VALUE);
            this.versionType = (String) ((JSONObject) array.get(array.size() - 1)).get(Updater.TYPE_VALUE);
            this.versionGameVersion = (String) ((JSONObject) array.get(array.size() - 1)).get(Updater.VERSION_VALUE);

            return true;
        } catch (final IOException e) {
            if (e.getMessage().contains("HTTP response code: 403")) {
                xAuthLog.warning("dev.bukkit.org rejected the API key provided in plugins/" + this.plugin.getName() + "/" + this.updaterConfigFileName);
                xAuthLog.warning("Please double-check your configuration to ensure it is correct.");
                this.result = UpdateResult.FAIL_APIKEY;
            } else {
                xAuthLog.warning("The updater could not contact dev.bukkit.org for updating.");
                xAuthLog.warning("If you have not recently modified your configuration and this is the first time you are seeing this message, the site may be experiencing temporary downtime.");
                this.result = UpdateResult.FAIL_DBO;
            }
            e.printStackTrace();
            return false;
        }
    }

    private class UpdateRunnable implements Runnable {

        @Override
        public void run() {
            try {
                if (Updater.this.url == null)
                    throw new UpdaterUrlNotNullException("Url is null. Please check previous errors");

                if (!Updater.this.read())
                    throw new UpdaterReadProcessErrorException("An error occured during read process. Please check previous errors");

                // Obtain the results of the project's file feed
                if (!Updater.this.versionCheck(Updater.this.versionName))
                    throw new UpdaterInvalidVersionException("An error occured during the version check process. Please check version format");

                if (Updater.this.type.equals(UpdateType.NO_DOWNLOAD)) {
                    Updater.this.result = UpdateResult.UPDATE_AVAILABLE;
                    return;
                }

                if (Updater.this.versionLink == null)
                    throw new UpdaterVersionLinkNotNullException("versionLink is null. Please check previous errors.");

                String name = Updater.this.file.getName();
                // If it's a zip file, it shouldn't be downloaded as the plugin's name
                if (Updater.this.versionLink.endsWith(".zip")) {
                    final String[] split = Updater.this.versionLink.split("/");
                    name = split[split.length - 1];
                }
                Updater.this.saveFile(new File(Updater.this.plugin.getDataFolder().getParent(), Updater.this.updateFolder + File.separator + Updater.this.remoteName), name, Updater.this.versionLink);
            } catch (UpdaterUrlNotNullException e) {
                xAuthLog.severe(e.getMessage());
            } catch (UpdaterReadProcessErrorException e) {
                xAuthLog.severe(e.getMessage());
            } catch (UpdaterInvalidVersionException e) {
                xAuthLog.severe(e.getMessage());
            } catch (UpdaterVersionLinkNotNullException e) {
                xAuthLog.severe(e.getMessage());
            }
        }

    }

    public abstract class UpdaterException extends RuntimeException {
        protected UpdaterException() {
        }

        protected UpdaterException(String msg) {
            super(msg);
        }
    }

    private class UpdaterUrlNotNullException extends UpdaterException {
            private String message;

            public UpdaterUrlNotNullException(String message) {
                this.message = message;
            }

            @Override
            public String getMessage() {
                return this.message;
            }
    }

    private class UpdaterReadProcessErrorException extends UpdaterException {
        private String message;

        public UpdaterReadProcessErrorException(String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return this.message;
        }
    }

    private class UpdaterInvalidVersionException extends UpdaterException {
        private String message;

        public UpdaterInvalidVersionException(String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return this.message;
        }
    }

    private class UpdaterVersionLinkNotNullException extends UpdaterException {
        private String message;

        public UpdaterVersionLinkNotNullException(String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return this.message;
        }
    }
}