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
import com.twelfthmile.utils.SeedManager;
import com.twelfthmile.utils.Utils;

import java.util.*;

class IndexHelper {

     static Map<Integer, List<String>> findColumnNameIndices(List<String> lines) {
        int indexCount = 0;
        Map<Integer, List<String>> indexToColumnNameMap = new HashMap<>();
        for (String line: lines) {
            List<String> header = getPossibleTableHeadersFromRow(line.toLowerCase());
            if (header.size() > 0) {
                if (!isNewHeaderSameAsBefore(indexToColumnNameMap, header))
                    indexToColumnNameMap.put(indexCount, header);
            }
            indexCount++;
        }
        return indexToColumnNameMap;
    }

    static List<String> getPossibleTableHeadersFromRow( String line) {
        int count = 0;
        boolean isDatePresent = false;
        boolean isDebitOrCreditPresent = false;

        Map<String, String> grammarSeed = SeedManager.getInstance()
                .getTokenToKeyMap();
        List<String> headerNames = new ArrayList<>();
        for (String token : grammarSeed.keySet()) {
            if (line.contains(token)) {
                if (grammarSeed.get(token)
                        .equalsIgnoreCase(MonkConstants.CREDIT)) {
                    isDebitOrCreditPresent = true;
                    count++;
                }
                if (grammarSeed.get(token)
                        .equalsIgnoreCase(MonkConstants.DEBIT)) {
                    isDebitOrCreditPresent = true;
                    count++;
                }
                if (grammarSeed.get(token)
                        .equalsIgnoreCase(MonkConstants.BALANCE)) {
                    count++;
                }
                if (grammarSeed.get(token)
                        .equalsIgnoreCase(MonkConstants.DATE)) {
                    isDatePresent = true;
                }
                headerNames.add(token);
            }
        }

        if (isDebitOrCreditPresent && count <= 2 && isDatePresent)
            count++;
        headerNames.sort(Comparator.comparingInt(line::indexOf));

        return count > 2 ? headerNames : new ArrayList<>();
    }

    private static boolean isNewHeaderSameAsBefore(Map<Integer, List<String>> indexToColumnNameMap,
                                            List<String> newHeader) {
        boolean found = false;
        for (Map.Entry<Integer, List<String>> entry: indexToColumnNameMap.entrySet()) {
            boolean isSame = true;
            if (entry.getValue().size() != newHeader.size()) {
                isSame = false;
            } else  {
                for (int j = 0; j < entry.getValue().size(); j++) {
                    if (!newHeader.get(j).equalsIgnoreCase(entry.getValue().get(j)))
                        isSame = false;
                }
            }
            if (isSame)
                found = true;
        }
        return found;
    }

    static int findEndIndex(int pairWiseEnd, boolean isLastColumn, String line) {
        if (isLastColumn || pairWiseEnd == line.length() - 1) {
            return line.length() - 1;
        } else {
            return findEmptyIndexBeforeKey(line, pairWiseEnd + 1 <
                    line.length() ? pairWiseEnd + 1 : pairWiseEnd);
        }
    }

    static int findEmptyIndexBeforeKey(String line, int key) {
        if (line.length() > 0 && key < line.length()) {
            if (line.charAt(key) != ' ' && line.charAt(key) != '\t') {
                for (int i = key - 1; i >= 0; i--) {
                    if (line.charAt(i) == ' ' || line.charAt(i) == '\t') {
                        return i;
                    }
                }
            }
        }
        return key;
    }

    static List<Map.Entry<Integer, Integer>>  getColumnIndices(String line,
                                                               List<String> columnNames) throws MonkException {
        List<Map.Entry<Integer, Integer>> indices = new ArrayList<>();

        int start = 0, end, i;
        Map<Integer, Integer> map;
        String lineToSearch = line;
        for (i = 1; i < columnNames.size(); i++) {
            end = lineToSearch.toLowerCase().indexOf(columnNames.get(i)
                    .split(" ")[0]) - 1;
            map = new HashMap<>();
            if (end < 0) {
                throw new MonkException("Column indices cannot be negative");
            }
            map.put(start, end);
            indices.addAll(map.entrySet());
            start = end;
            lineToSearch = Utils.maskString(lineToSearch, end);
        }

        map = new HashMap<>();
        map.put(start, line.length() - 1);
        indices.addAll(map.entrySet());

        return indices;
    }

}
