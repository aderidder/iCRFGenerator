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

import icrfgenerator.settings.runsettings.ItemDetails;

/**
 * Castor specific item details
 */
class CastorItemDetails extends ItemDetails {
    private String fieldType;
    private boolean enforceDecimals;

    /**
     * constructor
     * @param key codebook + datasetid + language
     * @param itemId id of the item
     */
    CastorItemDetails(String key, String itemId){
        super(key, itemId);
    }

    /**
     * set the item type
     * @param fieldType the new item type
     */
    void setItemFieldType(String fieldType){
        this.fieldType = fieldType;
    }

    /**
     * returns the item's field type
     * @return the item's field type
     */
    String getFieldType(){
        return fieldType;
    }

    /**
     * set the item's required value
     * @param enforceDecimals the item's required value
     */
    void setItemEnforceDecimalsValue(boolean enforceDecimals){
        this.enforceDecimals=enforceDecimals;
    }

    /**
     * get the item's enforce decimals value
     * @return the item's enforce decimals value
     */
    boolean getItemEnforceDecimalsValue(){
        return enforceDecimals;
    }

}
