package com.cypherx.xauth;

import org.bukkit.entity.Player;
import org.bukkit.event.block.*;

/**
 * Authenticate block listener
 * @author CypherX
 */
public class xAuthBlockListener extends BlockListener
{
    private final xAuth plugin;

    public xAuthBlockListener(final xAuth plugin)
    {
        this.plugin = plugin;
    }

    //Prevents players from breaking blocks
    public void onBlockBreak(BlockBreakEvent event)
    {
    	if (event.isCancelled())
			return;

    	Player player = event.getPlayer();
    	plugin.handleEvent(player, event);
    }

	//Prevents player from damaging a block
    /*public void onBlockDamage(BlockDamageEvent event)
    {
    	Player player = event.getPlayer();
    	plugin.handleEvent(player, event);
    }*/

	//Prevents player from using switches, buttons, etc.
    public void onBlockInteract(BlockInteractEvent event)
    {
    	if (event.isCancelled())
			return;

		if (event.isPlayer())
		{
			Player player = (Player)event.getEntity();
			plugin.handleEvent(player, event);
		}
    }

	//Prevents player from placing blocks
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		plugin.handleEvent(player, event);
	}
}