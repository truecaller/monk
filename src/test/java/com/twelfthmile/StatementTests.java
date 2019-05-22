package com.twelfthmile;

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
import com.twelfthmile.monk.Monk;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class StatementTests {


    @Test
    public void sampleFinancialStatementTest() throws IOException, MonkException {
        File file = TestFileUtils.readFile("sample-normaliser.json");
        String metadata = new String(Files.readAllBytes(file.toPath()));

        Monk.setExternalNormaliser(new JSONObject(metadata));

        List<Map<String, String>> out = Monk.getTransactions(TestFileUtils
                .readFile("sample.pdf"), null);

        Assert.assertEquals(out.size(), 24);

        Assert.assertEquals(out.get(0).get("balance"), "71545.11");
        Assert.assertEquals(out.get(0).get("debit"), "1000.0");
        Assert.assertEquals(out.get(0).get("date"), "2018-08-01 00:00:00");

        Assert.assertEquals(out.get(6).get("balance"), "7326.11");
        Assert.assertEquals(out.get(6).get("debit"), "168.0");
        Assert.assertEquals(out.get(6).get("date"), "2018-08-31 00:00:00");
    }
}
