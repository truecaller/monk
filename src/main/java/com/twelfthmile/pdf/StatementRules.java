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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatementRules {

    public static List<Map<String, String>> fixCreditDebitToBalance(List<Map<String, String>> records) {
        Double previousBalance = -1.0D;
        Double currentBalance;

        List<Map<String, String>> fixedList = new ArrayList<>();
        for (Map<String, String> record: records) {
            String balance = record.get("balance");
            String credit = record.get("credit");
            String debit = record.get("debit");
            if (null != balance) {
                try {
                    currentBalance = Double.parseDouble(balance);
                    if (previousBalance != -1.0D) {
                        if (null != credit && null != debit
                                && !credit.isEmpty() && !debit.isEmpty()) {
                            Double tempBalanceOne = previousBalance + Double.parseDouble(credit) - Double.parseDouble(debit);
                            Double tempBalanceTwo = previousBalance - Double.parseDouble(credit) + Double.parseDouble(debit);
                            if (Math.abs(currentBalance - tempBalanceOne) > Math.abs(currentBalance - tempBalanceTwo)) {
                                record.put("credit", debit);
                                record.put("debit", credit);
                            }
                        } else if (null != credit && !credit.isEmpty()) {
                            Double tempBalanceOne = previousBalance + Double.parseDouble(credit);
                            Double tempBalanceTwo = previousBalance - Double.parseDouble(credit);
                            if (Math.abs(currentBalance - tempBalanceOne) > Math.abs(currentBalance - tempBalanceTwo)) {
                                record.put("credit", debit);
                                record.put("debit", credit);
                            }
                        } else if (null != debit && !debit.isEmpty()) {
                            Double tempBalanceOne = previousBalance - Double.parseDouble(debit);
                            Double tempBalanceTwo = previousBalance + Double.parseDouble(debit);
                            if (Math.abs(currentBalance - tempBalanceOne) > Math.abs(currentBalance - tempBalanceTwo)) {
                                record.put("credit", debit);
                                record.put("debit", credit);
                            }
                        }
                    }
                    previousBalance = currentBalance;
                    fixedList.add(record);
                } catch (NumberFormatException e) {
                    Log.logError("Error while fixing debit credit cycle in finalize");
                }
            }
        }

        return fixedList;
    }

}
