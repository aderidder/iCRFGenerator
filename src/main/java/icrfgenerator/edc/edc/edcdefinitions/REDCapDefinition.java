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
 * definitions for REDCap
 */
public class REDCapDefinition {
    private static final List<String> codeListFieldTypes = Arrays.asList("dropdown", "radio", "checkbox");
    private static final List<String> nonCodeListFieldTypes = Arrays.asList("text", "notes");

//    private static final List<String> textValidationOptionsList = Arrays.asList("","integer", "number", "date", "phone", "email");
    private static final List<String> textValidationOptionsList = Arrays.asList("","integer", "number", "date");

    /**
     * returns a list with the field types that have a codelist
     * @return a list with the field types that have a codelist
     */
    public static List<String> getFieldTypesWithCodeList() {
        return codeListFieldTypes;
    }

    /**
     * returns a list with the field types that do not have a codelist
     * @return a list with the field types that do not have a codelist
     */
    public static List<String> getFieldTypesWithoutCodeList() {
        return nonCodeListFieldTypes;
    }

    /**
     * returns a list with the types that can be selected when a user selects "text" as field type
     * the types this method returns can be set as Text Validation
     * @return a list with text validation options
     */
    public static List<String> getTextValidationOptionsList(){
        return textValidationOptionsList;
    }

    // integer, number, date, (phone, email)
    public static String convertDataTypeToEDCValidationType(String dataType){
        return switch (dataType) {
            case "date" -> "date";
            case "integer", "boolean" -> "integer";
            case "decimal", "count", "quantity", "duration" -> "number";
            case "code", "datetime", "string", "identifier" -> "";
            default -> "";
        };
    }

}
