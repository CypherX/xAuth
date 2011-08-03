package com.cypherx.xauth;

import com.cypherx.xauth.commands.*;
import com.cypherx.xauth.database.*;
import com.cypherx.xauth.database.Database.DBMS;
import com.cypherx.xauth.listeners.*;
import com.cypherx.xauth.plugins.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class xAuth extends JavaPlugin {
	public static PluginDescriptionFile desc;
	public static File dataFolder;
	private ConcurrentHashMap<String, xAuthPlayer> playerCache = new ConcurrentHashMap<String, xAuthPlayer>();
	private ConcurrentHashMap<UUID, TeleLocation> teleLocations = new ConcurrentHashMap<UUID, TeleLocation>();
	private UUID globalUID = null;

	public void onDisable() {
		Player[] players = getServer().getOnlinePlayers();
		if (players.length > 0) {
			for (Player player : players) {
				xAuthPlayer xPlayer = getPlayer(player.getName());
				if (xPlayer.isGuest())
					removeGuest(xPlayer);
			}
		}

		Database.close();
		xAuthSettings.saveChanges();
		xAuthLog.info("v" + desc.getVersion() + " Disabled!");
	}

	public void onEnable() {
		desc = getDescription();
		dataFolder = getDataFolder();

		if (!dataFolder.exists())
			dataFolder.mkdirs();

		xAuthSettings.setup();
		xAuthMessages.setup();

		if (xAuthSettings.autoDisable && getServer().getOnlineMode()) {
			xAuthLog.warning("Disabling - Server is running in online-mode");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		initializePlugins();

		if (xAuthSettings.downloadLib && Database.getDBMS() == DBMS.H2 && !checkLibrary()) {
			xAuthLog.info("Downloading required library file..");
			downloadLibrary();
			xAuthLog.info("Download complete! Please restart/reload the server.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		Database.connect();
		if (!Database.isConnected()) {
			xAuthLog.severe("Disabling - No connection to database");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		DbUpdate dbUpdate = new DbUpdate();
		if (!dbUpdate.checkVersion()) {
			xAuthLog.info("Updating database..");
			dbUpdate.update();
		}

		DbUtil.deleteExpiredSessions();
		loadTeleLocations();

		File oldAuthFile = new File(dataFolder, "auths.txt");
		if (oldAuthFile.exists())
			importAccounts(oldAuthFile);

		DbUtil.printStats();

		Player[] players = getServer().getOnlinePlayers();
		if (players.length > 0) // /reload was used
			handleReload(players);

		(new xAuthPlayerListener(this)).registerEvents();
		(new xAuthBlockListener(this)).registerEvents();
		(new xAuthEntityListener(this)).registerEvents();
		getCommand("register").setExecutor(new RegisterCommand(this));
		getCommand("login").setExecutor(new LoginCommand(this));
		getCommand("changepw").setExecutor(new ChangePasswordCommand(this));
		getCommand("logout").setExecutor(new LogoutCommand(this));
		getCommand("xauth").setExecutor(new xAuthCommand(this));

		xAuthLog.info("v" + desc.getVersion() + " Enabled!");
	}

	private void initializePlugins() {
		xSpout.setup(this);
		xHelp.setup(this);
		xPermissions.setup(this);
	}

	private boolean checkLibrary() {
		File file = new File("lib/h2.jar");
		return file.exists() && !file.isDirectory();
	}

	private void downloadLibrary() {
		File dir = new File("lib");
		if (!dir.exists())
			dir.mkdir();

		File file = new File(dir, "h2.jar");
		BufferedInputStream input = null;
		FileOutputStream output = null;

		try {
			URL url = new URL("http://dl.dropbox.com/u/24661378/Bukkit/lib/h2.jar");
			input = new BufferedInputStream(url.openStream());
			output = new FileOutputStream(file);

			byte data[] = new byte[1024];
			int count;

			while ((count = input.read(data)) != -1)
				output.write(data, 0, count);
		} catch (IOException e) {
			xAuthLog.severe("Could not downloaded required library file!", e);
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

	private void importAccounts(File oldAuthFile) {
		xAuthLog.info("Importing old auths.txt file to new format..");
		List<Account> accounts = new ArrayList<Account>();
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(oldAuthFile));
			String line;
			Account account;

			while ((line = reader.readLine()) != null) {
				String[] split = line.split(":");
				account = new Account(split[0], split[1], null);
				accounts.add(account);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {}
		}

		DbUtil.insertAccounts(accounts);

		if (oldAuthFile.renameTo(new File(dataFolder, "auths.txt.old")))
			xAuthLog.info("Import complete! auths.txt renamed to auths.txt.old");
		else
			xAuthLog.info("Import complete! Verify that all accounts were imported then remove/rename auths.txt");
	}

	public xAuthPlayer getPlayer(String playerName) {
		String lowPlayerName = playerName.toLowerCase();

		if (playerCache.containsKey(lowPlayerName))
			return playerCache.get(lowPlayerName);

		xAuthPlayer xPlayer = DbUtil.getPlayerFromDb(playerName);
		if (xPlayer == null)
			xPlayer = new xAuthPlayer(playerName);

		playerCache.put(lowPlayerName, xPlayer);
		return xPlayer;
	}

	public xAuthPlayer getPlayerJoin(String playerName) {
		String lowPlayerName = playerName.toLowerCase();

		if (playerCache.containsKey(lowPlayerName))
			return DbUtil.reloadPlayer(playerCache.get(lowPlayerName));

		xAuthPlayer xPlayer = DbUtil.getPlayerFromDb(playerName);
		if (xPlayer == null)
			xPlayer = new xAuthPlayer(playerName);

		playerCache.put(lowPlayerName, xPlayer);
		return xPlayer;
	}

	private void handleReload(Player[] players) {
		for (Player player : players) {
			xAuthPlayer xPlayer = getPlayerJoin(player.getName());
			boolean isRegistered = xPlayer.isRegistered();

			if (!xPlayer.isAuthenticated() && (isRegistered || (!isRegistered && xPlayer.mustRegister()))) {
				createGuest(xPlayer);
				xAuthMessages.send("miscReloaded", player);
			}
		}
	}

	public void createGuest(xAuthPlayer xPlayer) {
		final Player player = xPlayer.getPlayer();

		// remove old session (if any)
		if (xPlayer.hasSession())
			DbUtil.deleteSession(xPlayer);

		if (xAuthSettings.guestTimeout > 0 && xPlayer.isRegistered()) {
			int taskId = getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
				public void run() {
					player.kickPlayer(xAuthMessages.get("miscKickTimeout", player, null));
				}
			}, xAuthSettings.guestTimeout * 20);

			xPlayer.setTimeoutTaskId(taskId);
		}

		protect(xPlayer);
		xPlayer.setLastNotifyTime(Util.getNow());
		xPlayer.setGuest(true);
	}

	public void removeGuest(xAuthPlayer xPlayer) {
		if (!xPlayer.isGuest())
			return;

		getServer().getScheduler().cancelTask(xPlayer.getTimeoutTaskId());
		restore(xPlayer);
		xPlayer.setGuest(false);
	}

	public void protect(final xAuthPlayer xPlayer) {
		final Player player = xPlayer.getPlayer();
		PlayerInventory playerInv = player.getInventory();

		DbUtil.insertInventory(xPlayer);
		playerInv.clear();
		playerInv.setHelmet(null);
		playerInv.setChestplate(null);
		playerInv.setLeggings(null);
		playerInv.setBoots(null);
		player.saveData();

		//if (player.getHealth() > 0)
			//xPlayer.setLocation(player.getLocation());

		//if (xAuthSettings.protectLoc)
			//player.teleport(getLocationToTeleport(player.getWorld()));

		// possible fix for spawning issues
		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
			public void run() {
				if (player.getHealth() > 0)
					xPlayer.setLocation(player.getLocation());

				if (xAuthSettings.protectLoc)
					player.teleport(getLocationToTeleport(player.getWorld()));
			}
		}, 2);
	}

	public void restore(xAuthPlayer xPlayer) {
		Player player = xPlayer.getPlayer();
		PlayerInventory playerInv = player.getInventory();

		ItemStack[] inv = DbUtil.getInventory(xPlayer);
		ItemStack[] items = new ItemStack[inv.length - 4];
		ItemStack[] armor = new ItemStack[4];

		for (int i = 0; i < inv.length - 4; i++)
			items[i] = inv[i];

		//Backpack fix
		if (playerInv.getSize() > items.length) {
			ItemStack[] newItems = new ItemStack[playerInv.getSize()];

			for(int i = 0; i < items.length; i++)
				newItems[i] = items[i];

			items = newItems;
		}
		//end Backpack fix

		armor[0] = inv[inv.length - 4];
		armor[1] = inv[inv.length - 3];
		armor[2] = inv[inv.length - 2];
		armor[3] = inv[inv.length - 1];

		playerInv.setContents(items);
		playerInv.setArmorContents(armor);
		DbUtil.deleteInventory(xPlayer);

		if (xPlayer.getLocation() != null)
			xPlayer.getPlayer().teleport(xPlayer.getLocation());
		player.saveData();
	}

	public void login(xAuthPlayer xPlayer) {
		Account account = xPlayer.getAccount();
		account.setLastLoginDate(Util.getNow());
		account.setLastLoginHost(Util.getHostFromPlayer(xPlayer.getPlayer()));
		DbUtil.saveAccount(account);

		Session session = new Session(account.getId(), account.getLastLoginHost());
		xPlayer.setSession(session);
		DbUtil.insertSession(session);

		removeGuest(xPlayer);
		xPlayer.setStrikes(0);
	}

	public void changePassword(Account account, String newPass) {
		account.setPassword(Util.encrypt(newPass));
		DbUtil.saveAccount(account);
	}

	public boolean checkPassword(Account account, String checkPass) {
		if (xAuthSettings.authURLEnabled) {
			StringBuilder sb = new StringBuilder();

			boolean result = checkAuthURLPass(account.getPlayerName(), checkPass, sb);
			// if true, tell whole server a player logged in
			// else, send the returned string (error message) to the user
			if (result)
				Bukkit.getServer().broadcastMessage("Player '" + account.getPlayerName() + "' logged in with forum name '" + sb.toString() + "'");
			else
				account.getPlayer().sendMessage(sb.toString());
			return result;
		}

		String realPass = account.getPassword();

		// check for old encryption (md5 or whirlpool)
		if (realPass.length() == 32 || realPass.length() == 128) {
			String hash = (realPass.length() == 32 ? Util.md5(checkPass) : Util.whirlpool(checkPass));
			if (realPass.equals(hash)) {
				changePassword(account, checkPass); // change password to use new encryption
				return true;
			} else
				return false;
		}

		// xAuth 2 encryption
		int saltPos = (checkPass.length() >= realPass.length() ? realPass.length() : checkPass.length());

		// extract salt
		String salt = realPass.substring(saltPos, saltPos + 12);

		// encrypt salt + checkPass
		Whirlpool w = new Whirlpool();
		byte[] digest = new byte[Whirlpool.DIGESTBYTES];
		w.NESSIEinit();
		w.NESSIEadd(salt + checkPass);
		w.NESSIEfinalize(digest);
		String hash = Whirlpool.display(digest);
		return (hash.substring(0, saltPos) + salt + hash.substring(saltPos)).equals(realPass);
	}

	public boolean checkAuthURLPass(String user, String pass, StringBuilder response) {
		try {
			user = URLEncoder.encode(user, "UTF-8");
			pass = URLEncoder.encode(pass, "UTF-8");

			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection uc = (HttpURLConnection) new URL(xAuthSettings.authURL).openConnection();

			uc.setRequestMethod("POST");
			uc.setDoInput(true);
			uc.setDoOutput(true);
			uc.setUseCaches(false);
			uc.setAllowUserInteraction(false);
			uc.setInstanceFollowRedirects(false);
			uc.setRequestProperty("User-Agent", "Mozilla/5.0 xAuth/" + desc.getVersion());
			uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			DataOutputStream out = new DataOutputStream(uc.getOutputStream());
			out.writeBytes("user=" + user + "&pass=" + pass);
			out.flush();
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String line = in.readLine();
			boolean success = line != null && line.equals("YES");
			response.append(in.readLine());
			in.close();
			return success;
		} catch (Exception e) {
			response.append(e.getMessage());
			return false;
		}
	}

	private void loadTeleLocations() {
		String sql = "SELECT * FROM `" + xAuthSettings.tblLocation + "`";
		ResultSet rs = Database.queryRead(sql);

		try {
			while (rs.next()) {
				TeleLocation teleLocation = new TeleLocation();
				String uid = rs.getString("uid");
				boolean update = false;

				// Database version 0001 -> 0002 fix
				if (Util.isUUID(uid))
					teleLocation.setUID(UUID.fromString(uid));
				else {
					teleLocation.setUID(getServer().getWorld(uid).getUID());
					update = true;
				}

				teleLocation.setX(rs.getDouble("x"));
				teleLocation.setY(rs.getDouble("y"));
				teleLocation.setZ(rs.getDouble("z"));
				teleLocation.setYaw(rs.getFloat("yaw"));
				teleLocation.setPitch(rs.getFloat("pitch"));
				teleLocation.setGlobal(rs.getInt("global"));
				teleLocations.put(teleLocation.getUID(), teleLocation);

				if (teleLocation.getGlobal() == 1)
					globalUID = teleLocation.getUID();

				if (update) {
					sql = "UPDATE `" + xAuthSettings.tblLocation + "` SET `uid` = ? WHERE `uid` = ?";
					Database.queryWrite(sql, teleLocation.getUID().toString(), uid);
				}
			}
		} catch (SQLException e) {
			xAuthLog.severe("Could not load TeleLocations from database!", e);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {}
		}
	}

	public TeleLocation getTeleLocation(UUID uid) {
		if (uid == null)
			return null;

		return teleLocations.get(uid);
	}

	public void setTeleLocation(TeleLocation teleLocation) {
		TeleLocation tOld = teleLocations.put(teleLocation.getUID(), teleLocation);
		if (teleLocation.getGlobal() == 1)
			globalUID = teleLocation.getUID();

		if (tOld == null)
			DbUtil.insertTeleLocation(teleLocation);
		else
			DbUtil.updateTeleLocation(teleLocation);
	}

	public void removeTeleLocation(TeleLocation teleLocation) {
		teleLocations.remove(teleLocation.getUID());
		if (teleLocation.getGlobal() == 1)
			globalUID = null;

		DbUtil.deleteTeleLocation(teleLocation);
	}

	public Location getLocationToTeleport(World world) {
		TeleLocation teleLocation = getTeleLocation((globalUID == null ? world.getUID() : globalUID));
		return (teleLocation == null ? world.getSpawnLocation() : teleLocation.getLocation());
	}

	public void strikeout(xAuthPlayer xPlayer) {
		Player player = xPlayer.getPlayer();

		if (xAuthSettings.strikeAction.equals("banip")) {
			StrikeBan ban = new StrikeBan(Util.getHostFromPlayer(player));
			DbUtil.insertStrikeBan(ban);
			xAuthLog.info(ban.getHost() + " banned by strike system");
		}

		player.kickPlayer(xAuthMessages.get("miscKickStrike", player, null));
		if (xAuthSettings.strikeAction.equals("kick"))
			xAuthLog.info(player.getName() + " kicked by strike system");

		xPlayer.setStrikes(0);
	}

	public boolean isBanned(String host) {
		final StrikeBan ban = DbUtil.loadStrikeBan(host);
		if (ban == null)
			return false;

		if (xAuthSettings.banLength == 0)
			return true;

		Timestamp unbanTime = new Timestamp(ban.getBanTime().getTime() + (xAuthSettings.banLength * 1000));
		if (unbanTime.compareTo(Util.getNow()) > 0) // still banned
			return true;
		else // no longer banned, remove from database
			DbUtil.deleteStrikeBan(ban);

		return false;
	}

	public void reload() {
		xAuthSettings.setup();
		xAuthMessages.setup();
	}

	public UUID getGlobalUID() {
		return globalUID;
	}
}