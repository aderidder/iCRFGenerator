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

package icrfgenerator.edc.edc.edcrunsettings.redcap;

import icrfgenerator.settings.runsettings.ItemDetails;

/**
 * REDCap specific item details
 */
class REDCapItemDetails extends ItemDetails {
    private String fieldType;
    private String textValidationType="";
    private String minValue="";
    private String maxValue="";

    /**
     * constructor
     * @param key codebook + datasetid + language
     * @param itemId id of the item
     */
    REDCapItemDetails(String key, String itemId){
        super(key, itemId);
    }

    /**
     * set the text validation type for this item
     * @param textValidationType the text validation type
     */
    void setTextValidationType(String textValidationType){
        this.textValidationType = textValidationType;
    }

    /**
     * set the item type  for this item
     * @param fieldType the item type
     */
    void setItemFieldType(String fieldType){
        this.fieldType = fieldType;
    }

    /**
     * get the item's text validation type
     * @return the item's text validation type
     */
    String getTextValidationType(){
        return textValidationType;
    }

    /**
     * returns the item's field type
     * @return the item's field type
     */
    String getFieldType(){
        return fieldType;
    }

    /**
     * set the max value for this item
     * @param minValue the max value
     */
    void setItemMinValue(String minValue){
        this.minValue = minValue;
    }

    /**
     * set the max value for this item
     * @param maxValue the max value
     */
    void setItemMaxValue(String maxValue){
        this.maxValue = maxValue;
    }

    /**
     * get the item's min value
     * @return the item's min value
     */
    String getItemMinValue(){
        return minValue;
    }

    /**
     * get the item's max value
     * @return the item's max value
     */
    String getItemMaxValue(){
        return maxValue;
    }
}
