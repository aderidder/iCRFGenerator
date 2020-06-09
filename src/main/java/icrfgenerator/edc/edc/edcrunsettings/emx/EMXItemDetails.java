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

package icrfgenerator.edc.edc.edcrunsettings.emx;

import icrfgenerator.settings.runsettings.ItemDetails;

/**
 * EMX specific item details
 */
class EMXItemDetails extends ItemDetails {
    private String dataType;
    private String minValue="";
    private String maxValue="";
//    private String width="";
    private boolean aggregateable;
    private boolean nillable;
    

    /**
     * constructor
     * @param key codebook + datasetid + language
     * @param itemId id of the item
     */
    EMXItemDetails(String key, String itemId){
        super(key, itemId);
    }

    /**
     * set the item's data type
     * @param dataType the new item type
     */
    void setItemDataType(String dataType){
        this.dataType = dataType;
    }

    /**
     * returns the item's data type
     * @return the item's data type
     */
    String getDataType(){
        return dataType;
    }

    /**
     * set the minvalue for this item
     * @param minValue the min value
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
//
//    /**
//     * set the item's width value
//     * @param width the item's width value
//     */
//    void setItemWidthValue(String width){
//        this.width = width;
//    }
//
//    /**
//     * get the item's width value
//     * @return the item's width value
//     */
//    String getItemWidthValue(){
//        return width;
//    }

    /**
     * set the item's aggregateable value
     * @param aggregateable the item's aggregateable value
     */
    void setItemAggregateableValue(boolean aggregateable){
        this.aggregateable =aggregateable;
    }

    /**
     * set the item's nillable value
     * @param nillable the item's aggregateable value
     */
    void setItemNillableValue(boolean nillable){
        this.nillable =nillable;
    }

    /**
     * get the item's aggregateable value
     * @return the item's aggregateable value
     */
    boolean getItemAggregateableValue(){
        return aggregateable;
    }

    /**
     * get the item's nillable value
     * @return the item's nillable  value
     */
    boolean getItemNillableValue(){
        return nillable;
    }




}
