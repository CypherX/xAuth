package com.cypherx.xauth;

import com.cypherx.xauth.commands.*;
import com.cypherx.xauth.datamanager.*;
import com.cypherx.xauth.listeners.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class xAuth extends JavaPlugin {
	public static PluginDescriptionFile desc;
	public static File dataFolder;
	private DataManager dataManager;

	public void onDisable() {
		Player[] players = getServer().getOnlinePlayers();
		if (players.length > 0) {
			for (Player player : players) {
				xAuthPlayer xPlayer = dataManager.getPlayer(player.getName());
				if (xPlayer.isGuest())
					removeGuest(xPlayer);
			}
		}

		if (dataManager != null)
			dataManager.close();

		xAuthSettings.saveChanges();
		xAuthLog.info("v" + desc.getVersion() + " Disabled!");
	}

	public void onEnable() {
		desc = getDescription();
		dataFolder = getDataFolder();

		if (!dataFolder.exists())
			dataFolder.mkdirs();

		xAuthSettings.setup(dataFolder);
		xAuthMessages.setup(dataFolder);

		if (xAuthSettings.autoDisable && getServer().getOnlineMode()) {
			xAuthLog.warning("Disabling - Server is running in online-mode");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		xAuthPermissions.setup(this);
		xAuthHelp.setup(this);

		dataManager = new DataManager();
		if (!dataManager.isConnected()) {
			xAuthLog.severe("Disabling - No connection to database");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		dataManager.runStartupTasks();

		File oldAuthFile = new File(dataFolder, "auths.txt");
		if (oldAuthFile.exists())
			importAccounts(oldAuthFile);

		dataManager.printStats();

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

		dataManager.insertAccounts(accounts);

		if (oldAuthFile.renameTo(new File(dataFolder, "auths.txt.old")))
			xAuthLog.info("Import complete! auths.txt renamed to auths.txt.old");
		else
			xAuthLog.info("Import complete! Verify that all accounts were imported then remove/rename auths.txt");
	}

	private void handleReload(Player[] players) {
		for (Player player : players) {
			xAuthPlayer xPlayer = dataManager.getPlayerJoin(player.getName());
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
			dataManager.deleteSession(xPlayer);

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

	public void protect(xAuthPlayer xPlayer) {
		Player player = xPlayer.getPlayer();
		PlayerInventory playerInv = player.getInventory();

		dataManager.insertInventory(xPlayer);
		playerInv.clear();
		playerInv.setHelmet(null);
		playerInv.setChestplate(null);
		playerInv.setLeggings(null);
		playerInv.setBoots(null);
		player.saveData();

		if (player.getHealth() > 0)
			xPlayer.setLocation(player.getLocation());

		if (xAuthSettings.protectLoc)
			player.teleport(getLocationToTeleport(player.getWorld()));
	}

	public void restore(xAuthPlayer xPlayer) {
		Player player = xPlayer.getPlayer();
		PlayerInventory playerInv = player.getInventory();

		ItemStack[] inv = dataManager.getInventory(xPlayer);
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
		dataManager.deleteInventory(xPlayer);

		if (xPlayer.getLocation() != null)
			xPlayer.getPlayer().teleport(xPlayer.getLocation());
		player.saveData();
	}

	public void login(xAuthPlayer xPlayer) {
		Account account = xPlayer.getAccount();
		account.setLastLoginDate(Util.getNow());
		account.setLastLoginHost(Util.getHostFromPlayer(xPlayer.getPlayer()));
		dataManager.saveAccount(account);

		Session session = new Session(account.getId(), account.getLastLoginHost());
		xPlayer.setSession(session);
		dataManager.insertSession(session);

		removeGuest(xPlayer);
		xPlayer.setStrikes(0);
	}

	public void changePassword(Account account, String newPass) {
		account.setPassword(Util.encrypt(newPass));
		dataManager.saveAccount(account);
	}

	public boolean checkPassword(Account account, String checkPass) {
		if(xAuthSettings.authURLEnabled){
			StringBuilder sb = new StringBuilder();

			boolean result = checkAuthURLPass(account.getPlayerName(), checkPass, sb);
			// if true, tell whole server a player logged in
			// else, send the returned string (error message) to the user
			if(result)
				Bukkit.getServer().broadcastMessage("Player '" + account.getPlayerName() + "' logged in with forum name '"+sb.toString()+"'");
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

	public Location getLocationToTeleport(World world) {
		TeleLocation teleLocation = dataManager.getTeleLocation(world.getName());
		return (teleLocation == null ? world.getSpawnLocation() : teleLocation.getLocation());
	}

	public void strikeout(xAuthPlayer xPlayer) {
		Player player = xPlayer.getPlayer();

		if (xAuthSettings.strikeAction.equals("banip")) {
			StrikeBan ban = new StrikeBan(Util.getHostFromPlayer(player));
			getDataManager().insertStrikeBan(ban);
			xAuthLog.info(ban.getHost() + " banned by strike system");
		}

		player.kickPlayer(xAuthMessages.get("miscKickStrike", player, null));
		if (xAuthSettings.strikeAction.equals("kick"))
			xAuthLog.info(player.getName() + " kicked by strike system");

		xPlayer.setStrikes(0);
	}

	public boolean isBanned(String host) {
		final StrikeBan ban = dataManager.loadStrikeBan(host);
		if (ban == null)
			return false;

		if (xAuthSettings.banLength == 0)
			return true;

		Timestamp unbanTime = new Timestamp(ban.getBanTime().getTime() + (xAuthSettings.banLength * 1000));
		if (unbanTime.compareTo(Util.getNow()) > 0) // still banned
			return true;
		else // no longer banned, remove from database
			dataManager.deleteStrikeBan(ban);

		return false;
	}

	public void reload() {
		xAuthSettings.setup(dataFolder);
		xAuthMessages.setup(dataFolder);
	}

	public DataManager getDataManager() {
		return dataManager;
	}
}