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
package de.luricos.bukkit.xAuth.restrictions;

import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

/**
 * @author lycano
 */
public class PlayerRestrictionNode {

    protected String[] splitEventName;
    protected String eventName;
    protected String eventAction;

    protected String restrictNode;
    protected String allowNode;

    private String eventType;

    private boolean useMaterialNames = true;

    public PlayerRestrictionNode(final String message) {
        this.splitEventName = message.split("\\.");
        this.eventName = splitEventName[this.splitEventName.length - 1];
        this.splitEventName = eventName.split("(?=\\p{Upper})");

        // first element (0) will always be empty
        this.eventType = this.splitEventName[1].toLowerCase(); //player, block, entity
        this.eventAction = this.splitEventName[2].toLowerCase(); // move, place, target, etc.

        this.restrictNode = String.format("restrict.%s.%s", this.eventType, this.eventAction);
        this.allowNode = String.format("allow.%s.%s", this.eventType, this.eventAction);

        this.useMaterialNames = xAuth.getPlugin().getConfig().getBoolean("permissions.use-material-names", useMaterialNames);
    }

    public String getRestrictionNode() {
        xAuthLog.fine("Requested RestrictionNode: " + restrictNode);
        return restrictNode;
    }

    public String getEventType() {
        xAuthLog.fine("Requested RestrictionEventType: " + eventType);
        return eventType;
    }

    public String getAction() {
        xAuthLog.fine("Requested RestrictionEventAction: " + eventAction);
        return eventAction;
    }

    public String getEventName() {
        xAuthLog.fine("Requested RestrictionEventName: " + eventName);
        return eventName;
    }

    public String getAllowNode() {
        xAuthLog.fine("Requested RestrictionAllowNode: " + allowNode);
        return allowNode;
    }

    public String getAllowedPermissionNode(Object[] arguments) {
        return this.assemblePermission(getAllowNode(), arguments);
    }

    public String getDeniedPermissionNode(Object[] arguments) {
        return this.assemblePermission(getRestrictionNode(), arguments);
    }

    protected String assemblePermission(String permission, Object[] arguments) {
        StringBuilder builder = new StringBuilder(permission);

        if (arguments != null) {
            for (Object obj : arguments) {
                if (obj == null) {
                    continue;
                }

                builder.append('.');
                builder.append(getObjectPermission(obj));
            }
        }

        return builder.toString();
    }

    public String getObjectPermission(Object obj) {
        if (obj instanceof Entity) {
            return (getEntityName((Entity) obj));
        } else if (obj instanceof EntityType) {
            return formatEnumString(((EntityType)obj).name());
        } else if (obj instanceof BlockState) {
            return (getBlockPermission(((BlockState)obj).getBlock()));
        } else if (obj instanceof ItemStack) {
            return (getItemPermission((ItemStack) obj));
        } else if (obj instanceof Material) {
            return (getMaterialPermission((Material) obj));
        } else if (obj instanceof Block) {
            return (getBlockPermission((Block) obj));
        } else if (obj instanceof InventoryType) {
            return getInventoryTypePermission((InventoryType)obj);
        }

        return (obj.toString());
    }

    private String formatEnumString(String enumName) {
        return enumName.toLowerCase().replace("_", "");
    }

    private String getInventoryTypePermission(InventoryType type) {
        return formatEnumString(type.name());
    }

    private String getMaterialPermission(Material type) {
        return this.useMaterialNames ? formatEnumString(type.name()) : Integer.toString(type.getId());
    }

    private String getMaterialPermission(Material type, byte metadata) {
        return getMaterialPermission(type) + (metadata > 0 ? ":" + metadata : "");
    }

    private String getBlockPermission(Block block) {
        return getMaterialPermission(block.getType(), block.getData());
    }

    public String getItemPermission(ItemStack item) {
        return getMaterialPermission(item.getType(), item.getData().getData());
    }

    private String getEntityName(Entity entity) {

        if (entity instanceof ComplexEntityPart) {
            return getEntityName(((ComplexEntityPart) entity).getParent());
        }

        String entityName = formatEnumString(entity.getType().toString());

        if (entity instanceof Item) {
            entityName = getItemPermission(((Item) entity).getItemStack());
        }

        if (entity instanceof Player) {
            return "player." + ((Player) entity).getName();
        } else if (entity instanceof Tameable) {
            Tameable animal = (Tameable) entity;

            return "animal." + entityName + (animal.isTamed() ? "." + animal.getOwner().getName() : "");
        }

        PlayerRestrictionCategory category = PlayerRestrictionCategory.fromEntity(entity);

        if (category == null) {
            return entityName; // category unknown (ender crystal)
        }

        return category.getNameDot() + entityName;
    }
}
