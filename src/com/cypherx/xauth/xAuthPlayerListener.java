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

    	if (xAuth.settings.getBool("security.filter.enabled") && !plugin.isNameLegal(player.getName()))
    		event.disallow(PlayerLoginEvent.Result.KICK_OTHER, xAuth.strings.getString("misc.filterkickmsg"));

    	if (xAuth.settings.getBool("security.filter.blankname") && player.getName().trim().equals(""))
    		event.disallow(PlayerLoginEvent.Result.KICK_OTHER, xAuth.strings.getString("misc.blankkickmsg"));
    }

    public void onPlayerJoin(PlayerEvent event)
    {
    	Player player = event.getPlayer();

    	if (!plugin.isLoggedIn(player))
    	{
    		plugin.saveInventory(player);

    		if (!plugin.isRegistered(player.getName()))
    			player.sendMessage(xAuth.strings.getString("register.login"));
    		else
    			player.sendMessage(xAuth.strings.getString("login.login"));
    	}
    }

	public void onPlayerQuit(PlayerEvent event)
	{
		plugin.logout(event.getPlayer());
	}

	//Prevents players from executing commands
	public void onPlayerCommandPreprocess(PlayerChatEvent event)
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
		//if (!xAuth.settings.getBool("limit.chat"))
			//return;

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

	//Prevents player from using an item such as food
	public void onPlayerItem(PlayerItemEvent event)
	{
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		plugin.handleEvent(player, event);
	}

	/*public void onPlayerInteract(PlayerInteractEvent event)
	{
		//if (!xAuth.settings.getBool("limit.interact"))
			//return;

		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		plugin.handleEvent(player, event);
	}*/

	//Prevents player from moving
	public void onPlayerMove(PlayerMoveEvent event)
	{
		//if (!xAuth.settings.getBool("limit.movement"))
			//return;

		if (event.isCancelled())
			return;

		Location from = event.getFrom();
		Location to = event.getTo();

		if (from.getX() == to.getX() && from.getZ() == to.getZ())
		{
			if (from.getY() > to.getY())
				return;
		}

		Player player = event.getPlayer();
		plugin.handleEvent(player, event);

		if (event.isCancelled())
			player.teleportTo(event.getFrom());
			//player.teleport(event.getFrom());
	}

	//Prevents player from picking up items
	public void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		//if (!xAuth.settings.getBool("limit.pickup"))
			//return;

		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		plugin.handleEvent(player, event);
	}
}