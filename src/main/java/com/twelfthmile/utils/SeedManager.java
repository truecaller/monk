package com.twelfthmile.utils;

/*
 * Author: Vishnu Satis
 * Github: vizsatiz
 *
 * LICENSE:
 *
 *                 Apache License
 *             Version 2.0, January 2004
 *          http://www.apache.org/licenses/
 *
 * Copyright (C) 2019 True Software Scandinavia AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  <http://www.apache.org/licenses/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SeedManager {

    private Map<String, String> tokenToKeyMap;

    private static class LazyLoader {
        private static SeedManager INSTANCE = new SeedManager();
    }

    public static SeedManager getInstance() {
        return LazyLoader.INSTANCE;
    }

    private SeedManager() {
        tokenToKeyMap = tokenToKeyMap(Utils.getDataTypeSeeds());
    }

    public void overrideNormalizerJson(JSONObject normaliser) {
        tokenToKeyMap = tokenToKeyMap(normaliser);
    }

    public Map<String, String> getTokenToKeyMap() {
        return tokenToKeyMap;
    }

    private Map<String, String> tokenToKeyMap(JSONObject dataTypes) {
        Map<String, String> tokenToKeyMap = new HashMap<>();

        for (Iterator it = dataTypes.keys(); it.hasNext(); ) {
            String key = (String) it.next();
            String[] values = dataTypes.getString(key).split("\\|");
            for (String value : values) {
                tokenToKeyMap.putIfAbsent(value, key);
            }
        }
        return tokenToKeyMap;
    }
}
