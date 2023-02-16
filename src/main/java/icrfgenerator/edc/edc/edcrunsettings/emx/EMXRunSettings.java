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

import java.util.List;

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
     * update the aggregateable value
     * @param key codebook+version+language
     * @param itemId item to update
     * @param required the new aggregateable value
     */
    public void updateItemAggregateableValue(String key, String itemId, boolean required){
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t->
            getSelectedItems(t).updateItemAggregateableValue(itemId,required)
        );
    }

    /**
     * update the nillable value
     * @param key codebook+version+language
     * @param itemId item to update
     * @param required the new nillable value
     */
    public void updateItemNillableValue(String key, String itemId, boolean required){
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t->
            getSelectedItems(t).updateItemNillableValue(itemId,required)
        );
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

}
