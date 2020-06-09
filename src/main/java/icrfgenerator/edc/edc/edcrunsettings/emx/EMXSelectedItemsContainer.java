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

import icrfgenerator.settings.runsettings.SelectedItemsContainer;

/**
 * EMX specific item container
 */
public class EMXSelectedItemsContainer extends SelectedItemsContainer {
    EMXSelectedItemsContainer(String key){
        super(key);
    }

    /**
     * add an EDC specific item to the container
     * @param key codebook+datasetId+language
     * @param itemId id of the item
     */
    @Override
    protected void addItemEDC(String key, String itemId) {
        itemDetailsMap.put(itemId, new EMXItemDetails(key, itemId));
    }

    /**
     * private helper function to get the item details from the map
     * @param key codebook+datasetId+language
     * @return EMXItemDetails for the key
     */
    private EMXItemDetails getItemDetails(String key){
        return ((EMXItemDetails) itemDetailsMap.get(key));
    }

    /**
     * update the data type of the item
     * @param itemId id of item
     * @param dataType data type
     */
    void updateItemDataType(String itemId, String dataType){
        getItemDetails(itemId).setItemDataType(dataType);
    }

    /**
     * returns data type for selected item
     * @param itemId id of item
     * @return data type
     */
    String getSelectedItemDataType(String itemId){
        return getItemDetails(itemId).getDataType();
    }

    /**
     * update the nillable value of the item
     * @param itemId id of the item
     * @param value the new required value
     */
    void updateItemNillableValue(String itemId, boolean value){
        getItemDetails(itemId).setItemNillableValue(value);
    }

    /**
     * returns the nillable value of the item
     * @param itemId id of the item
     * @return the nillable value
     */
    boolean getSelectedItemNillableValue(String itemId){
        return getItemDetails(itemId).getItemNillableValue();
    }

    /**
     * update the aggregateable value of the item
     * @param itemId id of the item
     * @param value the new required value
     */
    void updateItemAggregateableValue(String itemId, boolean value){
        getItemDetails(itemId).setItemAggregateableValue(value);
    }

    /**
     * returns the aggregateable value of the item
     * @param itemId id of the item
     * @return the aggregateable value
     */
    boolean getSelectedItemAggregateableValue(String itemId){
        return getItemDetails(itemId).getItemAggregateableValue();
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
     * returns the min value of the item
     * @param itemId id of the item
     * @return the min value
     */
    String getSelectedItemMinValue(String itemId){
        return getItemDetails(itemId).getItemMinValue();
    }

    /**
     * returns the max value of the item
     * @param itemId id of the item
     * @return the max value
     */
    String getSelectedItemMaxValue(String itemId){
        return getItemDetails(itemId).getItemMaxValue();
    }


//    /**
//     * update the width value of the item
//     * @param itemId id of the item
//     * @param value the new widthvalue
//     */
//    void updateItemWidthValue(String itemId, String value){
//        getItemDetails(itemId).setItemWidthValue(value);
//    }
//
//    /**
//     * returns the width value of the item
//     * @param itemId id of the item
//     * @return the width value
//     */
//    String getSelectedItemWidthValue(String itemId){
//        return getItemDetails(itemId).getItemWidthValue();
//    }
}
