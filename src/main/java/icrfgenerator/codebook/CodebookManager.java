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

package icrfgenerator.codebook;

import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.utils.GeneralUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * codebook manager keeps track of all the codebook that are loaded
 */
public class CodebookManager {
    private static final CodebookManager codebookManager = new CodebookManager();

    private final Map<String, CodebookMetaData> codebookMetaDataMap = new HashMap<>();
    private final Map<String, Codebook> codebookItemsMap = new HashMap<>();

    public static CodebookManager getInstance(){
        return codebookManager;
    }

    private CodebookManager(){

    }

    /**
     * Returns a single codebookItem for a specific item in codebook+datasetId+language
     * @param key codebook + datasetId + language
     * @param itemId id of an item in the codebook
     * @return a single CodebookItem
     */
    public CodebookItem getCodebookItem(String key, String itemId){
        return codebookItemsMap.get(key).getCodebookItem(itemId);
    }

    /**
     * returns whether an item in a codebook has a codelist
     * @param key codebook + datasetId + language
     * @param itemId id of an item in the codebook
     * @return true/false
     */
    public boolean codebookItemHasCodeList(String key, String itemId){
        return codebookItemsMap.get(key).getCodebookItem(itemId).hasCodeList();
    }

    /**
     * returns the name of an item
     * @param key codebook + datasetId + language
     * @param itemId id of an item in the codebook
     * @return the name of an item
     */
    public String getItemName(String key, String itemId){
        return codebookItemsMap.get(key).getCodebookItem(itemId).getItemName();
    }

    /**
     * returns the label for a code for an item
     * @param key codebook + datasetId + language
     * @param itemId id of an item in the codebook
     * @param code the code for which to retrieve the label
     * @return the label of the code
     */
    public String getValueForCode(String key, String itemId, String code){
        return codebookItemsMap.get(key).getCodebookItem(itemId).getValueForCode(code);
    }

    /**
     * Loads codebooks into memory.
     * For each codebook + datasetId + language that the user has selected, checks whether
     * the codebook is already stored in a hashmap. If it isn't, it creates a new codebook and
     * stores it in the map.
     */
    public void updateCodebookItems(){
        RunSettings runSettings = RunSettings.getInstance();
        List<String> codebookNames = runSettings.getSelectedCodebooks();
        // for each selected codebook
        for(String codebookName:codebookNames){
            // get the selected datasetIds
            List<String> datasetIds = runSettings.getCodebookSelectedDatasetIds(codebookName);
            // for each selected datasetId
            for(String datasetId:datasetIds){
                // get the selected languages
                List<String> languages = runSettings.getSelectedLanguages(codebookName, datasetId);
                // for each selected language
                for(String language:languages){
                    // get the key (codebookname + datasetId + language)
                    String key = GeneralUtils.getCodebookItemsMapKey(codebookName,datasetId,language);
                    // check whether this codebook already exists in the map
                    if(!codebookItemsMap.containsKey(key)){
                        // if it doesn't, addChild it
                        codebookItemsMap.put(key, new Codebook(datasetId, language, codebookName));
                    }
                }
            }
        }
    }

    /**
     * Update the metadata for the selected codebooks. This metadata consists of the datasetIds and languages available
     */
    public void updateCodebooksMetadata(){
        List<String> codebookNames = RunSettings.getInstance().getSelectedCodebooks();
        for(String codebookName:codebookNames){
            if(!codebookMetaDataMap.containsKey(codebookName)){
                codebookMetaDataMap.put(codebookName, new CodebookMetaData(codebookName));
            }
        }
    }

    /**
     * returns all the dataset identifiers that a codebook has
     * @param codebookName name of the codebook
     * @return a list with the codebooks dataset identifiers
     */
    public List<String> getDatasetIdentifiers(String codebookName){
        return codebookMetaDataMap.get(codebookName).getCodebookDatasetIdentifiers();
    }


    /**
     * returns the version of a codebook dataset
     * @param codebookName name of the codebook
     * @param datasetIdentifier identifier of the dataset
     * @return version of the dataset
     */
    public String getDatasetVersion(String codebookName, String datasetIdentifier){
        return codebookMetaDataMap.get(codebookName).getDatasetVersion(datasetIdentifier);
    }

    /**
     * returns the name of a dataset
     * @param codebookName name of the codebook
     * @param datasetIdentfifier identifier of the dataset
     * @return name of the dataset
     */
    public String getDatasetName(String codebookName, String datasetIdentfifier){
        return codebookMetaDataMap.get(codebookName).getDatasetName(datasetIdentfifier);
    }

    /**
     * returns the effective date of a dataset
     * @param codebookName name of the codebook
     * @param datasetIdentifier identifier of the dataset
     * @return the effective date
     */
    public String getDatasetEffectiveDate(String codebookName, String datasetIdentifier){
        return codebookMetaDataMap.get(codebookName).getDatasetEffectiveDate(datasetIdentifier);
    }

    /**
     * returns a list with the available langues for a codebook+version
     * @param codebookName name of the codebook
     * @param datasetIdentifier version of the codebook
     * @return a list with the available langues for a codebook+version
     */
    public List<String> getLanguagesForCodebookDataset(String codebookName, String datasetIdentifier){
        return codebookMetaDataMap.get(codebookName).getDatasetLanguages(datasetIdentifier);
    }

    /**
     * returns the root node representing the tree structure of the codebook
     * @param key codebook+datasetid+language
     * @return root node of the structure
     */
    public CodebookStructureNode getCodebookTree(String key){
        return codebookItemsMap.get(key).getCodebookTree();
    }
}
