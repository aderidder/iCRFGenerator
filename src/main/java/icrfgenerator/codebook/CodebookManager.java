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

import icrfgenerator.codebook.shared.CodebookStructureNode;

import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.utils.KeyUtils;

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

    private CodebookManager(){}

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
        return codebookItemsMap.get(key).codebookItemHasCodeList(itemId);
    }

    /**
     * returns the name of an item
     * @param key codebook + datasetId + language
     * @param itemId id of an item in the codebook
     * @return the name of an item
     */
    public String getItemName(String key, String itemId){
       return codebookItemsMap.get(key).getItemName(itemId);
    }

    /**
     * returns the label for a code for an item
     * @param key codebook + datasetId + language
     * @param itemId id of an item in the codebook
     * @param code the code for which to retrieve the label
     * @return the label of the code
     */
    public String getValueForOptionCode(String key, String itemId, String code){
        return codebookItemsMap.get(key).getValueForOptionCode(itemId, code);
    }

    /**
     * returns the codesystem for a code for an item
     * @param key codebook + datasetId + language
     * @param itemId id of an item in the codebook
     * @param code the code for which to retrieve the label
     * @return the codesystem of the code
     */
    public String getCodesystemForOptionCode(String key, String itemId, String code){
        return codebookItemsMap.get(key).getCodesystemForOptionCode(itemId, code);
    }

    /**
     * returns the description for a code for an item
     * @param key codebook + datasetId + language
     * @param itemId id of an item in the codebook
     * @param code the code for which to retrieve the label
     * @return the description of the code
     */
    public String getDescriptionForOptionCode(String key, String itemId, String code){
        return codebookItemsMap.get(key).getDescriptionForOptionCode(itemId, code);
    }

    /**
     * returns the codesystem for an item
     * @param key codebook + datasetId + language
     * @param itemId id of an item in the codebook
     * @return the codesystem for an item
     */
    public String getCodeSystemForItem(String key, String itemId){
        return codebookItemsMap.get(key).getCodeSystemForItem(itemId);
    }

    /**
     * returns the code for an item
     * @param key codebook + datasetId + language
     * @param itemId id of an item in the codebook
     * @return the code for an item
     */
    public String getCodeForItem(String key, String itemId){
        return codebookItemsMap.get(key).getCodeForItem(itemId);
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
                updateCodebookItemsLanguages(codebookName, datasetId, runSettings.getMainSimpleLanguage());
                runSettings.getOtherSimpleLanguages().forEach(t->updateCodebookItemsLanguages(codebookName, datasetId, t));
            }
        }
    }

    /**
     * next step in the updateCodebookItems process. Loads the codebooks and stores them in a map
     * @param codebookName   name of the codebook
     * @param datasetId      id of the codebook
     * @param simpleLanguage language of the codebook
     */
    private void updateCodebookItemsLanguages(String codebookName, String datasetId, String simpleLanguage){
        String key = KeyUtils.getSimpleLanguageKey(codebookName, datasetId, simpleLanguage);

        // To be able to get the codebook(s), we need to know the full language, since e.g. ART-DECOR uses this
        // in the rest-call. We'll grab the extendedLanguage and assume there's a 1-1 mapping (e.g. en --> "en-US" and not
        // "en" --> "en-US", "en-UK"
        String extendedLanguage = CodebookManager.getInstance().getDatasetExtendedLanguagesForSimpleLanguage(codebookName, datasetId, simpleLanguage).get(0);

        // check whether this codebook already exists in the map
        if(!codebookItemsMap.containsKey(key)){
            // if it doesn't, add it
            codebookItemsMap.put(key, CodebookFactory.generateCodebook(datasetId, extendedLanguage, codebookName));
        }
    }

    /**
     * Update the metadata for the selected codebooks. This metadata consists of the datasetIds and languages available
     */
    public void updateCodebooksMetadata(){
        List<String> codebookNames = RunSettings.getInstance().getSelectedCodebooks();
        for(String codebookName:codebookNames){
            if(!codebookMetaDataMap.containsKey(codebookName)){
                codebookMetaDataMap.put(codebookName, CodebookMetaDataFactory.getMetaData(codebookName));
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
     * returns a list with the available languages for a dataset
     * @param codebookName      name of the codebook
     * @param datasetIdentifier identifier of the dataset
     * @return list with the available languages for a dataset
     */
    public List<String> getDatasetSimpleLanguages(String codebookName, String datasetIdentifier){
        return codebookMetaDataMap.get(codebookName).getDatasetSimpleLanguages(datasetIdentifier);
    }

    /**
     * returns a list with a mapping of the simpleLanguage to the extendedLanguage for a dataset.
     * E.g. calling it with "en" could return "en-US", "en-UK", etc.
     * Caution: the program does assume at some point that there is only a 1-1 mapping and that if a codeboook
     * is available for en-US, it is not also available for en-UK.
     * @param codebookName      name of the codebook
     * @param datasetIdentifier identifier of the dataset
     * @param simpleLanguage    simple language, e.g. en
     * @return a list with the extended names for the codebook for the simple language
     */
    public List<String> getDatasetExtendedLanguagesForSimpleLanguage(String codebookName, String datasetIdentifier, String simpleLanguage){
        return codebookMetaDataMap.get(codebookName).getDatasetExtendedLanguagesForSimpleLanguage(datasetIdentifier, simpleLanguage);
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
