/*
 * xAuth for Bukkit
 * Copyright (C) 2012 Lycano <https://github.com/lycano/xAuth/>
 *
 * Copyright (C) 2011 CypherX <https://github.com/CypherX/xAuth/>
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
package de.luricos.bukkit.xAuth;

import de.luricos.bukkit.xAuth.database.Table;
import de.luricos.bukkit.xAuth.exceptions.xAuthPlayerDataException;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class PlayerDataHandler {
    private final xAuth plugin;

    public PlayerDataHandler(final xAuth plugin) {
        this.plugin = plugin;
    }

    public void storeData(xAuthPlayer xp, Player p) {
        PlayerInventory pInv = p.getInventory();
        ItemStack[] items = pInv.getContents();
        ItemStack[] armor = pInv.getArmorContents();
        Location loc = p.isDead() ? null : p.getLocation();
        Collection<PotionEffect> potEffects = p.getActivePotionEffects();
        int fireTicks = p.isDead() ? 0 : p.getFireTicks();
        int remainingAir = p.getRemainingAir();
        GameMode gameMode = p.getGameMode();

        String strItems = null;
        String strArmor = null;
        String strLoc = null;
        String strPotFx = buildPotFxString(potEffects);
        xp.setPlayerData(new PlayerData(items, armor, loc, potEffects, fireTicks, remainingAir, gameMode));

        boolean hideInv = plugin.getConfig().getBoolean("guest.hide-inventory");
        boolean hideLoc = plugin.getConfig().getBoolean("guest.protect-location");

        if (hideInv) {
            strItems = buildItemString(items);
            strArmor = buildItemString(armor);

            pInv.clear();
            pInv.setHelmet(null);
            pInv.setChestplate(null);
            pInv.setLeggings(null);
            pInv.setBoots(null);
        }

        if (hideLoc && !p.isDead()) {
            strLoc = loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch();
            p.teleport(plugin.getLocationManager().getLocation(p.getWorld()));
        }

        for (PotionEffect effect : p.getActivePotionEffects())
            p.addPotionEffect(new PotionEffect(effect.getType(), 0, 0), true);

        p.setFireTicks(0);
        p.setRemainingAir(300);

        // only store player data if there's something to insert
        if (strItems != null || strArmor != null || strLoc != null || strPotFx != null) {
            Connection conn = plugin.getDatabaseController().getConnection();
            PreparedStatement ps = null;

            try {
                String sql;
                if (plugin.getDatabaseController().isMySQL())
                    sql = String.format("INSERT IGNORE INTO `%s` VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                            plugin.getDatabaseController().getTable(Table.PLAYERDATA));
                else
                    sql = String.format("INSERT INTO `%s` SELECT ?, ?, ?, ?, ?, ?, ?, ? FROM DUAL WHERE NOT EXISTS (SELECT * FROM `%s` WHERE `playername` = ?)",
                            plugin.getDatabaseController().getTable(Table.PLAYERDATA), plugin.getDatabaseController().getTable(Table.PLAYERDATA));

                ps = conn.prepareStatement(sql);
                ps.setString(1, p.getName());
                ps.setString(2, strItems);
                ps.setString(3, strArmor);
                ps.setString(4, strLoc);
                ps.setString(5, strPotFx);
                ps.setInt(6, fireTicks);
                ps.setInt(7, remainingAir);
                ps.setString(8, gameMode.name());
                if (!plugin.getDatabaseController().isMySQL())
                    ps.setString(9, p.getName());
                ps.executeUpdate();
            } catch (SQLException e) {
                xAuthLog.severe("Failed to insert player data into database!", e);
            } finally {
                plugin.getDatabaseController().close(conn, ps);
            }
        }
    }

    private String buildItemString(ItemStack[] items) {
        StringBuilder sbItems = new StringBuilder();
        StringBuilder sbAmount = new StringBuilder();
        StringBuilder sbDurability = new StringBuilder();
        StringBuilder sbEnchants = new StringBuilder();

        for (ItemStack item : items) {
            int itemId = 0;
            int amount = 0;
            short durability = 0;
            Map<Enchantment, Integer> enchants = null;

            if (item != null) {
                itemId = item.getTypeId();
                amount = item.getAmount();
                durability = item.getDurability();
                enchants = item.getEnchantments();

                if (!enchants.keySet().isEmpty()) {
                    for (Enchantment enchant : enchants.keySet()) {
                        int id = enchant.getId();
                        int level = enchants.get(enchant);
                        sbEnchants.append(id + ":" + level + "-");
                    }

                    sbEnchants.deleteCharAt(sbEnchants.lastIndexOf("-"));
                }
            }

            sbItems.append(itemId).append(",");
            sbAmount.append(amount).append(",");
            sbDurability.append(durability).append(",");
            sbEnchants.append(",");
        }

        sbItems.deleteCharAt(sbItems.lastIndexOf(","));
        sbAmount.deleteCharAt(sbAmount.lastIndexOf(","));
        sbDurability.deleteCharAt(sbDurability.lastIndexOf(","));
        sbEnchants.deleteCharAt(sbEnchants.lastIndexOf(","));
        return sbItems.append(";").append(sbAmount).append(";").append(sbDurability).append(";").append(sbEnchants).toString();
    }

    private String buildPotFxString(Collection<PotionEffect> effects) {
        if (effects.size() < 1)
            return null;

        StringBuilder sbType = new StringBuilder();
        StringBuilder sbDur = new StringBuilder();
        StringBuilder sbAmp = new StringBuilder();

        for (PotionEffect effect : effects) {
            sbType.append(effect.getType().getId()).append(",");
            sbDur.append(effect.getDuration()).append(",");
            sbAmp.append(effect.getAmplifier()).append(",");
        }

        sbType.deleteCharAt(sbType.lastIndexOf(","));
        sbDur.deleteCharAt(sbDur.lastIndexOf(","));
        sbAmp.deleteCharAt(sbAmp.lastIndexOf(","));
        return sbType.append(";").append(sbDur).append(";").append(sbAmp).toString();
    }

    private ItemStack[] buildItemStack(String str) {
        ItemStack[] items = null;
        String[] itemSplit = str.split(";");
        String[] itemid = itemSplit[0].split(",");
        String[] amount = itemSplit[1].split(",");
        String[] durability = itemSplit[2].split(",");
        String[] enchants = itemSplit[3].split(",", -1);
        items = new ItemStack[itemid.length];

        for (int i = 0; i < items.length; i++) {
            items[i] = new ItemStack(Integer.parseInt(itemid[i]), Integer.parseInt(amount[i]), Short.parseShort(durability[i]));

            if (!enchants[i].isEmpty()) {
                String[] itemEnchants = enchants[i].split("-");
                for (String enchant : itemEnchants) {
                    String[] enchantSplit = enchant.split(":");
                    int id = Integer.parseInt(enchantSplit[0]);
                    int level = Integer.parseInt(enchantSplit[1]);
                    Enchantment e = new EnchantmentWrapper(id);
                    items[i].addUnsafeEnchantment(e, level);
                }
            }
        }

        return items;
    }

    private Collection<PotionEffect> buildPotFx(String str) {
        String[] effectSplit = str.split(";");
        String[] type = effectSplit[0].split(",");
        String[] duration = effectSplit[1].split(",");
        String[] amplifier = effectSplit[2].split(",");
        Collection<PotionEffect> effects = new ArrayList<PotionEffect>();

        for (int i = 0; i < type.length; i++) {
            PotionEffectType potFxType = PotionEffectType.getById(Integer.parseInt(type[i]));
            int potFxDur = Integer.parseInt(duration[i]);
            int potFxAmp = Integer.parseInt(amplifier[i]);
            effects.add(new PotionEffect(potFxType, potFxDur, potFxAmp));
        }

        return effects;
    }

    public void restoreData(xAuthPlayer xp, Player p) {
        restoreData(xp, p.getName());
    }

    //@TODO refactor
    public void restoreData(xAuthPlayer xp, String playerName) {
        ItemStack[] items = null;
        ItemStack[] armor = null;
        Location loc = null;
        Collection<PotionEffect> potFx = null;
        int fireTicks = 0;
        int remainingAir = 300;
        GameMode gameMode = xp.getGameMode();

        // Use cached copy of player data, if it exists
        PlayerData playerData = xp.getPlayerData();
        if (playerData != null) {
            items = playerData.getItems();
            armor = playerData.getArmor();
            loc = playerData.getLocation();
            potFx = playerData.getPotionEffects();
            fireTicks = playerData.getFireTicks();
            remainingAir = playerData.getRemainingAir();
            gameMode = playerData.getGameMode();
        } else {
            Connection conn = plugin.getDatabaseController().getConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                String sql = String.format("SELECT * FROM `%s` WHERE `playername` = ?",
                        plugin.getDatabaseController().getTable(Table.PLAYERDATA));
                ps = conn.prepareStatement(sql);
                ps.setString(1, playerName);
                rs = ps.executeQuery();

                if (rs.next()) {
                    items = buildItemStack(rs.getString("items"));
                    armor = buildItemStack(rs.getString("armor"));

                    String rsLoc = rs.getString("location");
                    if (rsLoc != null) {
                        String[] locSplit = rsLoc.split(":");
                        loc = new Location(Bukkit.getWorld(locSplit[0]), Double.parseDouble(locSplit[1]), Double.parseDouble(locSplit[2]),
                                Double.parseDouble(locSplit[3]), Float.parseFloat(locSplit[4]), Float.parseFloat(locSplit[5]));
                    }

                    String rsPotFx = rs.getString("potioneffects");
                    if (rsPotFx != null)
                        potFx = buildPotFx(rsPotFx);

                    fireTicks = rs.getInt("fireticks");
                    remainingAir = rs.getInt("remainingair");
                    gameMode = GameMode.valueOf(rs.getString("gamemode"));
                }
            } catch (SQLException e) {
                xAuthLog.severe("Failed to load playerdata from database for player: " + playerName, e);
            } finally {
                plugin.getDatabaseController().close(conn, ps, rs);
            }
        }

        Player player = xp.getPlayer();
        try {
            // @TODO remove after next 1.3.1 RB as this is a bug
            if (player == null) {
                throw new xAuthPlayerDataException("Error during restoreData execution. Can't restore player inventory.");
            }

            PlayerInventory pInv = player.getInventory();
            boolean hideInv = plugin.getConfig().getBoolean("guest.hide-inventory");
            boolean hideLoc = plugin.getConfig().getBoolean("guest.protect-location");

            if (hideInv && items != null) {
                // Fix for inventory extension plugins
                if (pInv.getSize() > items.length) {
                    ItemStack[] newItems = new ItemStack[pInv.getSize()];
                    System.arraycopy(items, 0, newItems, 0, items.length);

                    items = newItems;
                }
                //End Fix for inventory extension plugins

                pInv.setContents(items);
            }

            if (hideInv && armor != null)
                pInv.setArmorContents(armor);

            if (hideLoc && loc != null && player.getHealth() > 0)
                player.teleport(loc);

            if (potFx != null)
                player.addPotionEffects(potFx);

            player.setFireTicks(fireTicks);
            player.setRemainingAir(remainingAir);
            player.setGameMode(gameMode);
        } catch (xAuthPlayerDataException e) {
            xAuthLog.severe(e.getMessage());
        }

        // reset playerData
        xp.setPlayerData(null);

        // release playerdata from database
        Connection conn = plugin.getDatabaseController().getConnection();
        PreparedStatement ps = null;
        try {
            String sql = String.format("DELETE FROM `%s` WHERE `playername` = ?",
                    plugin.getDatabaseController().getTable(Table.PLAYERDATA));
            ps = conn.prepareStatement(sql);
            ps.setString(1, playerName);
            ps.executeUpdate();
        } catch (SQLException e) {
            xAuthLog.severe("Could not delete playerdata record from database for player: " + playerName, e);
        } finally {
            plugin.getDatabaseController().close(conn, ps);
        }
    }
}