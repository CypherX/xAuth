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
package de.luricos.bukkit.xAuth.password;

import com.google.common.collect.Maps;

import java.util.Map;

public enum PasswordType {
    DEFAULT(0, "DEFAULT"),
    WHIRLPOOL(1, "WHIRLPOOL"),
    MD5(2, "MD5"),
    SHA1(3, "SHA1"),
    SHA256(4, "SHA-256"),
    AUTHME_SHA256(5, "AUTHME_SHA256");

    private final int typeId;
    private final String algorithm;
    private static PasswordType[] byId = new PasswordType[6];
    private final static Map<String, PasswordType> BY_NAME = Maps.newHashMap();

    PasswordType(int typeId, String algorithm) {
        this.typeId = typeId;
        this.algorithm = algorithm;
    }

    static {
        for (PasswordType passwordType : values()) {
            byId[passwordType.typeId] = passwordType;
            BY_NAME.put(passwordType.name(), passwordType);
        }
    }

    /**
     * Gets the type ID of this PasswordType
     *
     * @return ID of this PasswordType
     */
    public int getTypeId() {
        return typeId;
    }

    /**
     * Get the PasswordType's name
     *
     * @return String name of the PasswordType
     */
    public String getName() {
        return byId[this.getTypeId()].name();
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * Attempts to get the PasswordType with the given ID
     *
     * @param id ID of the type to get
     * @return PasswordType if found, or null
     */
    public static PasswordType getType(final int id) {
        if (byId.length > id && id >= 0) {
            return byId[id];
        }
        return null;
    }

    /**
     * Get the PasswordType via name
     *
     * @param name Name of the type to get
     * @return PasswordType
     */
    public static PasswordType getType(final String name) {
        return BY_NAME.get(name);
    }
}