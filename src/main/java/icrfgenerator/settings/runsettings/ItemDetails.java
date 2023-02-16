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

package icrfgenerator.settings.runsettings;

import icrfgenerator.types.OperatorType;

import java.util.ArrayList;
import java.util.List;

/**
 * details of a selected item
 * extended by all  edc specific item details
*/
public class ItemDetails{
    private final String key;
    private final String itemId;
    private final List<String> codesList = new ArrayList<>();

    // items which most EDCs will want to use
    private String dataType;
    private boolean required;
    private String minValue="";
    private String maxValue="";
    private String units ="";

    private OperatorType maxCheckOperator;
    private OperatorType minCheckOperator;

    /**
     * itemdetails constructor
     * @param key used as a reference to the codebook
     * @param itemId this item's identifier
     */
    protected ItemDetails(String key, String itemId){
        this.key = key;
        this.itemId = itemId;
    }

    /**
     * add code & value the user just selected
     * @param code code
     */
    void addSelectedTerminologyCode(String code){
        if(!codesList.contains(code)){
            codesList.add(code);
        }
    }

    /**
     * remove the code the user deselected
     * @param code code
     */
    void removeSelectedTerminologyCode(String code){
        codesList.remove(code);
    }

    /**
     * clear all selected codes
     */
    void removeSelectedTerminologyCodes(){
        codesList.clear();
    }

    /**
     * get a list with the selected terminology codes
     * @return List with the selected terminology codes
     */
    List<String> getSelectedTerminologyCodes(){
        return codesList;
    }

    /**
     * returns the item's data type
     * @return the item's data type
     */
    String getDataType(){
        return dataType;
    }

    /**
     * set the item's data type
     * @param dataType the new item type
     */
    void setItemDataType(String dataType){
        this.dataType = dataType;
    }

    /**
     * get the item's required value
     * @return the item's required value
     */
    boolean getItemRequiredValue(){
        return required;
    }

    /**
     * set the item's required value
     * @param required the item's required value
     */
    void setItemRequiredValue(boolean required){
        this.required=required;
    }

    /**
     * get the item's min value
     * @return the item's min value
     */
    String getItemMinValue(){
        return minValue;
    }

    /**
     * set the min value for this item
     * @param minValue the max value
     */
    void setItemMinValue(String minValue){
        this.minValue = minValue;
    }

    /**
     * set the minimal check operator
     * @param minCheckOperator the operator
     */
    void setItemMinCheckFieldValue(OperatorType minCheckOperator){
        this.minCheckOperator = minCheckOperator;
    }

    /**
     * get the minimal check operator
     * @return the operator
     */
    OperatorType getItemMinCheckFieldValue(){
        return minCheckOperator;
    }

    /**
     * set the max value for this item
     * @param maxValue the max value
     */
    void setItemMaxValue(String maxValue){
        this.maxValue = maxValue;
    }

    /**
     * get the maximum check operator
     * @param maxCheckOperator the operator
     */
    void setItemMaxCheckFieldValue(OperatorType maxCheckOperator){
        this.maxCheckOperator = maxCheckOperator;
    }

    /**
     * get the maximum check operator
     * @return the operator
     */
    OperatorType getItemMaxCheckFieldValue(){
        return maxCheckOperator;
    }

    /**
     * get the item's max value
     * @return the item's max value
     */
    String getItemMaxValue(){
        return maxValue;
    }

    /**
     * get the item's width value
     * @return the item's width value
     */
    String getItemUnitsValue(){
        return units;
    }

    /**
     * set the item's units value
     * @param units the item's units value
     */
    void setItemUnitsValue(String units){
        this.units = units;
    }

}
