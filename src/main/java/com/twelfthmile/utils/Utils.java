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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Utils {

    private static String readFromFile(String file) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream(file);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            if (null != is) {
                Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } else {
                Log.logError("Couldn't read seed file due to null Input Stream");
            }
        } catch (Exception e) {
            Log.logError("Couldn't read seed file :" +  e.getMessage());
        } finally {
            try {
                if (null != is)
                    is.close();
            } catch (Exception e) {
                Log.logError("Couldn't close input stream : " + e.getMessage());
            }
        }
        return writer.toString();
    }

    static JSONObject getDataTypeSeeds() throws JSONException {
        String binderSeed = readFromFile("raw/normaliser.json");
        return new JSONObject(binderSeed);
    }

    public static String maskString(String s, int l) {
        StringBuilder builder = new StringBuilder();
        for (int k = 0; k < s.length(); k++) {
            if (k <= l)
                builder.append("*");
            else
                builder.append(s.charAt(k));
        }
        return builder.toString();
    }
}
