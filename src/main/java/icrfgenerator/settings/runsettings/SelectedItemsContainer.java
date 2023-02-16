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

import java.util.*;

/**
 * keeps track of the items selected in a codebook+version+language
 * all EDCs are expected to create an extension of this class to allow for their specifics
 */
public abstract class SelectedItemsContainer {
    private final List<String> selectedItemsList = new ArrayList<>();
    private final String key;

    protected Map<String, ItemDetails> itemDetailsMap = new HashMap<>();

    /**
     * constructor for a selected items container
     * @param key codebook+datasetId+language
     */
    protected SelectedItemsContainer(String key){
        this.key = key;
    }

    /**
     * abstract method which is necessary for every EDC to add an EDC-specific item
     * @param key codebook+datasetId+language
     * @param itemId id of the item
     */
    abstract protected void addItemEDC(String key, String itemId);

    /**
     * returns whether the item is selected
     * @param itemId id of the item
     * @return true/false
     */
    boolean isItemSelected(String itemId){
        return selectedItemsList.contains(itemId);
    }

    /**
     * returns the selected codes + values for an item
     * @param itemId item id
     * @return set with codes + values
     */
    List<String> getTerminologyCodes(String itemId){
        if(itemDetailsMap.containsKey(itemId)) {
            itemDetailsMap.get(itemId).getSelectedTerminologyCodes().sort(new SortByCode());
            return itemDetailsMap.get(itemId).getSelectedTerminologyCodes();
        }
        return new ArrayList<>();
    }

    /**
     * add the selected code + value for the item
     * @param itemId id of the item
     * @param code selected code
     */
    void addTerminology(String itemId, String code){
        itemDetailsMap.get(itemId).addSelectedTerminologyCode(code);
    }

    /**
     * remove the selected code for the item
     * @param itemId id of the item
     * @param code code to remove
     */
    void removeTerminology(String itemId, String code){
        itemDetailsMap.get(itemId).removeSelectedTerminologyCode(code);
    }

    /**
     * remove all terminology codes for an item
     * @param itemId id of the item
     */
    void removeTerminologies(String itemId){
        itemDetailsMap.get(itemId).removeSelectedTerminologyCodes();
    }

    /**
     * add the item the user just selected
     * @param itemId id of item
     */
    void addItem(String itemId){
        if(!selectedItemsList.contains(itemId)){
            selectedItemsList.add(itemId);
        }
        if(!itemDetailsMap.containsKey(itemId)){
            addItemEDC(key, itemId);
        }
    }

    /**
     * remove the item, after the user deselected it
     * @param itemId id of item
     */
    void removeItem(String itemId){
        selectedItemsList.remove(itemId);
    }

    /**
     * returns list of selected items
     * @return list of selected items
     */
    List<String> getSelectedItemsList(){
        return selectedItemsList;
    }

    /**
     * update the data type of the item
     * @param itemId id of item
     * @param dataType data type
     */
    void updateItemDataType(String itemId, String dataType){
        itemDetailsMap.get(itemId).setItemDataType(dataType);
    }

    /**
     * returns data type for selected item
     * @param itemId id of item
     * @return data type
     */
    String getSelectedItemDataType(String itemId){
        return itemDetailsMap.get(itemId).getDataType();
    }

    /**
     * update the required value of the item
     * @param itemId id of the item
     * @param value the new required value
     */
    void updateItemRequiredValue(String itemId, boolean value){
        itemDetailsMap.get(itemId).setItemRequiredValue(value);
    }

    /**
     * returns the required value of the item
     * @param itemId id of the item
     * @return the required value
     */
    boolean getSelectedItemRequiredValue(String itemId){
        return itemDetailsMap.get(itemId).getItemRequiredValue();
    }

    /**
     * update the min value of the item
     * @param itemId id of the item
     * @param value the new min value
     */
    void updateItemMinValue(String itemId, String value){
        itemDetailsMap.get(itemId).setItemMinValue(value);
    }

    /**
     * update the min value operator
     * @param itemId           id of the item
     * @param minCheckOperator new operator
     */
    void updateItemMinCheckFieldValue(String itemId, OperatorType minCheckOperator){
        itemDetailsMap.get(itemId).setItemMinCheckFieldValue(minCheckOperator);
    }

    /**
     * returns the min value operator
     * @param itemId id of the item
     * @return the min value operator
     */
    OperatorType getItemMinCheckFieldValue(String itemId){
        return itemDetailsMap.get(itemId).getItemMinCheckFieldValue();
    }

    /**
     * update the max value of the item
     * @param itemId id of the item
     * @param value  the new max value
     */
    void updateItemMaxValue(String itemId, String value){
        itemDetailsMap.get(itemId).setItemMaxValue(value);
    }

    /**
     * update the max value operator
     * @param itemId           id of the item
     * @param maxCheckOperator new operator
     */
    void updateItemMaxCheckFieldValue(String itemId, OperatorType maxCheckOperator){
        itemDetailsMap.get(itemId).setItemMaxCheckFieldValue(maxCheckOperator);
    }

    /**
     * returns the max value operator
     * @param itemId id of the item
     * @return the max value operator
     */
    OperatorType getItemMaxCheckFieldValue(String itemId){
        return itemDetailsMap.get(itemId).getItemMaxCheckFieldValue();
    }

    /**
     * returns the min value for selected item
     * @param itemId id of item
     * @return the min value
     */
    String getSelectedItemMinValue(String itemId){
        return itemDetailsMap.get(itemId).getItemMinValue();
    }

    /**
     * returns the max value for selected item
     * @param itemId id of item
     * @return the max value
     */
    String getSelectedItemMaxValue(String itemId){
        return itemDetailsMap.get(itemId).getItemMaxValue();
    }

    /**
     * update the width value of the item
     * @param itemId id of the item
     * @param value the new units value
     */
    void updateItemUnitsValue(String itemId, String value){
        itemDetailsMap.get(itemId).setItemUnitsValue(value);
    }

    /**
     * returns the units value of the item
     * @param itemId id of the item
     * @return the units value
     */
    String getSelectedItemUnitsValue(String itemId){
        return itemDetailsMap.get(itemId).getItemUnitsValue();
    }

    /**
     * sort the selected terminology codes
     */
    static class SortByCode implements Comparator<String> {
        @Override
        public int compare(String code1, String code2) {
            try{
                int code1Int = Integer.parseInt(code1);
                int code2Int = Integer.parseInt(code2);
                if(code1Int>code2Int) return 1;
                else if(code1Int<code2Int) return -1;
                return 0;
            }catch (Exception e) {
                return code1.compareToIgnoreCase(code2);
            }
        }
    }

}