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

import icrfgenerator.settings.runsettings.SelectedItemsContainer;

/**
 * REDCap specific item container
 */
public class REDCapSelectedItemsContainer extends SelectedItemsContainer {
    REDCapSelectedItemsContainer(String key){
        super(key);
    }

    /**
     * add an EDC specific item to the container
     * @param key codebook+datasetId+language
     * @param itemId id of the item
     */
    @Override
    protected void addItemEDC(String key, String itemId) {
        itemDetailsMap.put(itemId, new REDCapItemDetails(key, itemId));
    }

    /**
     * private helper function to get the item details from the map
     * @param key codebook+datasetId+language
     * @return CastorItemDetails for the key
     */
    private REDCapItemDetails getItemDetails(String key){
        return ((REDCapItemDetails) itemDetailsMap.get(key));
    }


    /**
     * update the field type of the item
     * @param itemId id of item
     * @param fieldType field type
     */
    void updateItemFieldType(String itemId, String fieldType){
        getItemDetails(itemId).setItemFieldType(fieldType);
    }

    /**
     * update the text validation type of the item
     * @param itemId id of the item
     * @param value the new text validation type
     */
    void updateItemTextValidationType(String itemId, String value){
        getItemDetails(itemId).setTextValidationType(value);
    }

    /**
     * returns field type for selected item
     * @param itemId id of item
     * @return the field type
     */
    String getSelectedItemFieldType(String itemId){
        return getItemDetails(itemId).getFieldType();
    }

    /**
     * returns text validation type for selected item
     * @param itemId id of item
     * @return the text validation type
     */
    String getSelectedItemTextValidationType(String itemId){
        return getItemDetails(itemId).getTextValidationType();
    }

    /**
     * update the min value of the item
     * @param itemId id of the item
     * @param value the new min value
     */
    void updateItemMinValue(String itemId, String value){
        getItemDetails(itemId).setItemMinValue(value);
    }

    /**
     * update the max value of the item
     * @param itemId id of the item
     * @param value the new max value
     */
    void updateItemMaxValue(String itemId, String value){
        getItemDetails(itemId).setItemMaxValue(value);
    }

    /**
     * returns the min value for selected item
     * @param itemId id of item
     * @return the min value
     */
    String getSelectedItemMinValue(String itemId){
        return getItemDetails(itemId).getItemMinValue();
    }

    /**
     * returns the max value for selected item
     * @param itemId id of item
     * @return the max value
     */
    String getSelectedItemMaxValue(String itemId){
        return getItemDetails(itemId).getItemMaxValue();
    }

}
