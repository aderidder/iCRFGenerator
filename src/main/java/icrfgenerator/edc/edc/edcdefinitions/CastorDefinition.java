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
    private static final List<String> nonCodeListFieldTypes = Arrays.asList("date", "numeric", "string", "textarea");

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
        String description;
        switch (fieldType){
            case "checkbox":
                description = "Checkbox"; break;
            case "date":
                description = "Date"; break;
            case "dropdown":
                description = "Dropdown"; break;
            case "numeric":
                description = "Numeric field"; break;
            case "radio":
                description = "Radiobutton"; break;
            case "string":
                description = "Textfield"; break;
            case "textarea":
                description = "Multiline textfield"; break;
            default: description="Unknown...";
        }
        return description;
    }

    /**
     * converts the art-decor datatype to a castor datatype if possible; otherwise returns string
     * @param dataType art-decor datatype
     * @return converted datatype or string
     */
    public static String convertDataTypeToEDCFieldType(String dataType){
        String converted;
        switch (dataType){
            case "code":
                converted = "dropdown";
                break;
            case "integer":
            case "count":
            case "quantity":
            case "boolean":
            case "duration":
                converted = "numeric";  // not 100% about boolean and duration...
                break;
            case "date":
                converted = "date";
                break;
            case "string":
            case "identifier": //could be INT, but assume it's ST
             default:
                converted = "string";
        }
        return converted;
    }

}
