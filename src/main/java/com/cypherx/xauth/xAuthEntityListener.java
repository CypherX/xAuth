package com.cypherx.xauth;

import org.bukkit.entity.*;
import org.bukkit.event.entity.*;

/**
 * Handle events for all Entity related events
 * @author CypherX
 */
public class xAuthEntityListener extends EntityListener
{
    private final xAuth plugin;

    public xAuthEntityListener(final xAuth plugin)
    {
        this.plugin = plugin;
    }

	//Prevents player from taking damage or giving damage
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();

		//Player taking damage
		if (entity instanceof Player)
			plugin.handleEvent((Player)entity, event);
		//Player dealing damage to other entity
		else if (event instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent edbeEvent = (EntityDamageByEntityEvent)event;
			Entity damager = edbeEvent.getDamager();

			if (damager instanceof Player)
				plugin.handleEvent((Player)damager, event);
		}
	}

	//Prevents monsters from attacking player
	public void onEntityTarget(EntityTargetEvent event)
	{
		if (event.isCancelled())
			return;

		Entity entity = event.getTarget();
		if (entity instanceof Player)
			plugin.handleEvent((Player)entity, event);
	}
}