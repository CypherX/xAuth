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
package de.luricos.bukkit.xAuth.strike;

import java.util.HashMap;
import java.util.Map;

public class StrikeRecord {
    private Map<String, Integer> strikes = new HashMap<String, Integer>();

    public StrikeRecord() {
        // Empty constructor is empty
    }

    // Returns new strike amount
    public int addStrike(String playerName) {
        Integer count = strikes.get(playerName);
        if (count == null)
            count = 0;

        strikes.put(playerName, ++count);
        return count;
    }

    public void clearStrikes(String playerName) {
        strikes.remove(playerName);
    }
}