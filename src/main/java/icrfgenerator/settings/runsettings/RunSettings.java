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

import icrfgenerator.edc.edc.EDC;
import icrfgenerator.gui.i18n.I18N;

import java.util.*;

import static icrfgenerator.utils.GeneralUtils.getCodebookItemsMapKey;

/**
 * Runsettings keeps track of everything the user selects
 * It is an abstract class and all EDCs have their own RunSettings, which extends this one, to add their own specifics
 * as well as implement the defined abstract methods.
 *
 * The keys are the identifiers used to access the appropriate codebook in the codebook manager
 * at this point a key is: codebookName + datasetId + language
 * I've had to change this key definition in the past, due to the deviating ZIB structure, which ruined the
 * my previous key.
 */
public abstract class RunSettings{
    // there is one runsettings
    private static RunSettings runSettings;

    private EDC edc;
    private List<String> selectedCodebooks = new ArrayList<>();
    private Map<String, SelectedCodebooksInfo> selectedCodebooksInfoMap = new HashMap<>();
    protected Map<String, SelectedItemsContainer> selectedItemsContainerForCodebookDatasetLanguageMap = new HashMap<>();

    /**
     * Returns the runSettings, which we can access anywhere
     */
    public static RunSettings getInstance(){
        return runSettings;
    }

    /**
     * Constructor
     * @param edc the selected EDC
     */
    protected RunSettings(EDC edc){
        RunSettings.runSettings = this;
        this.edc = edc;
    }

    /**
     * get a list of the currently selected keys (codebookName + datasetId + language)
     * @return a list with the keys
     */
    public List<String> getKeys(){
        List<String> keyList = new ArrayList<>();
        // loop over the selected codebooks
        for(String codebookName:selectedCodebooks) {
            if(selectedCodebooksInfoMap.containsKey(codebookName)) {
                SelectedCodebooksInfo selectedCodebooksInfo = selectedCodebooksInfoMap.get(codebookName);
                List<String> selectedDatasetIds = selectedCodebooksInfo.getSelectedDatasetIds();
                // and the selected datasetIds in them
                for (String datasetId : selectedDatasetIds) {
                    List<String> selectedLanguages = selectedCodebooksInfo.getSelectedLanguages(datasetId);
                    // and the selected languages in these
                    for (String language : selectedLanguages) {
                        // to create the key list
                        keyList.add(getCodebookItemsMapKey(codebookName, datasetId, language));
                    }
                }
            }
        }
        return keyList;
    }

    /**
     * get the selected EDC
     * @return the selected EDC
     */
    public EDC getEDC(){
        return edc;
    }

    /**
     * add a selected codebook to the selected codebook list
     * @param codebook codebook to add
     */
    public void addSelectedCodebook(String codebook){
        selectedCodebooks.add(codebook);
    }

    /**
     * after a codebook has been deselected, remove it from the list
     * @param codebook codebook to remove
     */
    public void removeSelectedCodebook(String codebook){
        selectedCodebooks.remove(codebook);
    }

    /**
     * returns the selected codebooks, sorted
     * @return the selected sorted codebook list
     */
    public List<String> getSelectedCodebooks(){
        Collections.sort(selectedCodebooks);
        return selectedCodebooks;
    }

    /**
     * set the selected languages for a codebook+datasetId
     * @param codebook codebook
     * @param datasetId datasetId
     * @param languages language
     */
    public void setDatasetSelectedLanguages(String codebook, String datasetId, List<String> languages){
        if(!selectedCodebooksInfoMap.containsKey(codebook)){
            selectedCodebooksInfoMap.put(codebook, new SelectedCodebooksInfo(codebook));
        }
        selectedCodebooksInfoMap.get(codebook).setDatasetSelectedLanguages(datasetId, languages);
    }

    /**
     * remove the datasetId + languages for a codebook
     * @param codebook codebook
     * @param datasetId datasetId
     */
    public void clearDatasetSelectedLanguages(String codebook, String datasetId){
        selectedCodebooksInfoMap.get(codebook).clearDatasetSelectedLanguages(datasetId);
    }


    /**
     * prints the selected codebook, the selected datasetids and selected languages
     */
    public void printDatasetIdSelectedLanguages(){
        for(SelectedCodebooksInfo selectedCodebooksInfo:selectedCodebooksInfoMap.values()){
            selectedCodebooksInfo.printDatasetIdSelectedLanguages();
        }
    }

    /**
     * returns a list of the selected versions for a codebook or an empty list
     * @param codebook codebook for which to find the selected datasetIds
     * @return list with selected datasetIds or empty list
     */
    public List<String> getCodebookSelectedDatasetIds(String codebook){
        if(selectedCodebooksInfoMap.containsKey(codebook)) {
            return selectedCodebooksInfoMap.get(codebook).getSelectedDatasetIds();
        }
        return new ArrayList<>();
    }

    /**
     * returns a list of the selected languages for a codebook+datasetId
     * @param codebook codebook
     * @param datasetId datasetId
     * @return a list of the selected languages for a codebook+datasetId or empty list
     */
    public List<String> getSelectedLanguages(String codebook, String datasetId){
        if(selectedCodebooksInfoMap.containsKey(codebook)) {
            return selectedCodebooksInfoMap.get(codebook).getSelectedLanguages(datasetId);
        }
        return new ArrayList<>();
    }


    /**
     * returns whether there is any selected dataset + language
     * this information is used to determine whether a user can proceed to the next page in the wizard
     * @return true/false
     */
    public boolean oneOrMoreSelectedDatasetsAndLanguages(){
        for(String codebook:selectedCodebooks){
            List<String> selectedDatasets = getCodebookSelectedDatasetIds(codebook);
            if(selectedDatasets.stream().anyMatch(t->getSelectedLanguages(codebook, t).size()>0)){
                return true;
            }
        }
        return false;
    }

    /**
     * returns whether one or more items are selected
     * this information is used to determine whether a user can proceed to the next page in the wizard
     * @return true/false
     */
    public boolean oneOrMoreSelectedItems(){
        // get a list of the keys
        List<String> keys = getKeys();
        // filter on whether the key exists in the Map; if it does, check whether any item is selected
        return keys.stream().filter(t->selectedItemsContainerForCodebookDatasetLanguageMap.containsKey(t)).anyMatch(t->selectedItemsContainerForCodebookDatasetLanguageMap.get(t).getSelectedItemsList().size()>0);
    }

    /**
     * add a selected item to a codebook+datasetId+language
     * @param key codebook+datasetId+language
     * @param idemId id of the item which is now selected
     */
    public void addSelectedItem(String key, String idemId){
        if(!selectedItemsContainerForCodebookDatasetLanguageMap.containsKey(key)){
            // add an edc specific item container for the codebook
            addSelectedItemContainerEDC(key);
        }
        // add the item to the edc specific item container for the codebook
        selectedItemsContainerForCodebookDatasetLanguageMap.get(key).addItem(idemId);
    }

    /**
     * remove a now deselected item from a codebook+datasetId+language
     * @param key codebook+datasetId+language
     * @param itemId id of the item which is now deselected
     */
    public void removeSelectedItem(String key, String itemId){
        selectedItemsContainerForCodebookDatasetLanguageMap.get(key).removeItem(itemId);
    }

    /**
     * returns whether the item is selected by the user
     * @param key reference to the codebook
     * @param itemId the item identifier
     * @return true/false
     */
    public boolean itemIsSelected(String key, String itemId){
        if(!selectedItemsContainerForCodebookDatasetLanguageMap.containsKey(key)){
            return false;
        }
        return selectedItemsContainerForCodebookDatasetLanguageMap.get(key).isItemSelected(itemId);
    }

    /**
     * add the selected codelist codeto the item in a codebook+datasetId+language
     * @param key codebook+datasetId+language
     * @param itemId id of item
     * @param code code to add
     */
    public void addSelectedItemTerminology(String key, String itemId, String code){
        selectedItemsContainerForCodebookDatasetLanguageMap.get(key).addTerminology(itemId, code);
    }

    /**
     * remote the code from the selected codelist for the item in a codebook+datasetId+language
     * @param key codebook+datasetId+language
     * @param itemId id of item
     * @param code code to remove
     */
    public void removeSelectedItemTerminology(String key, String itemId, String code){
        selectedItemsContainerForCodebookDatasetLanguageMap.get(key).removeTerminology(itemId, code);
    }

    public void removeSelectedItemTerminologies(String key, String itemId){
        selectedItemsContainerForCodebookDatasetLanguageMap.get(key).removeTerminologies(itemId);
    }

    /**
     * returns the list of the terminology codes selected for an item
     * @param key codebook+datasetId+language
     * @param itemId id of item
     * @return list of selected terminology codes
     */
    public List<String> getSelectedItemSelectedTerminologyCodes(String key, String itemId){
        return selectedItemsContainerForCodebookDatasetLanguageMap.get(key).getTerminologyCodes(itemId);
    }

    /**
     * get all the selected itemIds for a codebook+datasetId+language
     * @param key codebook+datasetId+language
     * @return list with the selected item ids
     */
    public List<String> getSelectedItemIdentifiers(String key){
        if(selectedItemsContainerForCodebookDatasetLanguageMap.containsKey(key)) {
            return selectedItemsContainerForCodebookDatasetLanguageMap.get(key).getSelectedItemsList();
        }
        return new ArrayList<>();
    }

    /**
     * returns the item name of the selected item
     * @param key codebook+datasetId+language
     * @param itemId id of item
     * @return name of the selected item
     */
//    public String getSelectedItemItemName(String key, String itemId){
//        return selectedItemsContainerForCodebookDatasetLanguageMap.get(key).getSelectedItemItemName(itemId);
//    }

    /**
     * returns a summary of the (number of) selected codebooks and items, which we use on the last page of the wizard
     * @return a summary string
     */
    public String getSummary(){
        int nrItemsSelected=0;
        int nrCodebookdatasetIdLanguages=0;
        String summary= I18N.getLanguageText("summaryPart1")+" ";

        for(String codebookName:selectedCodebooks){
            if(selectedCodebooksInfoMap.containsKey(codebookName)) {
                SelectedCodebooksInfo selectedCodebooksInfo = selectedCodebooksInfoMap.get(codebookName);
                List<String> selectedDatasetIds = selectedCodebooksInfo.getSelectedDatasetIds();
                for (String selectedDatasetId : selectedDatasetIds) {
                    List<String> selectedLanguages = selectedCodebooksInfo.getSelectedLanguages(selectedDatasetId);
                    for (String language : selectedLanguages) {
                        String key = codebookName + selectedDatasetId + language;
                        nrCodebookdatasetIdLanguages++;
                        if (selectedItemsContainerForCodebookDatasetLanguageMap.containsKey(key)) {
                            SelectedItemsContainer selectedItemsContainerForCodebookdatasetIdLanguage = selectedItemsContainerForCodebookDatasetLanguageMap.get(key);
                            List<String> selectedItemIds = selectedItemsContainerForCodebookdatasetIdLanguage.getSelectedItemsList();
                            nrItemsSelected += selectedItemIds.size();
                        }
                    }
                }
            }
        }

        summary+=nrItemsSelected+" "+I18N.getLanguageText("summaryPart2")+" "+nrCodebookdatasetIdLanguages+" "+I18N.getLanguageText("summaryPart3");
        return summary;
    }

    /**
     * EDCs are expected to implement this method, which adds an EDC specific container
     * @param key codebook+datasetId+language
     */
    protected abstract void addSelectedItemContainerEDC(String key);

}
