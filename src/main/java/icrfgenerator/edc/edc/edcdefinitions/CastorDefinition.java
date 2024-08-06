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
 * definitions for castor
 */
public class CastorDefinition {
    // define the field types that do / do not have a codelist
    private static final List<String> codeListFieldTypes = Arrays.asList("checkbox", "dropdown", "radio");
    private static final List<String> nonCodeListFieldTypes = Arrays.asList("datetime", "date", "numeric", "string", "textarea");

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
     * converts a field type to a field type description
     * @param fieldType the field type
     * @return the description of the field type
     */
    public static String getFieldTypeDescription(String fieldType){
        return switch (fieldType) {
            case "checkbox" -> "Checkbox";
            case "date" -> "Date";
            case "dropdown" -> "Dropdown";
            case "numeric" -> "Numeric field";
            case "radio" -> "Radiobutton";
            case "string" -> "Textfield";
            case "textarea" -> "Multiline textfield";
            case "datetime" -> "Date & Time";
            default -> "Unknown...";
        };
    }

    /**
     * converts the art-decor datatype to a castor datatype if possible; otherwise returns string
     * @param dataType art-decor datatype
     * @return converted datatype or string
     */
    public static String convertDataTypeToEDCFieldType(String dataType){
        return switch (dataType) {
            case "code" -> "dropdown";
            case "integer", "decimal", "count", "quantity", "boolean", "duration" -> "numeric";  // not 100% about boolean and duration...
            case "date" -> "date";
            case "string", "identifier" -> "string";
            case "datetime" -> "datetime";
            default -> "string";
        };
    }

}
