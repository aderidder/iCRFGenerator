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

import icrfgenerator.settings.runsettings.SelectedItemsContainer;

/**
 * OpenClinica 3 specific item container
 */
public class OpenClinica3SelectedItemsContainer extends SelectedItemsContainer {
    OpenClinica3SelectedItemsContainer(String key){
        super(key);
    }

    /**
     * add an EDC specific item to the container
     * @param key codebook+datasetId+language
     * @param itemId id of the item
     */
    @Override
    protected void addItemEDC(String key, String itemId) {
        itemDetailsMap.put(itemId, new OpenClinica3ItemDetails(key, itemId));
    }

    /**
     * update the field type of the item
     * @param itemId id of item
     * @param fieldType field type
     */
    void updateItemFieldType(String itemId, String fieldType){
        ((OpenClinica3ItemDetails)itemDetailsMap.get(itemId)).setItemFieldType(fieldType);
    }

    /**
     * returns field type for selected item
     * @param itemId id of item
     * @return field type
     */
    String getSelectedItemFieldType(String itemId){
        return ((OpenClinica3ItemDetails)itemDetailsMap.get(itemId)).getFieldType();
    }

}
