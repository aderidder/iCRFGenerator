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

package icrfgenerator.edc.edc.edcdefinitions;

import java.util.Arrays;
import java.util.List;

/**
 * definitions for ODM
 */
public class ODMDefinition {
    // there are more options which we could perhaps add in the future...
    private static final List<String> codeListDataTypes = Arrays.asList("integer", "float", "string");
    private static final List<String> nonCodeListDataTypes = Arrays.asList("integer", "float", "date", "time", "datetime", "string", "boolean");

    /**
     * returns a list with the possible datatypes for codelists
     * @return a list with the possible datatypes for codelists
     */
    public static List<String> getDataTypesWithCodeList() {
        return codeListDataTypes;
    }

    /**
     * returns a list with the possible datatypes for non-codelists items
     * @return a list with the possible datatypes for non-codelists items
     */
    public static List<String> getDataTypesWithoutCodeList() {
        return nonCodeListDataTypes;
    }

    // "text", "integer", "float", "date", "time", "datetime", "string", "boolean", "double",
    // "hexBinary", "base64Binary", "hexFloat", "base64Float", "partialDate", "partialTime",
    // "partialDatetime", "durationDatetime", "intervalDatetime", "incompleteDatetime",
    // "incompleteDate", "incompleteTime", "URI"
    public static String convertDataTypeToEDCDataType(String dataType) {
        return switch (dataType) {
            case "decimal" -> "float";
            case "integer", "count", "quantity", "duration" -> "integer";
            case "boolean" -> "boolean";
            case "date" -> "date";
            case "code", "string", "identifier" -> "string";
            case "datetime" -> "datetime";
            default -> "string";
        };
    }
}
