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

package icrfgenerator.edc.edc.edcrunsettings.castor;

import icrfgenerator.edc.edc.CastorEDC;
import icrfgenerator.settings.runsettings.RunSettings;

/**
 * Castor specific runsettings
 */
public class CastorRunSettings extends RunSettings {
    public CastorRunSettings(){
        super(new CastorEDC());
    }

    /**
     * add an EDC specific container to store items for this EDC
     * @param key codebook+datasetId+language
     */
    @Override
    protected void addSelectedItemContainerEDC(String key) {
        selectedItemsContainerForCodebookDatasetLanguageMap.put(key, new CastorSelectedItemsContainer(key));
    }

    /**
     * returns the selected items contained for a codebook + datasetid + language
     * @param key codebook + datasetid + language
     * @return the selected items container
     */
    private CastorSelectedItemsContainer getSelectedItems(String key){
        return ((CastorSelectedItemsContainer) selectedItemsContainerForCodebookDatasetLanguageMap.get(key));
    }

    /**
     * update the field type of an item in a codebook+version+language
     * @param key codebook+version+language
     * @param itemId item to update
     * @param fieldType the new field type
     */
    public void updateItemFieldType(String key, String itemId, String fieldType){
        getSelectedItems(key).updateItemFieldType(itemId,fieldType);
    }

    /**
     * get the selected field type for an item in a codebook+version+language
     * @param key codebook+version+language
     * @param itemId id of item
     * @return field type
     */
    public String getSelectedItemFieldType(String key, String itemId){
        return getSelectedItems(key).getSelectedItemFieldType(itemId);
    }

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

    /**
     * update the required value
     * @param key codebook+version+language
     * @param itemId item to update
     * @param required the new required value
     */
    public void updateItemRequiredValue(String key, String itemId, boolean required){
        getSelectedItems(key).updateItemRequiredValue(itemId,required);
    }

    /**
     * update the width value
     * @param key codebook+version+language
     * @param itemId item to update
     * @param widthValue the new width value
     */
    public void updateItemWidthValue(String key, String itemId, String widthValue){
        getSelectedItems(key).updateItemWidthValue(itemId,widthValue);
    }

    /**
     * returns the item's required value
     * @param key codebook+version+language
     * @param itemId item to update
     * @return the item's required value
     */
    public boolean getSelectedItemRequiredValue(String key, String itemId){
        return getSelectedItems(key).getSelectedItemRequiredValue(itemId);
    }

    /**
     * returns the item's width value
     * @param key codebook+version+language
     * @param itemId item to update
     * @return the item's width value
     */
    public String getSelectedItemWidthValue(String key, String itemId){
        return getSelectedItems(key).getSelectedItemWidthValue(itemId);
    }
}
