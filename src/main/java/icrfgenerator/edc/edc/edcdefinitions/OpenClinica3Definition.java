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
 * definitions for OpenClinica 3
 */
public class OpenClinica3Definition{
    private static final List<String> codeListFieldTypes = Arrays.asList("single-select", "radio", "multi-select", "checkbox");
    private static final List<String> nonCodeListFieldTypes = Arrays.asList("text", "textarea");

    private static final List<String> fieldTypeWithDefaultList = Arrays.asList("single-select", "multi-select");
    private static final List<String> supportedDatatypes = Arrays.asList("INT", "REAL", "ST", "DATE");

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
     * convert an art-decor data type to an OC datatype; if not possible, returns a string
     * @param dataType the art-decor data type
     * @return the converted datatype or string
     */
    public static String convertDataTypeToEDCDataType(String dataType, String codeDataType){
        String converted;
        switch (dataType){
            case "code":
                converted = convertDataTypeToEDCDataType(codeDataType, "");
//                converted = codeDataType;
                break;
            case "integer":
            case "count":
            case "boolean":
                converted = "INT";
                break;
            case "date":
                converted = "DATE";
                break;
            case "string":
            case "identifier": //could be INT, but assume it's ST
            case "quantity": // could be INT, but also REAL, we'll set it to ST to be safe
            case "duration": // could be INT, but also REAL, we'll set it to ST to be safe
            default:
                converted = "ST";
        }
        return converted;
    }

    /**
     * returns a list of the supported data types
     * @return a list of the supported data types
     */
    public static List<String> getSupportedDatatypes() {
        return supportedDatatypes;
    }

    /**
     * returns whether a field has a Default (e.g. single-select can het "Please select an option" as its default
     * @param fieldType the field type to check
     * @return true/false
     */
    public static boolean isFieldTypeWithDefault(String fieldType){
        return fieldTypeWithDefaultList.contains(fieldType);
    }

}
