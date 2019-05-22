package com.twelfthmile.pdf;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableSanitizer {

    public List<List<Map<String, String>>> sanitizeTables(List<Map.Entry<List<String>, List<List<String>>>> tables) {
        return tables.stream().map(t -> sanitizeTable(t.getKey(), t.getValue()))
                .collect(Collectors.toList());
    }

    private List<Map<String, String>> sanitizeTable(List<String> columns, List<List<String>> table) {
        List<List<String>> mergedTable = sanitizeRowsByMerging(table);
        return mergedTable.stream().map(m -> sanitizeRow(columns, m))
                .collect(Collectors.toList());
    }

    private Map<String, String> sanitizeRow(List<String> columns, List<String> row) {
        Map<String, String> valMap = new HashMap<>();
        for (int i = 0; i < row.size(); i++) {
            String value = sanitizeValue(row.get(i));
            valMap.put(columns.get(i), value);
        }
        return valMap;
    }

    private  List<List<String>> sanitizeRowsByMerging(List<List<String>> table) {
        List<List<String>> output = new ArrayList<>();

        for (List<String> row : table) {
            int numberOfValidColumns = 0;
            for (String aRow : row) {
                String strippedValue = aRow
                        .replaceAll(" ", "");
                strippedValue = strippedValue
                        .replaceAll("\t", "");
                if (strippedValue.length() > 0) {
                    numberOfValidColumns++;
                }
            }
            if (numberOfValidColumns <= row.size()/2) {
                if (output.size() > 0) {
                    List<String> lastFoundColumn = output.get(output.size() - 1);
                    for (int j = 0; j < row.size(); j++) {
                        if (row.get(j).replaceAll(" ", "").length() > 0) {
                            lastFoundColumn.set(j, lastFoundColumn.get(j) + "|" + row.get(j));
                        }
                    }
                    output.set(output.size() - 1, lastFoundColumn);
                }
            } else {
                output.add(row);
            }
        }
        return output;
    }

    private String sanitizeValue(String value) {
        return value.trim()
                .replaceAll("[ ]{2,}", " ");
    }
}
