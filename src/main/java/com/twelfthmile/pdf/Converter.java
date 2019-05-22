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

import com.external.PDFLayoutTextStripper;
import com.twelfthmile.exception.MonkException;
import com.twelfthmile.utils.MonkConstants;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Converter {

    public static List<String> convertPDFToStringRows(File file, String password) throws MonkException {
        String pdfString = convertPDFToString(file, password);
        return Arrays.asList(pdfString.split(MonkConstants.NEW_LINE));
    }

    private static String convertPDFToString(File file, String password) throws MonkException {
        try {
            RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
            PDFParser pdfParser = null == password ? new PDFParser(accessFile) : new PDFParser(accessFile, password);
            pdfParser.parse();
            PDDocument pdDocument = new PDDocument(pdfParser.getDocument());
            PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper();
            return pdfTextStripper.getText(pdDocument);
        } catch (IOException e) {
            throw new MonkException("Error while parsing PDF " + e.getMessage());
        }
    }
}
