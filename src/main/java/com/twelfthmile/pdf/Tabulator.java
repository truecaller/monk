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

import com.twelfthmile.exception.MonkException;
import com.twelfthmile.utils.MonkConstants;

import java.util.*;

public class Tabulator {

    private List<String> pdf;

    public Tabulator(List<String> pdf) {
        this.pdf = pdf;
    }

    public List<Map.Entry<List<String>, List<List<String>>>> getTableList() throws MonkException {

        List<Map.Entry<List<String>, List<List<String>>>> tables = new ArrayList<>();
        Map<Integer, List<String>> indices = IndexHelper.findColumnNameIndices(this.pdf);

        Map<List<String>, List<List<String>>> map;
        if (indices.size() > 0) {
            for (Map.Entry<Integer, List<String>> tableColumn: indices.entrySet()) {
                List<List<String>> table = getTable(tableColumn);
                map = new HashMap<>();
                map.put(tableColumn.getValue(), table);
                tables.addAll(map.entrySet());
            }
        }
        return tables;
    }

    private List<List<String>> getTable(Map.Entry<Integer, List<String>> indexToColumnNameMap) throws MonkException {
        List<Map.Entry<Integer, Integer>>  columnIndices = IndexHelper
                .getColumnIndices(this.pdf.get(indexToColumnNameMap.getKey()), indexToColumnNameMap.getValue());

        List<List<String>> table = new ArrayList<>();
        for (int j = indexToColumnNameMap.getKey() + 1; j < this.pdf.size(); j++) {
            String line = this.pdf.get(j);
            if (line.length() > 0) {

                // check if this is the start of next table
                List<String> extractedHeaders = IndexHelper.getPossibleTableHeadersFromRow(line.toLowerCase());
                if (extractedHeaders.size() > 0) {
                    boolean isCurrentRowAHeader = extractedHeaders.size() == indexToColumnNameMap.getValue().size()
                            && isSameAsCurrentRowHeaders(line.toLowerCase(),
                            indexToColumnNameMap.getValue());
                    if (isCurrentRowAHeader) {
                        Map<Integer, List<String>> newIndexMap = new HashMap<>();
                        newIndexMap.put(j, indexToColumnNameMap.getValue());
                        for (Map.Entry<Integer, List<String>> tableColumn: newIndexMap.entrySet())
                            columnIndices = IndexHelper.getColumnIndices(this.pdf.get(tableColumn.getKey()),
                                    tableColumn.getValue());
                        continue;
                    } else {
                        // Done with current table (which includes multiple continuous tables with same headers)
                        break;
                    }
                }

                List<String> rowValue = new ArrayList<>();
                int i = 0;
                for (Map.Entry<Integer, Integer> pair : columnIndices) {
                    int start = IndexHelper.findEmptyIndexBeforeKey(line, pair.getKey());
                    int end = IndexHelper.findEndIndex(pair.getValue(), i == columnIndices.size() - 1, line);
                    end = end < line.length() ? end : line.length() - 1;
                    start = start < line.length() ? start : line.length() - 1;
                    rowValue.add(line.substring(start, end));
                    i++;
                }

                table.add(rowValue);
            }
        }

        return table;
    }



    private boolean isSameAsCurrentRowHeaders(String line, List<String> columnNames) {
        for (String column: columnNames) {
            if (!line.contains(column)) {
                return false;
            }
        }
        return true;
    }



}
