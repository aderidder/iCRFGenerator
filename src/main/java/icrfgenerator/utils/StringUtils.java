
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

public class StringUtils {
    /**
     * removes all spaces from a string
     * @param string string
     * @return string without spaces
     */
    public static String removeSpacesFromString(String string){
        return string.replaceAll(" ","_");
    }


    /**
     * escapes comma in string
     * @param string string
     * @return string with escaped commas
     */
    public static String escapeCommas(String string){
        return string.replaceAll(",", "\\\\,");
    }

    /**
     * escapes comma in string
     * @param string string
     * @return string with escaped commas
     */
    public static String addQuotationMarks(String string){
        return "\""+string+"\"";
    }

    public static String prepareValueForXML(String value){
        value = value.replaceAll("&", "&amp;");
        value = value.replaceAll("<", "&lt;");
        value = value.replaceAll(">", "&gt;");
        value = value.replaceAll("'", "&apos;");
        value = value.replaceAll("\"", "&quot;");
        value = value.replaceAll("≥", "&gt;=");
        value = value.replaceAll("≤", "&lt;=");
        return value.trim();
    }

}
