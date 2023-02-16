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

import java.util.*;

/**
 * definitions for OpenClinica 3
 */
public class OpenClinica3Definition{
    private static final List<String> codeListFieldTypes = Arrays.asList("single-select", "radio", "multi-select", "checkbox");
    private static final List<String> nonCodeListFieldTypes = Arrays.asList("text", "textarea");

    private static final List<String> fieldTypeWithDefaultList = Arrays.asList("single-select", "multi-select");
//    private static final List<String> supportedDatatypes = Arrays.asList("INT", "REAL", "ST", "DATE");

    private static final Map<String, List<String>> fieldTypeDataTypesMap = new HashMap<>();

    static {
//        fieldTypeDataTypesMap.put("textarea", List.of("ST"));
//        fieldTypeDataTypesMap.put("text", List.of("INT", "REAL", "ST", "DATE"));
//        fieldTypeDataTypesMap.put("single-select", List.of("INT", "ST"));
//        fieldTypeDataTypesMap.put("radio", List.of("INT", "ST"));
//        fieldTypeDataTypesMap.put("multi-select", List.of("INT", "ST"));
//        fieldTypeDataTypesMap.put("checkbox", List.of("INT", "ST"));
        fieldTypeDataTypesMap.put("textarea", new ArrayList<>(List.of("ST")));
        fieldTypeDataTypesMap.put("text", new ArrayList<>(List.of("INT", "REAL", "ST", "DATE")));
        fieldTypeDataTypesMap.put("single-select", new ArrayList<>(List.of("INT", "ST")));
        fieldTypeDataTypesMap.put("radio", new ArrayList<>(List.of("INT", "ST")));
        fieldTypeDataTypesMap.put("multi-select", new ArrayList<>(List.of("INT", "ST")));
        fieldTypeDataTypesMap.put("checkbox", new ArrayList<>(List.of("INT", "ST")));
    }

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
        return switch (dataType) {
            case "code" -> convertDataTypeToEDCDataType(codeDataType, "");
            case "decimal", "quantity" -> "REAL";
            case "integer", "count", "boolean" -> "INT";
            case "date" -> "DATE";
            case "string", "identifier", "duration", "datetime" -> "ST";
            default -> "ST";
        };
    }

    /**
     * returns a list of the supported data types
     * @return a list of the supported data types
     */
//    public static List<String> getSupportedDatatypes() {
//        return supportedDatatypes;
//    }
    public static List<String> getDataTypesList(String fieldType){
        return fieldTypeDataTypesMap.get(fieldType);
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
