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

import icrfgenerator.settings.runsettings.ItemDetails;

/**
 * OpenClinica 3 specific item details
 */
class OpenClinica3ItemDetails extends ItemDetails {
    private String dataType;
    private String fieldType;

    OpenClinica3ItemDetails(String key, String itemId){
        super(key, itemId);
    }

    /**
     * update the data type
     * @param dataType the new data type
     */
    void setItemDataType(String dataType){
        this.dataType = dataType;
    }

    /**
     * update the item type
     * @param fieldType the new item type
     */
    void setItemFieldType(String fieldType){
        this.fieldType = fieldType;
    }

    /**
     * returns the item's data type
     * @return the item's data type
     */
    String getDataType(){
        return dataType;
    }

    /**
     * returns the item's field type
     * @return the item's field type
     */
    String getFieldType(){
        return fieldType;
    }
}
