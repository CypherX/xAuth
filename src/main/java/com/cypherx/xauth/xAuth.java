//xAuth 1.2.2
//Built against Bukkit #653, CraftBukkit #674, and Permissions v2.7

package com.cypherx.xauth;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.server.PropertyManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * xAuth for Bukkit
 *
 * @author CypherX
 */
public class xAuth extends JavaPlugin
{
	private final xAuthPlayerListener playerListener = new xAuthPlayerListener(this);
	private final xAuthBlockListener blockListener = new xAuthBlockListener(this);
	private final xAuthEntityListener entityListener = new xAuthEntityListener(this);
	private static PluginDescriptionFile pdfFile;

	private static final String DIR = "plugins" + File.separator + "xAuth" + File.separator;
	private static final String CONFIG_FILE = "config.yml";
	private static final String STRINGS_FILE = "strings.yml";
	private static final String AUTH_FILE = "auths.txt";

	public static Settings settings;
	public static Strings strings;
	public static PermissionHandler Permissions;

	//autosave test code
	private static Boolean fullyEnabled;

	private ConcurrentHashMap<String, String> auths = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<Player, ItemStack[]> inventory = new ConcurrentHashMap<Player, ItemStack[]>();
	private ConcurrentHashMap<Player, ItemStack[]> armor = new ConcurrentHashMap<Player, ItemStack[]>();
	private ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<String, Session>();
	private ConcurrentHashMap<Player, Date> lastNotifyTimes = new ConcurrentHashMap<Player, Date>();
	private ConcurrentHashMap<String, Integer> strikes = new ConcurrentHashMap<String, Integer>();
	private ArrayList<String> illegalNames = new ArrayList<String>();

	public void onEnable()
	{
		fullyEnabled = false;
		pdfFile = this.getDescription();

		PropertyManager props = new PropertyManager(new File("server.properties"));
		if (props.a("online-mode", true))
		{
			System.out.println("[" + pdfFile.getName() + "] Stopping - Server is running in online-mode");
			this.setEnabled(false);
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		File fDir = new File(DIR);

		if (!fDir.exists())
			fDir.mkdir();

		try
		{
			File fAuths = new File(DIR + AUTH_FILE);
	
			if (!fAuths.exists())
				fAuths.createNewFile();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}

		settings = new Settings(new File(DIR + CONFIG_FILE));
		strings = new Strings(new File(DIR + STRINGS_FILE));
		getAuths();
		setupPermissions();

		//Hide inventory of any players online while server is starting (means /reload was used)
		Player[] players = getServer().getOnlinePlayers();
		if (players.length > 0)
		{
			for (Player player : players)
			{
				if (isRegistered(player.getName()))
				{
					saveInventory(player);
					player.sendMessage(strings.getString("misc.reloaded"));
				}
			}
		}

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Lowest, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Event.Priority.Lowest, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Event.Priority.Lowest, this);
		pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, Event.Priority.Lowest, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Lowest, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Lowest, this);
		pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Event.Priority.Lowest, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Lowest, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Lowest, this);

		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Lowest, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Lowest, this);

		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Lowest, this);
		pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, Priority.Lowest, this);

		System.out.println("[" + pdfFile.getName() + "]" + " v" + pdfFile.getVersion() + " Enabled!");

		//autosave stuff
		fullyEnabled = true;
	}

	public void getAuths()
	{
		System.out.println("[" + pdfFile.getName() + "] Loading player accounts..");

		try
		{
			BufferedReader authReader = new BufferedReader(new FileReader(DIR + AUTH_FILE));
	
			String line;
			while ((line = authReader.readLine()) != null)
			{
				String[] split = line.split(":");
				auths.put(split[0], line);
			}
			authReader.close();
			System.out.print("[" + pdfFile.getName() + "] Done! Loaded " + auths.size() + " Accounts!");
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	public void onDisable()
	{
		getServer().getScheduler().cancelAllTasks();

		//Restore players inventories so they are not lost
		Player[] players = getServer().getOnlinePlayers();
		if (players.length > 0)
		{
			for (Player player : players)
				if (!sessionExists(player.getName()))
					restoreInventory(player);
		}

		if (fullyEnabled)
			updateAuthFile();

		System.out.println("[" + pdfFile.getName() + "]" + " v" + pdfFile.getVersion() + " Disabled");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if (!this.isEnabled())
			return false;

		CommandHandler cmdHandler = new CommandHandler(this);
	
		if (sender instanceof Player)
			cmdHandler.handlePlayerCommand((Player)sender, cmd, args);
		else if (sender instanceof ConsoleCommandSender)
			cmdHandler.handleConsoleCommand(cmd, args);

		return true;
	}
	
	//AUTH / REGISTER FUNCTIONS
	public void addAuth(String pName, String pass)
	{
		String hash = whirlpool(pass);
		auths.put(pName.toLowerCase(), pName.toLowerCase() + ":" + hash);
	
		if (settings.getBool("misc.autosave"))
			updateAuthFile();
	}
	
	public Boolean isRegistered(String pName)
	{
		if (auths.containsKey(pName.toLowerCase()))
			return true;
	
		return false;
	}
	
	public void changePass(String pName, String pass)
	{
		String hash = whirlpool(pass);
	
		auths.remove(pName.toLowerCase());
		auths.put(pName.toLowerCase(), pName.toLowerCase() + ":" + hash);
	
		if (settings.getBool("misc.autosave"))
			updateAuthFile();
	}
	
	public void removeAuth(String pName)
	{
		pName = pName.toLowerCase();
		auths.remove(pName);
		
		if (sessionExists(pName))
			removeSession(pName);
	
		if (settings.getBool("misc.autosave"))
			updateAuthFile();
	}
	
	public void updateAuthFile()
	{
		try
		{
			BufferedWriter authWriter = new BufferedWriter(new FileWriter(DIR + AUTH_FILE));
			for (String key: auths.keySet())
			{
				authWriter.write(auths.get(key));
				authWriter.newLine();
			}
			authWriter.close();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	public Boolean isValidPass(String pass)
	{
		if (!settings.getBool("password.complexity.enabled"))
		{
			if (pass.length() < settings.getInt("password.min-length"))
				return false;

			return true;
		}

		String pattern = "(";

		if (settings.getBool("password.complexity.numbers"))
			pattern += "(?=.*\\d)";

		if (settings.getBool("password.complexity.lowercase"))
			pattern += "(?=.*[a-z])";

		if (settings.getBool("password.complexity.uppercase"))
			pattern += "(?=.*[A-Z])";

		if (settings.getBool("password.complexity.symbols"))
			pattern += "(?=.*\\W)";

		pattern += ".{" + settings.getInt("password.min-length") + ",})";

		//String pattern = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*\\W).{3,})";
		Pattern p = Pattern.compile(pattern);
		Matcher matcher = p.matcher(pass);
		return matcher.matches();
	}
	
	//LOGIN / LOGOUT FUNCTIONS
	public void login(Player player)
	{
		startSession(player);
		restoreInventory(player);
	}
	
	public Boolean checkPass(Player player, String pass)
	{
		String account = auths.get(player.getName().toLowerCase());
		int md5Length = player.getName().length() + 33;
		String hash;

		if (account.length() == md5Length)
			hash = md5(pass);
		else
			hash = whirlpool(pass);			
		
		if (account.equals(player.getName().toLowerCase() + ":" + hash))
		{
			//change pass to whirlpool if md5
			if (hash.length() == 32)
				changePass(player.getName(), pass);

			return true;
		}
		else
			return false;
	}
	
	public void logout(Player player)
	{
		String pName = player.getName();

		if (sessionExists(pName))
		{
			Session session = sessions.get(pName.toLowerCase());

			if (session.isExpired(new Date(session.getLoginTime() + (settings.getInt("session.timeout") * 1000))))
				removeSession(pName);
		}
		else
			restoreInventory(player);
	}

	public void addStrike(Player player)
	{
		String addr = player.getAddress().getAddress().getHostAddress();
		int newCount = 1;
		if (strikes.containsKey(addr))
		{
			newCount = strikes.get(addr) + 1;
			strikes.remove(addr);
		}
		
		strikes.put(addr, newCount);
	}

	public int getStrikes(Player player)
	{
		String addr = player.getAddress().getAddress().getHostAddress();

		if (!strikes.containsKey(addr))
			return 0;

		return strikes.get(addr);
	}

	public void clearStrikes(Player player)
	{
		String addr = player.getAddress().getAddress().getHostAddress();
		strikes.remove(addr);
	}
	
	//NOTIFY FUNCTIONS
	public void handleEvent(Player player, Cancellable event)
	{
		if (!isRegistered(player.getName()) && !mustRegister(player))
			return;

		if (!sessionExists(player.getName()))
		{
			event.setCancelled(true);

			if (canNotify(player))
				notifyPlayer(player);
		}
	}
	
	public Boolean isCmdAllowed(String cmd)
	{
		if (settings.getStrList("misc.allowed-cmds").contains(cmd))
			return true;

		return false;
	}
	
	public Boolean canNotify(Player player)
	{
		if (lastNotifyTimes.get(player) == null)
			return true;
	
		Date nextNotifyTime = new Date(lastNotifyTimes.get(player).getTime() + (settings.getInt("notify.limit") * 1000));
		if (nextNotifyTime.compareTo(new Date()) < 0)
			return true;
	
		return false;
	}
	
	public void notifyPlayer(Player player)
	{
		player.sendMessage(strings.getString("misc.illegal"));
		updateNotifyTime(player, new Date());
	}
	
	public void updateNotifyTime(Player player, Date date)
	{
		lastNotifyTimes.remove(player);
		lastNotifyTimes.put(player, date);
	}
	
	//INVENTORY FUNCTIONS
	public void saveInventory(Player player)
	{
		PlayerInventory playerInv = player.getInventory();
		inventory.put(player, playerInv.getContents());
		playerInv.clear();
		armor.put(player, playerInv.getArmorContents());
		playerInv.setHelmet(null);
		playerInv.setChestplate(null);
		playerInv.setLeggings(null);
		playerInv.setBoots(null);
	}
	
	public void restoreInventory(Player player)
	{
		PlayerInventory playerInv = player.getInventory();
	
		if (inventory.containsKey(player))
		{
			playerInv.setContents(inventory.get(player));
			inventory.remove(player);
		}
	
		if (armor.containsKey(player))
		{
			playerInv.setBoots(armor.get(player)[0].getTypeId() == 0 ? null : armor.get(player)[0]);
			playerInv.setLeggings(armor.get(player)[1].getTypeId() == 0 ? null : armor.get(player)[1]);
			playerInv.setChestplate(armor.get(player)[2].getTypeId() == 0 ? null : armor.get(player)[2]);
			playerInv.setHelmet(armor.get(player)[3].getTypeId() == 0 ? null : armor.get(player)[3]);
			armor.remove(player);
		}

		player.saveData();
	}
	
	//SESSION FUNCTIONS
	public void startSession(Player player)
	{
		sessions.put(player.getName().toLowerCase(), new Session(player));
	}
	
	public Boolean sessionExists(String pName)
	{
		if (sessions.containsKey(pName.toLowerCase()))
			return true;
	
		return false;
	}

	public Boolean isLoggedIn(Player player)
	{
		if (sessionExists(player.getName()))
		{
			if (isSessionValid(player))
				return true;
	
			removeSession(player.getName());
		}

		return false;
	}

	public Boolean isSessionValid(Player player)
	{
		Session session = sessions.get(player.getName().toLowerCase());
		if (session.isExpired(new Date(session.getLoginTime() + (settings.getInt("session.timeout") * 1000))))
			return false;

		if (settings.getBool("session.verifyip") && !session.isValidAddr(player.getAddress().getAddress().getHostAddress()))
			return false;

		return true;
	}

	public void removeSession(String pName)
	{
		pName = pName.toLowerCase();
		if (sessionExists(pName))
			sessions.remove(pName);
	}

	public void killSession(Player player)
	{
		String pName = player.getName();
		removeSession(pName);

		if (player != null)
		{
			saveInventory(player);
			player.sendMessage(strings.getString("logout.success.ended"));
		}
	}
	
	//MISC FUNCTIONS
	private void setupPermissions()
	{
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
		
		if (xAuth.Permissions == null)
	    {
			if (test != null)
				xAuth.Permissions = ((Permissions)test).getHandler();
	        else
	        	System.out.println("[" + pdfFile.getName() + "] Permissions plugin not detected, defaulting to ops.txt");
	    }
	}

	public String md5(String str)
	{
		try
		{
	        MessageDigest md = MessageDigest.getInstance("MD5");
	        byte[] messageDigest = md.digest(str.getBytes());
	        BigInteger number = new BigInteger(1, messageDigest);
	        String hashtext = number.toString(16);
	        while (hashtext.length() < 32)
	            hashtext = "0" + hashtext;
	
	        return hashtext;
	    }
	    catch (Exception e)
	    {
	        System.out.println(e.getMessage());
	    }
	
		return null;
	}

	public String whirlpool(String str)
	{
		Whirlpool w = new Whirlpool();
		byte[] digest = new byte[Whirlpool.DIGESTBYTES];
		w.NESSIEinit();
		w.NESSIEadd(str);
		w.NESSIEfinalize(digest);
		return Whirlpool.display(digest);
	}
	
	public boolean canUseCommand(Player player, String node)
	{
		if (xAuth.Permissions == null)
		{
			if (!player.isOp())
				return false;
	
			return true;
		}
	
		if (!xAuth.Permissions.has(player, node))
			return false;
	
		return true;
	}

	public Boolean isNameLegal(String pName)
	{
		pName = pName.toLowerCase();

		if (illegalNames.contains(pName))
			return false;

		String allowed = settings.getStr("filter.allowed");

		for(int i = 0; i < pName.length(); i++)
			if (allowed.indexOf(pName.charAt(i)) == -1)
			{
				illegalNames.add(pName);
				return false;
			}

		return true;
	}

	public void reload()
	{
		updateAuthFile();
		settings = new Settings(new File(DIR + CONFIG_FILE));
		strings = new Strings(new File(DIR + STRINGS_FILE));
		getAuths();
		System.out.println("[" + pdfFile.getName() + "] Configuration & Accounts reloaded");
	}

	public Boolean mustRegister(Player player)
	{
		if (!settings.getBool("registration.forced"))
			return false;

		if (xAuth.Permissions != null)
			if (xAuth.Permissions.has(player, "xauth.exclude"))
				return false;

		return true;
	}
}