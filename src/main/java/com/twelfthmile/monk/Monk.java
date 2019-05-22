package com.twelfthmile.monk;

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
import com.twelfthmile.pdf.*;
import com.twelfthmile.utils.IMonkLogger;
import com.twelfthmile.utils.Log;
import com.twelfthmile.utils.SeedManager;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Map;


public class Monk {

    public static List<Map<String, String>> getTransactions(File pdfFile)
            throws MonkException {
        return getAllTransactions(Converter.convertPDFToStringRows(pdfFile, null));

    }

    public static List<Map<String, String>> getTransactions(File pdfFile, String password)
            throws MonkException {
        return getAllTransactions(Converter.convertPDFToStringRows(pdfFile, password));

    }

    public static void setLogger(IMonkLogger monkLogger) {
        Log.setLogger(monkLogger);
    }

    public static void setExternalNormaliser(JSONObject normaliser) {
        SeedManager.getInstance().overrideNormalizerJson(normaliser);
    }

    private static List<Map<String, String>> getAllTransactions(List<String> pdfAsList) throws MonkException {

        Tabulator tabulator = new Tabulator(pdfAsList);
        List<Map.Entry<List<String>, List<List<String>>>> tables = tabulator
                .getTableList();

        TableSanitizer sanitizer = new TableSanitizer();
        List<List<Map<String, String>>> maps = sanitizer
                .sanitizeTables(tables);

        List<Map<String, String>> normalizedData = BranchRules
                .createNormalizeOutput(maps, SeedManager.getInstance()
                        .getTokenToKeyMap());

        return StatementRules.fixCreditDebitToBalance(normalizedData);

    }
}
