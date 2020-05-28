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

package icrfgenerator.utils;

import icrfgenerator.settings.GlobalSettings;

/**
 * Class that gives the necessary calls to make to art-decor
 */
public class ArtDecorCalls {
    /**
     * returns the URI to retrieve a dataset (codebook) for the specific language
     * @param codebook     name of the codebook, necessary to determine the server
     * @param datasetId    identifier of the dataset to retrieve
     * @param langFrom     language of the source data
     * @return the URI that can be used to retrieve the dataset
     */
    static String getRetrieveDatasetURI(String codebook, String datasetId, String langFrom){
        return GlobalSettings.getServer(codebook)+"RetrieveDataSet?id="+datasetId+"&language="+langFrom+"&format=xml";
    }

    /**
     * returns the URI to retrieve project information, which contains information on which
     * dataset version are available, as well as their dataset identifiers
     * this information we can then use to retrieve the dataset (=codebook)
     * @param codebook     name of the codebook, necessary to determine the server
     * @param prefix    prefix of the codebook
     * @return  URI which contains project information
     */
    public static String getProjectIndexURI(String codebook, String prefix){
        return GlobalSettings.getServer(codebook)+"ProjectIndex?view=d&prefix="+prefix+"&format=xml";
    }
}
