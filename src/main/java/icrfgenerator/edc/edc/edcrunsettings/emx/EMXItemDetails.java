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

import icrfgenerator.settings.runsettings.ItemDetails;

/**
 * EMX specific item details
 */
class EMXItemDetails extends ItemDetails {
    private boolean aggregateable;
    private boolean nillable;

    /**
     * constructor
     * @param key codebook + datasetid + language
     * @param itemId id of the item
     */
    EMXItemDetails(String key, String itemId){
        super(key, itemId);
    }

    /**
     * set the item's aggregateable value
     * @param aggregateable the item's aggregateable value
     */
    void setItemAggregateableValue(boolean aggregateable){
        this.aggregateable =aggregateable;
    }

    /**
     * set the item's nillable value
     * @param nillable the item's aggregateable value
     */
    void setItemNillableValue(boolean nillable){
        this.nillable =nillable;
    }

    /**
     * get the item's aggregateable value
     * @return the item's aggregateable value
     */
    boolean getItemAggregateableValue(){
        return aggregateable;
    }

    /**
     * get the item's nillable value
     * @return the item's nillable  value
     */
    boolean getItemNillableValue(){
        return nillable;
    }

}
