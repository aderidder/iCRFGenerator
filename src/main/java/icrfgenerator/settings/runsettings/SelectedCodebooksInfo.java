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

package icrfgenerator.settings.runsettings;

import java.util.*;

/**
 * class with information on a selected codebook: selected version and languages
 */
class SelectedCodebooksInfo {
    private String codebook;


    private List<String> selectedDatasetIds = new ArrayList<>();
    private Map<String, List<String>> datasetIdToSelectedLanguagesMap = new HashMap<>();

    SelectedCodebooksInfo(String codebook){
        this.codebook = codebook;
    }

    /**
     * update the selected datasetId and languages
     * @param datasetId datasetId of the codebook
     * @param languages list of languages the user has selected for this codebook
     */
    void setDatasetSelectedLanguages(String datasetId, List<String> languages){
        // if no language is selected, we will have nothing to do for this datasetId, so
        // set remove the datasetId from the selected datasetId list
        if(languages.size()==0){
            selectedDatasetIds.remove(datasetId);
        }
        // otherwise add it if it isn't already in the list
        else if(!selectedDatasetIds.contains(datasetId)){
            selectedDatasetIds.add(datasetId);
        }
        // update the selected languages for the datasetId
        datasetIdToSelectedLanguagesMap.put(datasetId, languages);
    }

    /**
     * if a dataset has been deselected, remove it from the selection list and also remove
     * the associated languages
     * @param datasetId datasetId to remove
     */
    void clearDatasetSelectedLanguages(String datasetId){
        datasetIdToSelectedLanguagesMap.remove(datasetId);
        selectedDatasetIds.remove(datasetId);
    }

    /**
     * get a sorted list of the selected datasetIds
     * @return sorted list of the selected datasetIds
     */
    List<String> getSelectedDatasetIds(){
        Collections.sort(selectedDatasetIds);
        return selectedDatasetIds;
    }

    /**
     * get a list of the selected languages for this datasetId
     * @param datasetId datasetId
     * @return list of the selected languages for this datasetId
     */
    List<String> getSelectedLanguages(String datasetId){
        if(datasetIdToSelectedLanguagesMap.containsKey(datasetId)) {
            return datasetIdToSelectedLanguagesMap.get(datasetId);
        }
        return new ArrayList<>();
    }

    /**
     * feedback / test messages
     */
    void printDatasetIdSelectedLanguages(){
        System.out.println("Codebook: "+codebook);
        for (String datasetId: datasetIdToSelectedLanguagesMap.keySet()){
            System.out.println("DatasetId: "+datasetId);
            System.out.println("Languages: "+ String.join(", ", datasetIdToSelectedLanguagesMap.get(datasetId)));
//                System.out.println("Languages: "+datasetIdToSelectedLanguagesMap.get(datasetId).stream().collect(Collectors.joining(", ")));
        }
    }
}