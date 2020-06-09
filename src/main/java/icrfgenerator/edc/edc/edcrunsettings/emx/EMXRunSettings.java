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

import icrfgenerator.edc.edc.EDC;
import icrfgenerator.settings.runsettings.RunSettings;

/**
 * EMX specific runsettings
 */
public class EMXRunSettings extends RunSettings {
    public EMXRunSettings(EDC edc){
        super(edc);
    }

    /**
     * add an EDC specific container to store items for this EDC
     * @param key codebook+datasetId+language
     */
    @Override
    protected void addSelectedItemContainerEDC(String key) {
        selectedItemsContainerForCodebookDatasetLanguageMap.put(key, new EMXSelectedItemsContainer(key));
    }

    /**
     * returns the selected items contained for a codebook + datasetid + language
     * @param key codebook + datasetid + language
     * @return the selected items container
     */
    private EMXSelectedItemsContainer getSelectedItems(String key){
        return ((EMXSelectedItemsContainer) selectedItemsContainerForCodebookDatasetLanguageMap.get(key));
    }

    /**
     * update the data type of an item in a codebook+version+language
     * @param key codebook+version+language
     * @param itemId item to update
     * @param dataType the new data type
     */
    public void updateItemDataType(String key, String itemId, String dataType){
        getSelectedItems(key).updateItemDataType(itemId,dataType);
    }

    /**
     * get the selected data type for an item in a codebook+version+language
     * @param key codebook+version+language
     * @param itemId id of item
     * @return data type
     */
    public String getSelectedItemDataType(String key, String itemId){
        return getSelectedItems(key).getSelectedItemDataType(itemId);
    }

    /**
     * update the aggregateable value
     * @param key codebook+version+language
     * @param itemId item to update
     * @param required the new aggregateable value
     */
    public void updateItemAggregateableValue(String key, String itemId, boolean required){
        getSelectedItems(key).updateItemAggregateableValue(itemId,required);
    }

    /**
     * update the nillable value
     * @param key codebook+version+language
     * @param itemId item to update
     * @param required the new nillable value
     */
    public void updateItemNillableValue(String key, String itemId, boolean required){
        getSelectedItems(key).updateItemNillableValue(itemId,required);
    }

    /**
     * returns the item's aggregateable value
     * @param key codebook+version+language
     * @param itemId item to update
     * @return the item's required value
     */
    public boolean getSelectedItemAggregateableValue(String key, String itemId){
        return getSelectedItems(key).getSelectedItemAggregateableValue(itemId);
    }

    /**
     * returns the item's nillable value
     * @param key codebook+version+language
     * @param itemId item to update
     * @return the item's nillable value
     */
    public boolean getSelectedItemNillableValue(String key, String itemId){
        return getSelectedItems(key).getSelectedItemNillableValue(itemId);
    }

//
//    /**
//     * returns the item's width value
//     * @param key codebook+version+language
//     * @param itemId item to update
//     * @return the item's width value
//     */
//    public String getSelectedItemWidthValue(String key, String itemId){
//        return getSelectedItems(key).getSelectedItemWidthValue(itemId);
//    }
//
    /**
     * update the min value
     * @param key codebook+version+language
     * @param itemId item to update
     * @param minValue the new min value
     */
    public void updateItemMinValue(String key, String itemId, String minValue){
        getSelectedItems(key).updateItemMinValue(itemId,minValue);
    }

    /**
     * update the max value
     * @param key codebook+version+language
     * @param itemId item to update
     * @param maxValue the new max value
     */
    public void updateItemMaxValue(String key, String itemId, String maxValue){
        getSelectedItems(key).updateItemMaxValue(itemId,maxValue);
    }

    /**
     * returns the item's min value
     * @param key codebook+version+language
     * @param itemId item to update
     * @return the item's min value
     */
    public String getSelectedItemMinValue(String key, String itemId){
        return getSelectedItems(key).getSelectedItemMinValue(itemId);
    }

    /**
     * returns the item's max value
     * @param key codebook+version+language
     * @param itemId item to update
     * @return the item's max value
     */
    public String getSelectedItemMaxValue(String key, String itemId){
        return getSelectedItems(key).getSelectedItemMaxValue(itemId);
    }
//
//    /**
//     * update the width value
//     * @param key codebook+version+language
//     * @param itemId item to update
//     * @param widthValue the new width value
//     */
//    public void updateItemWidthValue(String key, String itemId, String widthValue){
//        getSelectedItems(key).updateItemWidthValue(itemId,widthValue);
//    }
}
