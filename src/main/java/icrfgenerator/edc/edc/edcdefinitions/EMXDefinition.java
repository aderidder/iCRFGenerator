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
public class EMXDefinition {
    // define the field types that do / do not have a codelist
    // Not yet included here: xref, mref, compound, file, email, enum, hyperlink, one_to_many
    private static final List<String> codeListFieldTypes = Arrays.asList("categorical");
    private static final List<String> nonCodeListFieldTypes = Arrays.asList("string", "text", "int", "long", "decimal", "bool", "date", "datetime");

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
     * converts the art-decor datatype to a castor datatype if possible; otherwise returns string
     * <a href="https://art-decor.org/mediawiki/index.php?title=DECOR-dataset">...</a>
     * @param dataType art-decor datatype
     * @return converted datatype or string
     */
    public static String convertDataTypeToEDCFieldType(String dataType){
        return switch (dataType) {
            case "code" -> "categorical";
            case "decimal" -> "decimal";
            case "integer", "count", "quantity", "duration" -> "int";
            case "boolean" -> "bool";
            case "date" -> "date";
            case "string", "identifier", "datetime" -> "string";
            default -> "string";
        };
    }

}
