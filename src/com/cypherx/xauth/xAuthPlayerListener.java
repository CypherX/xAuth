package com.cypherx.xauth;

import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.PlayerInventory;
import org.bukkit.ChatColor;

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

    public void onPlayerJoin(PlayerEvent event)
    {
    	//PlayerInventory inv;
		//ItemStack[] is;
    	Player player = event.getPlayer();

    	if (!plugin.isLoggedIn(player))
    	{
    		/*System.out.println("[xAuth] :: Join - Before saving inventory");

    		inv = event.getPlayer().getInventory();
    		is = inv.getContents();
    		for (ItemStack item : is)
    			System.out.println(item.getType() + " : " + item.getAmount());*/

    		plugin.saveInventory(player);

    		/*System.out.println("[xAuth] :: Join - After saving inventory");

    		inv = event.getPlayer().getInventory();
    		is = inv.getContents();
    		for (ItemStack item : is)
    			System.out.println(item.getType() + " : " + item.getAmount());*/

    		if (!plugin.isRegistered(player.getName()))
    			player.sendMessage(ChatColor.RED + "You are not registered. Please register using /register <password>.");
    		else
    			player.sendMessage(ChatColor.RED + "Please log in using /login <password>.");
    	}

    	/*if (!plugin.isRegistered(player.getName()))
    	{
    		plugin.saveInventory(player);
			player.sendMessage(ChatColor.RED + "You are not registered. Please register using /register <password>.");
    	}
    	else if (!plugin.isLoggedIn(player))
    	{
    		plugin.saveInventory(player);
			player.sendMessage(ChatColor.RED + "Please log in using /login <password>.");
    	}*/
    }

	public void onPlayerQuit(PlayerEvent event)
	{
		/*PlayerInventory inv;
		ItemStack[] is;

		System.out.println("[xAuth] :: Quit - Before restoring inventory");

		inv = event.getPlayer().getInventory();
		is = inv.getContents();
		for (ItemStack item : is)
			System.out.println(item.getType() + " : " + item.getAmount());*/

		plugin.logout(event.getPlayer());

		/*System.out.println("[xAuth] :: Quit - After restoring inventory");
		inv = event.getPlayer().getInventory();
		is = inv.getContents();
		for (ItemStack item : is)
			System.out.println(item.getType() + " : " + item.getAmount());*/
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

	//Prevents player from using an item such as food
	public void onPlayerItem(PlayerItemEvent event)
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

		Player player = event.getPlayer();
		plugin.handleEvent(player, event);
		if (event.isCancelled())
			player.teleportTo(event.getFrom());
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