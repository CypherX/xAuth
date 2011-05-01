package com.cypherx.xauth;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;

/**
 * Handle events for all Player related events
 * @author CypherX
 */
public class xAuthPlayerListener extends PlayerListener
{
    private final xAuth plugin;

    public xAuthPlayerListener(final xAuth instance)
    {
        plugin = instance;
    }

    public void onPlayerLogin(PlayerLoginEvent event)
    {
    	Player player = event.getPlayer();

    	if (player.isOnline())
    		event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "A player with this name is already online.");

    	if (xAuth.settings.getBool("filter.enabled") && !plugin.isNameLegal(player.getName()))
    		event.disallow(PlayerLoginEvent.Result.KICK_OTHER, xAuth.strings.getString("misc.filterkickmsg"));

    	if (xAuth.settings.getBool("filter.block-blankname") && player.getName().trim().equals(""))
    		event.disallow(PlayerLoginEvent.Result.KICK_OTHER, xAuth.strings.getString("misc.blankkickmsg"));
    }

    public void onPlayerJoin(PlayerJoinEvent event)
    {
    	final Player player = event.getPlayer();

    	if (!plugin.isLoggedIn(player))
    	{
    		plugin.loginLocation.put(player, player.getLocation());
    		player.teleport(player.getWorld().getSpawnLocation());

    		if (!plugin.isRegistered(player.getName()))
    		{
    			if (!plugin.mustRegister(player))
    				return;

    			plugin.saveInventory(player);
    			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
    	            public void run() {
    	            	player.sendMessage(xAuth.strings.getString("register.login"));
    	            }
    	        }, 5);
    		}
    		else
    		{
    			plugin.saveInventory(player);
    			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
    	            public void run() {
    	            	player.sendMessage(xAuth.strings.getString("login.login"));
    	            }
    	        }, 5);
    		}
    	}
    }

	public void onPlayerQuit(PlayerQuitEvent event)
	{
		plugin.logout(event.getPlayer());
	}

	//Prevents players from executing commands
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
	{
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		String[] msg = event.getMessage().split(" ");

		if (!plugin.isCmdAllowed(msg[0]))
			plugin.handleEvent(player, event);

		if (event.isCancelled())
			event.setMessage("/");
	}

	//Prevents player from being able to chat
	public void onPlayerChat(PlayerChatEvent event)
	{
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		plugin.handleEvent(player, event);
	}

	//Prevents player from being able to drop an item (inventory should be empty anyway)
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		plugin.handleEvent(player, event);
	}

	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		plugin.handleEvent(player, event);
	}

	//Prevents player from moving
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (event.isCancelled())
			return;

		/*Location from = event.getFrom();
		Location to = event.getTo();

		if (from.getX() == to.getX() && from.getZ() == to.getZ())
		{
			if (from.getY() > to.getY())
				return;
		}*/

		Player player = event.getPlayer();
		plugin.handleEvent(player, event);

		Location loc = player.getWorld().getSpawnLocation();

		if (event.isCancelled() && player.teleport(loc)) {
			event.setTo(loc);
			event.setFrom(loc);
		}
	}

	//Prevents player from picking up items
	public void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		plugin.handleEvent(player, event);
	}
}