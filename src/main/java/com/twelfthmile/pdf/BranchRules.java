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

import com.twelfthmile.utils.Log;
import com.twelfthmile.utils.MonkConstants;
import com.twelfthmile.yuga.Yuga;
import com.twelfthmile.yuga.types.ParseException;
import com.twelfthmile.yuga.types.Response;
import com.twelfthmile.yuga.utils.Constants;

import java.util.*;

public class BranchRules {

    public static List<Map<String, String>> createNormalizeOutput(List<List<Map<String, String>>> tables,
                                                                  Map<String, String> tokenToKeyMap) {

        List<Map<String, String>> records = new ArrayList<>();
        tables.sort((o1, o2) -> o2.size() - o1.size());
        if (tables.size() > 0) {
            List<Map<String, String>> table = tables.get(0);
            if (tables.size() > 1)
                Log.logInfo("Ignoring a smaller table: " + tables.size());
            for (Map<String, String> row : table) {
                Map<String, String> record = processRowForTheKey(row, tokenToKeyMap);
                if (isRecordWorthy(record)) {
                    try {
                        record.put(MonkConstants.DATE,
                                String.valueOf(Yuga.parse(record
                                        .get(MonkConstants.DATE)).getStr()));
                    } catch (Exception e) {
                        Log.logInfo("Error while parsing date: " + record
                                .get(MonkConstants.DATE));
                    }
                    List<Map<String, String>> recs = handleNonEmptyCreditAndDebit(processRecord(record));
                    records.addAll(recs);
                } else {
                    Log.logInfo("Ignoring record as there is not credit or debit info: " + record.toString());
                }
            }
        }
        return records;
    }

    private static List<Map<String, String>> handleNonEmptyCreditAndDebit(Map<String, String> record) {
        String credit = record.get(MonkConstants.CREDIT);
        String debit = record.get(MonkConstants.DEBIT);
        try {
            credit = (null != credit && Float.parseFloat(credit) > 0.0f)
                    ? String.valueOf(Float.parseFloat(credit)) : null;
        } catch (Exception e){
            Log.logInfo("Unable to parse credit: " + e.getMessage());
        }
        try {
            debit = (null != debit && Float.parseFloat(debit) > 0.0f)
                    ? String.valueOf(Float.parseFloat(debit)) : null;
        } catch (Exception e){
            Log.logInfo("Unable to parse credit: " + e.getMessage());
        }
        List<Map<String, String>> records = new ArrayList<>();
        if (null != credit && null != debit) {
            if (!credit.isEmpty() &&  !debit.isEmpty()) {
                Map<String, String> newRecord = new HashMap<>(record);

                newRecord.put(MonkConstants.CREDIT, "");
                records.add(newRecord);

                newRecord = new HashMap<>(record);
                newRecord.put(MonkConstants.DEBIT, "");

                records.add(newRecord);
            } else {
                records.add(record);
            }
        } else {
            records.add(record);
        }
        return records;
    }

    private static Map<String, String> processRecord(Map<String, String> record) {
        cleanUpCreditOrDebitValues(record, "credit");
        cleanUpCreditOrDebitValues(record, "debit");
        cleanUpBalanceValues(record);
        shuffleDebitCreditNegations(record);
        return record;
    }

    private static void cleanUpCreditOrDebitValues(Map<String, String> record, String key) {

        String amounts = record.get(key);
        HashMap<String, String> config = new HashMap<>();
        config.put(Constants.YUGA_SOURCE_CONTEXT, Constants.YUGA_SC_CURR);
        config.put("type", Constants.TY_AMT);

        if (null == amounts || amounts.isEmpty())
            return;
        float total = 0;

        String [] values = amounts.split("\\|");
        for (String value: values) {
            value = value.replaceAll(",", "");
            String [] possibleValues = value.trim().split(" ");
            for (String possibility : possibleValues) {
                try {
                    value = cleanAmountValues(possibility);
                    Response yugaResponse = Yuga.parse(value, config);
                    // Making value negative for its a debit
                    if (value.toLowerCase().contains("cr"))
                        yugaResponse.setStr("-" + yugaResponse.getStr());
                    total += Float.parseFloat(yugaResponse.getStr());
                    break;
                } catch (Exception e) {
                    Log.logInfo("Ignoring [" + key + "] for value: " + value);
                }
            }
        }
        record.put(key, String.valueOf(total));
    }

    private static String cleanAmountValues(String amount) {
        String [] splitByDecimal = amount.split("\\.");
        if (splitByDecimal.length > 1) {
            int i = 0;
            StringBuilder builder = new StringBuilder();
            while(i < splitByDecimal.length - 1) {
                builder.append(splitByDecimal[i]);
                i++;
            }
            builder.append(".").append(splitByDecimal[i]);
            return builder.toString();
        } else {
            return amount;
        }
    }

    private static void cleanUpBalanceValues(Map<String, String> record) {
        record.putIfAbsent(MonkConstants.BALANCE, "0.0");
        String balance = record.get(MonkConstants.BALANCE);
        String [] values = balance.split(" \\|");
        if (values.length > 0) {
            String balanceValue = values[values.length - 1].trim();
            try {
                balanceValue = Yuga.parse(balanceValue).getStr();
                record.put(MonkConstants.BALANCE, balanceValue);
            } catch (Exception e) {
                try {
                    balance = breakTillFirstAlphabet(balance);
                    HashMap<String, String> config = new HashMap<>();
                    config.put(Constants.YUGA_SOURCE_CONTEXT, Constants.YUGA_SC_CURR);
                    balanceValue = Yuga.parse(balance, config).getStr();
                    record.put(MonkConstants.BALANCE, balanceValue);
                } catch (Exception e2) {
                    Log.logInfo("Error while parsing balance: " + e2.getMessage());
                    record.put(MonkConstants.BALANCE, "0.0");
                }
                Log.logInfo("Error while parsing balance: " + e.getMessage());
            }
        }
    }

    private static String breakTillFirstAlphabet(String balance) {
        StringBuilder builder = new StringBuilder();
        char [] balChars = balance.toCharArray();
        for (char balChar : balChars) {
            if (Character.isDigit(balChar) || balChar == ' '
                    || balChar == ',' || balChar == '.') {
                builder.append(balChar);
            } else {
                break;
            }
        }
        return builder.toString();
    }

    private static Map<String, String> processRowForTheKey(Map<String, String> row,
                                                    Map<String, String> tokenToKeyMap) {
        boolean noTokenFound = true;
        Set<String> keys = row.keySet();
        Map<String, String> valMap = new HashMap<>();

        for (String key: keys) {
            for (String token : tokenToKeyMap.keySet()) {
                if (key.contains(token)) {
                    noTokenFound = false;
                    if (valMap.get(tokenToKeyMap.get(token)) == null) {
                        valMap.put(tokenToKeyMap.get(token), row.get(key));
                    }

                }
            }
            if (noTokenFound) {
                valMap.put("misc", row.get(key));
            }
        }

        return valMap;
    }

    private static void shuffleDebitCreditNegations(Map<String, String> record) {
        String credit = record.get("credit");
        String debit = record.get("debit");
        try {

            Float creditFloat = null, debitFloat= null;

            if (null != credit) {
                creditFloat = Float.parseFloat(credit);
            }
            if (null != debit) {
                debitFloat = Float.parseFloat(debit);
            }

            if (null != debitFloat && null != creditFloat) {
                if (creditFloat < 0) {
                    debitFloat += creditFloat * (-1.0f);
                    creditFloat = 0.0f;
                }
                if (debitFloat < 0) {
                    creditFloat += debitFloat * (-1.0f);
                    debitFloat = 0.0f;
                }
            }

            if (null != debitFloat) {
                if (debitFloat < 0) {
                    creditFloat = debitFloat * (-1.0f);
                    debitFloat = 0.0f;
                }
            }

            if (null != creditFloat) {
                if (creditFloat < 0) {
                    debitFloat = creditFloat * (-1.0f);
                    creditFloat = 0.0f;
                }
            }

            record.put(MonkConstants.CREDIT, String.valueOf(null != creditFloat ? creditFloat : 0.0f));
            record.put(MonkConstants.DEBIT, String.valueOf(null != debitFloat ? debitFloat : 0.0f));

        } catch (Exception e) {
            Log.logInfo("Error while processing debit and credit exchange: " + e.getMessage());
        }
    }

    private static boolean isRecordWorthy(Map<String, String> record) {
        boolean worthy = true;
        if (nonNumericOrEmpty(record.get(MonkConstants.CREDIT))
                && nonNumericOrEmpty(record.get(MonkConstants.DEBIT))) {
            if (nonNumericOrEmpty(record.get(MonkConstants.BALANCE)))
                worthy = false;
        } else if (nonNumericOrEmpty(record.get(MonkConstants.CREDIT))) {
            if(isDateInvalid(record.get(MonkConstants.DATE)))
                worthy = false;
        } else if (nonNumericOrEmpty(record.get(MonkConstants.DEBIT))) {
            if(isDateInvalid(record.get(MonkConstants.DATE)))
                worthy = false;
        }

        if (isDateInvalid(record.get(MonkConstants.DATE)))
            worthy = false;

        return worthy;
    }

    private static boolean isDateInvalid(String date) {
        if (null == date || date.isEmpty()) {
            return true;
        }
        try {
            Response response = Yuga.parse(date.trim());
            return response == null;
        } catch (Exception e) {
            return true;
        }
    }

    private static boolean nonNumericOrEmpty(String value) {
        try {
            if (Yuga.parse(value.trim()).getType().equalsIgnoreCase("AMT")) {
                return false;
            }
        } catch (Exception e){
            // Ignoring yuga parse error
        }
        if (null == value || (value.matches(".*[a-zA-Z]+.*") && value.length() > 11) || value.isEmpty())
            return true;
        String numericValue = value.replaceAll("[^\\d.]", "");
        if (numericValue.length() == value.length())
            return false;
        return numericValue.length() < 3;
    }

    public static List<Map<String, String>> processMiscData(List<Map<String, String>> records) {
        List<Map<String, String>> out = new ArrayList<>();
        for (Map<String, String> record: records) {
            if (record.containsKey(MonkConstants.LOAN)) {
                String loanLine = record.get(MonkConstants.LOAN);
                String [] values = loanLine.split(" ");
                for (String value : values) {
                    try {
                        Response response = Yuga.parse(value.trim());
                        if (response.getType().equalsIgnoreCase("AMT")) {
                            Map<String, String> map = new HashMap<>();
                            map.put(MonkConstants.LOAN, response.getStr());
                            if (loanLine.toLowerCase().contains("repayment"))
                                map.put(MonkConstants.TYPE, "repayment");
                            if (loanLine.toLowerCase().contains("principal"))
                                map.put(MonkConstants.TYPE, "principal");
                            out.add(map);
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        }
        return out;
    }

}
