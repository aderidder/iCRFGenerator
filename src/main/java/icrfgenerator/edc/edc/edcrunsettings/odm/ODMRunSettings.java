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

package icrfgenerator.edc.edc.edcrunsettings.odm;

import icrfgenerator.edc.edc.EDC;
import icrfgenerator.settings.runsettings.RunSettings;

/**
 * ODM specific runsettings
 */
public class ODMRunSettings extends RunSettings {
    public ODMRunSettings(EDC edc){
        super(edc);
    }

    /**
     * add an EDC specific container to store items for this EDC
     * @param key codebook+datasetId+language
     */
    @Override
    protected void addSelectedItemContainerEDC(String key) {
        selectedItemsContainerForCodebookDatasetLanguageMap.put(key, new ODMSelectedItemsContainer(key));
    }

    /**
     * returns the selected items contained for a codebook + datasetid + language
     * @param key codebook + datasetid + language
     * @return the selected items container
     */
    private ODMSelectedItemsContainer getSelectedItems(String key){
        return ((ODMSelectedItemsContainer) selectedItemsContainerForCodebookDatasetLanguageMap.get(key));
    }

}
