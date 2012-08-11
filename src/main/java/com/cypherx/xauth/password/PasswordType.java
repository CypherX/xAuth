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
package com.cypherx.xauth.password;

public enum PasswordType {
    DEFAULT(0),
    WHIRLPOOL(1),
    MD5(2, "MD5"),
    SHA1(3, "SHA1"),
    SHA256(4, "SHA-256"),
    AUTHME_SHA256(5);

    private int type;
    private String algorithm;

    PasswordType(int type) {
        this(type, null);
    }

    PasswordType(int type, String algorithm) {
        this.type = type;
        this.algorithm = algorithm;
    }

    public int getType() {
        return type;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public static PasswordType getType(int type) {
        for (PasswordType t : values())
            if (t.type == type)
                return t;

        return null;
    }
}