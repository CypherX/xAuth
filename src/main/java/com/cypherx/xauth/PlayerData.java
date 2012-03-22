package com.cypherx.xauth;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerData {
	private ItemStack[] items = null;
	private ItemStack[] armor = null;
	private Location location = null;
	private Collection<PotionEffect> potEffects;

	public PlayerData(ItemStack[] items, ItemStack[] armor, Location location, Collection<PotionEffect> potEffects) {
		this.items = items;
		this.armor = armor;
		this.location = location;
		this.potEffects = potEffects;
	}

	public ItemStack[] getItems() { return items; }
	public ItemStack[] getArmor () { return armor; }
	public Location getLocation() { return location; }
	public Collection<PotionEffect> getPotionEffects() { return potEffects; }
}