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

import icrfgenerator.edc.edc.EDC;
import icrfgenerator.settings.runsettings.RunSettings;

import java.util.List;

/**
 * Castor specific runsettings
 */
public class CastorRunSettings extends RunSettings {
    public CastorRunSettings(EDC edc){
        super(edc);
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
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t->
            getSelectedItems(t).updateItemFieldType(itemId,fieldType)
        );
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
     * update the required value
     * @param key codebook+version+language
     * @param itemId item to update
     * @param value the new enforce decimals value
     */
    public void updateItemEnforceDecimalsValue(String key, String itemId, boolean value){
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t->
            getSelectedItems(t).updateItemEnforceDecimalsValue(itemId,value)
        );
    }

    /**
     * returns the item's enforce decimals value
     * @param key codebook+version+language
     * @param itemId item to update
     * @return the item's enforce decimals value
     */
    public boolean getSelectedItemEnforceDecimalsValue(String key, String itemId){
        return getSelectedItems(key).getSelectedItemEnforceDecimalsValue(itemId);
    }
}
