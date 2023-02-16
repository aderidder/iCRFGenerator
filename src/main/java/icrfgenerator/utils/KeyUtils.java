/*
 * Copyright (c) 2019 VUmc/KWF TraIT2Health-RI
 *
 * This file is part of iCRFGenerator
 *
 * iCRFGenerator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * iCRFGenerator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with iCRFGenerator. If not, see <http://www.gnu.org/licenses/>
 */

package icrfgenerator.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper functions to ensure we use the same keys everywhere within the program
 */
public class KeyUtils {

    private static final Pattern languagePattern = Pattern.compile(".*\\d(\\D+)$");

    /**
     * construct a key - basically a concatenation of the provided components with "_" in between them
     * @param components the components for the key, e.g. a, b, c
     * @return the key, e.g. a_b_c
     */
    public static String getKey(String ... components){
        return String.join("_", components);
    }

    /**
     * helper for constructing the simpleLanguageKey
     * @param codebookId     codebookId
     * @param datasetId      datasetId
     * @param simpleLanguage simpleLanguage
     * @return the key, codebookId_datasetId_simpleLanguage
     */
    public static String getSimpleLanguageKey(String codebookId, String datasetId, String simpleLanguage){
        return getKey(codebookId, datasetId, simpleLanguage);
    }

    public static String extractLanguageFromKey(String key){
        Matcher matcher = languagePattern.matcher(key);
        if(matcher.matches()) {
            return matcher.group(1);
        }
        return "";
    }
}
