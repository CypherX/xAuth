/*
 * xAuth for Bukkit
 * Copyright (C) 2012 Lycano <https://github.com/lycano/xAuth/>
 *
 * Modifyworld - PermissionsEx ruleset plugin for Bukkit
 * Copyright (C) 2011 t3hk0d3 http://www.tehkode.ru
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
package de.luricos.bukkit.xAuth.restrictions;

import org.bukkit.entity.*;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a modified version borrowed from t3hk0d3's Modifyworld plugin.
 *
 * @author t3hk0d3
 * @modifiedby lycano
 */
public enum PlayerRestrictionCategory {
    PLAYER("player", Player.class),
    ITEM("item", Item.class),
    ANIMAL("animal", Animals.class, Squid.class),
    MONSTER("monster", Monster.class, Slime.class, EnderDragon.class, Ghast.class ),
    NPC("npc", NPC.class),
    PROJECTILE("projectile", Projectile.class);

    private String name;
    private Class<? extends Entity> classes[];

    private final static Map<Class<? extends Entity>, PlayerRestrictionCategory> map = new HashMap<Class<? extends Entity>, PlayerRestrictionCategory>();

    static {
        for (PlayerRestrictionCategory cat : PlayerRestrictionCategory.values()) {
            for (Class<? extends Entity> catClass : cat.getClasses()) {
                map.put(catClass, cat);
            }
        }
    }

    private PlayerRestrictionCategory(String name, Class<? extends Entity>... classes) {
        this.name = name;
        this.classes = classes;
    }

    public String getName() {
        return this.name;
    }

    public String getNameDot() {
        return this.getName() + ".";
    }

    public Class<? extends Entity>[] getClasses() {
        return this.classes;
    }

    public static PlayerRestrictionCategory fromEntity(Entity entity) {
        for (Class<? extends Entity> entityClass : map.keySet()) {
            if (entityClass.isAssignableFrom(entity.getClass())) {
                return map.get(entityClass);
            }
        }

        return null;
    }
}
