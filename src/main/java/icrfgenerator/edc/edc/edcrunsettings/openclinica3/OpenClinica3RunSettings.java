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

package icrfgenerator.edc.edc.edcrunsettings.openclinica3;

import icrfgenerator.edc.edc.OpenClinica3EDC;
import icrfgenerator.settings.runsettings.RunSettings;

/**
 * OpenClinica 3 specific item details
 */
public class OpenClinica3RunSettings extends RunSettings {
    public OpenClinica3RunSettings(){
        super(new OpenClinica3EDC());
    }

    /**
     * add an EDC specific container to store items for this EDC
     * @param key codebook+datasetId+language
     */
    @Override
    protected void addSelectedItemContainerEDC(String key) {
        selectedItemsContainerForCodebookDatasetLanguageMap.put(key, new OpenClinica3SelectedItemsContainer(key));
    }

    /**
     * update the data type of an item in a codebook+version+language
     * @param key codebook+version+language
     * @param itemId item to update
     * @param dataType the new data type
     */
    public void updateItemDataType(String key, String itemId, String dataType){
        ((OpenClinica3SelectedItemsContainer) selectedItemsContainerForCodebookDatasetLanguageMap.get(key)).updateItemDataType(itemId,dataType);
    }

    /**
     * update the field type of an item in a codebook+version+language
     * @param key codebook+version+language
     * @param itemId item to update
     * @param fieldType the new field type
     */
    public void updateItemFieldType(String key, String itemId, String fieldType){
        ((OpenClinica3SelectedItemsContainer) selectedItemsContainerForCodebookDatasetLanguageMap.get(key)).updateItemFieldType(itemId,fieldType);
    }

    /**
     * get the data type for an item in a codebook+version+language
     * @param key codebook+version+language
     * @param itemId id of item
     * @return data type
     */
    public String getSelectedItemDataType(String key, String itemId){
        return ((OpenClinica3SelectedItemsContainer) selectedItemsContainerForCodebookDatasetLanguageMap.get(key)).getSelectedItemDataType(itemId);
    }

    /**
     * get the selected field type for an item in a codebook+version+language
     * @param key codebook+version+language
     * @param itemId id of item
     * @return field type
     */
    public String getSelectedItemFieldType(String key, String itemId){
        return ((OpenClinica3SelectedItemsContainer) selectedItemsContainerForCodebookDatasetLanguageMap.get(key)).getSelectedItemFieldType(itemId);
    }

}
