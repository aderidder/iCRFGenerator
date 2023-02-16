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

import icrfgenerator.codebook.CodebookItem;
import icrfgenerator.codebook.CodebookManager;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.settings.LanguageHelper;
import icrfgenerator.types.OperatorType;
import icrfgenerator.edc.edc.EDC;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Runsettings keeps track of everything the user selects
 * It is an abstract class and all EDCs have their own RunSettings, which extends this one, to add their own specifics
 * as well as implement the defined abstract methods.
 * The keys are the identifiers used to access the appropriate codebook in the codebook manager
 * at this point a key is: codebookName + datasetId + language
 * I've had to change this key definition in the past, due to the deviating ZIB structure, which ruined the
 * previous key.
 */
public abstract class RunSettings{
    // there is one runsettings
    private static RunSettings runSettings;

    private final EDC edc;
    private final SelectedCodebooksTracker selectedCodebooksTracker = new SelectedCodebooksTracker();
    protected final Map<String, SelectedItemsContainer> selectedItemsContainerForCodebookDatasetLanguageMap = new HashMap<>();

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
        selectedCodebooksTracker.addCodebook(codebook);
    }

    /**
     * after a codebook has been deselected, remove it from the list
     * @param codebook codebook to remove
     */
    public void removeSelectedCodebook(String codebook){
        selectedCodebooksTracker.removeCodebook(codebook);
    }

    /**
     * returns the selected codebooks, sorted
     * @return the selected sorted codebook list
     */
    public List<String> getSelectedCodebooks(){
        List<String> selectedCodebooks = selectedCodebooksTracker.getSelectedCodebooks();
        Collections.sort(selectedCodebooks);
        return selectedCodebooks;
    }

    /**
     * select a codebook's dataset
     * @param codebook  codebook
     * @param datasetId id of the dataset
     */
    public void addSelectedDataset(String codebook, String datasetId){
        selectedCodebooksTracker.addCodebookDatasetId(codebook, datasetId);
    }

    /**
     * deselect a codebook's dataset
     * @param codebook  codebook
     * @param datasetId id of the dataset
     */
    public void removeSelectedDataset(String codebook, String datasetId){
        selectedCodebooksTracker.removeCodebookDatasetId(codebook, datasetId);
    }

    /**
     * when selecting multiple codebooks, this returns the overlapping languages
     * @return the overlapping languages
     */
    public List<String> getOverlappingSimpleLanguages(){
        return selectedCodebooksTracker.getOverlappingSimpleLanguages();
    }

    /**
     * fetches a (tree)map with the overlapping languages. Key descriptive name, Value simple name
     * @return map descriptive name --> simple name
     */
    public Map<String, String> getOverlappingDescriptiveToSimpleLanguageMap(){
        return selectedCodebooksTracker.getOverlappingDescriptiveToSimpleLanguageMap();
    }

    /**
     * the system has a main language and other languages for which codebooks will also be generated
     * returns the other languages
     * @return the other languages
     */
    public List<String> getOtherSimpleLanguages(){
        return selectedCodebooksTracker.getOtherSimpleLanguages();
    }

    /**
     * sets the other languages to the provided list
     * used when the UI attempts to select previously selected languages, but not all languages are available due
     * to changed codebook selection
     * @param otherLanguages list with languages
     */
    public void setOtherSimpleLanguage(List<String> otherLanguages){
        selectedCodebooksTracker.setOtherSimpleLanguage(otherLanguages);
    }

    /**
     * add a language to the other languages selection
     * @param otherLanguage a language
     */
    public void addOtherSimpleLanguage(String otherLanguage){
        selectedCodebooksTracker.addOtherSimpleLanguage(otherLanguage);
    }

    /**
     * remove a language to the other languages selection
     * @param otherLanguage a language
     */
    public void removeOtherSimpleLanguage(String otherLanguage){
        selectedCodebooksTracker.removeOtherSimpleLanguage(otherLanguage);
    }

    /**
     * returns the main language
     * @return the main language
     */
    public String getMainSimpleLanguage(){
        return selectedCodebooksTracker.getMainSimpleLanguage();
    }

    /**
     * updates the main language to the new main language
     * setting a new main language resets the item selection as well
     * @param mainLanguage the new main language
     */
    public void setMainSimpleLanguage(String mainLanguage){
        selectedCodebooksTracker.setMainSimpleLanguage(mainLanguage);
        // if the main language changes, reset the item selection, since the UI of the new language does not
        // know the selected items in the previous main language
        selectedItemsContainerForCodebookDatasetLanguageMap.clear();
    }

    /**
     * returns a list with all the identifiers for the main language
     * Format: codebookName_datasetId_mainLanguage
     * @return a list with all the identifiers for the main language
     */
    public List<String> getMainSimpleLanguageIds(){
        return selectedCodebooksTracker.getMainSimpleLanguageIds();
    }

//    public List<String> getOtherSimpleLanguageIds(){
//        return selectedCodebooksTracker.getOtherSimpleLanguageIds();
//    }

    /**
     * returns whether any dataset is selected
     * @return true/false
     */
    public boolean anyDatasetsSelected(){
        return selectedCodebooksTracker.anyDatasetSelected();
    }

    /**
     * returns whether any codebook is selected
     * @return true/false
     */
    public boolean anyCodebooksSelected(){
        return selectedCodebooksTracker.anyCodebookSelected();
    }

    /**
     * create the map with the language-based identifiers
     */
    public void determineLanguageBasedIdentifiers(){
        selectedCodebooksTracker.determineLanguageBasedIdentifiers();
    }

    /**
     * the key is something like "Basisgegevenssset Zorg 2017_2.16.840.1.113883.2.4.3.11.60.42.1.1_en"
     * the map maps this to a list with "..._en" , "..._nl", etc
     * these identifiers can be used to ensure the item selection in the codebooks in all the selected languages
     * @param key key
     * @return list with keys for all selected languages for the key
     */
    public List<String> getAllLanguageKeysForMainKey(String key){
        return selectedCodebooksTracker.getAllSimpleLanguageKeysForMainKey(key);
    }

    /**
     * returns all the codebook_datasetId_language keys for a language
     * this way we can create a CRF for a single language, based on all the codebooks/datasets
     * @param simpleLanguage the language
     * @return key list
     */
    public List<String> getAllKeysForLanguage(String simpleLanguage){
        return selectedCodebooksTracker.getAllKeysForSimpleLanguage(simpleLanguage);
    }

    /**
     * list with all selected languages (main language + other languages)
     * @return list with all selected languages
     */
    public List<String> getAllLanguages(){
        return selectedCodebooksTracker.getAllSimpleLanguages();
    }

    /**
     * prints the selected codebook, the selected datasetids and selected languages
     */
    public void printDatasetIdSelectedLanguages(){
//        System.out.println("TODO");
        // TODO: add this?
    }

    /**
     * returns a list of the selected versions for a codebook or an empty list
     * @param codebook codebook for which to find the selected datasetIds
     * @return list with selected datasetIds or empty list
     */
    public List<String> getCodebookSelectedDatasetIds(String codebook){
        return selectedCodebooksTracker.getCodebookDatasetIds(codebook);
    }

    /**
     * returns whether one or more items are selected
     * this information is used to determine whether a user can proceed to the next page in the wizard
     * @return true/false
     */
    public boolean oneOrMoreSelectedItems(){
        // get a list of the keys
        List<String> keys = getMainSimpleLanguageIds();
        // filter on whether the key exists in the Map; if it does, check whether any item is selected
        return keys.stream().filter(t->selectedItemsContainerForCodebookDatasetLanguageMap.containsKey(t)).anyMatch(t->selectedItemsContainerForCodebookDatasetLanguageMap.get(t).getSelectedItemsList().size()>0);
    }

    /**
     * add a selected item to a codebook+datasetId+language
     * @param key codebook+datasetId+language
     * @param idemId id of the item which is now selected
     */
    public void addSelectedItem(String key, String idemId){
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t->{
            if(!selectedItemsContainerForCodebookDatasetLanguageMap.containsKey(t)){
                // add an edc specific item container for the codebook
                addSelectedItemContainerEDC(t);
            }
            // add the item to the edc specific item container for the codebook
            selectedItemsContainerForCodebookDatasetLanguageMap.get(t).addItem(idemId);
        });
    }

    /**
     * remove a now deselected item from a codebook+datasetId+language
     * @param key codebook+datasetId+language
     * @param itemId id of the item which is now deselected
     */
    public void removeSelectedItem(String key, String itemId){
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t->{
            selectedItemsContainerForCodebookDatasetLanguageMap.get(t).removeItem(itemId);
        });
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
     * add the selected codelist code to the item in a codebook+datasetId+language
     * @param key codebook+datasetId+language
     * @param itemId id of item
     * @param code code to add
     */
    public void addSelectedItemTerminology(String key, String itemId, String code){
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t-> {
            selectedItemsContainerForCodebookDatasetLanguageMap.get(t).addTerminology(itemId, code);
        });
    }

    /**
     * add all codelist codes as selected for an item
     * @param key    codebook+datasetId+language
     * @param itemId id of item
     */
    public void addSelectedItemTerminologies(String key, String itemId){
        CodebookItem codebookItem = CodebookManager.getInstance().getCodebookItem(key, itemId);
        List<String> codelistCodes = codebookItem.getCodelistCodes();
        codelistCodes.forEach(t->addSelectedItemTerminology(key, itemId, t));
    }

    /**
     * remove the code from the selected codelist for the item in a codebook+datasetId+language
     * @param key    codebook+datasetId+language
     * @param itemId id of item
     * @param code   code to remove
     */
    public void removeSelectedItemTerminology(String key, String itemId, String code){
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t-> {
            selectedItemsContainerForCodebookDatasetLanguageMap.get(t).removeTerminology(itemId, code);
        });
    }

    /**
     * remove all codes for an item
     * @param key    codebook+datasetId+language
     * @param itemId id of item
     */
    public void removeSelectedItemTerminologies(String key, String itemId){
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t-> {
            selectedItemsContainerForCodebookDatasetLanguageMap.get(t).removeTerminologies(itemId);
        });
    }

    /**
     * returns the list of the terminology codes selected for an item
     * @param key codebook+datasetId+language
     * @param itemId id of item
     * @return list of selected terminology codes
     */
    public List<String> getSelectedItemSelectedTerminologyCodes(String key, String itemId){
        if(selectedItemsContainerForCodebookDatasetLanguageMap.containsKey(key)) {
            return selectedItemsContainerForCodebookDatasetLanguageMap.get(key).getTerminologyCodes(itemId);
        }
        return new ArrayList<>();
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
     * update the data type of an item in a codebook+version+language
     * @param key codebook+version+language
     * @param itemId item to update
     * @param dataType the new data type
     */
    public void updateItemDataType(String key, String itemId, String dataType){
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t-> {
            selectedItemsContainerForCodebookDatasetLanguageMap.get(t).updateItemDataType(itemId, dataType);
        });
    }

    /**
     * get the selected data type for an item in a codebook+version+language
     * @param key codebook+version+language
     * @param itemId id of item
     * @return data type
     */
    public String getSelectedItemDataType(String key, String itemId){
        return selectedItemsContainerForCodebookDatasetLanguageMap.get(key).getSelectedItemDataType(itemId);
    }

    /**
     * update the required value
     * @param key codebook+version+language
     * @param itemId item to update
     * @param required the new required value
     */
    public void updateItemRequiredValue(String key, String itemId, boolean required){
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t-> {
            selectedItemsContainerForCodebookDatasetLanguageMap.get(t).updateItemRequiredValue(itemId, required);
        });
    }

    /**
     * returns the item's required value
     * @param key codebook+version+language
     * @param itemId item to update
     * @return the item's required value
     */
    public boolean getSelectedItemRequiredValue(String key, String itemId){
        return selectedItemsContainerForCodebookDatasetLanguageMap.get(key).getSelectedItemRequiredValue(itemId);
    }

    /**
     * update the min value
     * @param key codebook+version+language
     * @param itemId item to update
     * @param minValue the new min value
     */
    public void updateItemMinValue(String key, String itemId, String minValue){
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t-> {
            selectedItemsContainerForCodebookDatasetLanguageMap.get(t).updateItemMinValue(itemId,minValue);
        });
    }

    /**
     * update the operator for the min value
     * @param key codebook+version+language
     * @param itemId item to update
     * @param minCheckOperator the new operator
     */
    public void updateItemMinCheckFieldValue(String key, String itemId, OperatorType minCheckOperator){
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t-> {
            selectedItemsContainerForCodebookDatasetLanguageMap.get(t).updateItemMinCheckFieldValue(itemId, minCheckOperator);
        });
    }

    /**
     * update the max value
     * @param key codebook+version+language
     * @param itemId item to update
     * @param maxValue the new max value
     */
    public void updateItemMaxValue(String key, String itemId, String maxValue){
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t-> {
            selectedItemsContainerForCodebookDatasetLanguageMap.get(t).updateItemMaxValue(itemId,maxValue);
        });
    }

    /**
     * update the operator for the min value
     * @param key codebook+version+language
     * @param itemId item to update
     * @param maxCheckOperator the new operator
     */
    public void updateItemMaxCheckFieldValue(String key, String itemId, OperatorType maxCheckOperator){
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t-> {
            selectedItemsContainerForCodebookDatasetLanguageMap.get(t).updateItemMaxCheckFieldValue(itemId, maxCheckOperator);
        });
    }

    /**
     * returns the item's min value
     * @param key codebook+version+language
     * @param itemId item to update
     * @return the item's min value
     */
    public String getSelectedItemMinValue(String key, String itemId){
        return selectedItemsContainerForCodebookDatasetLanguageMap.get(key).getSelectedItemMinValue(itemId);
    }

    /**
     * returns the item's min check operator
     * @param key codebook+version+language
     * @param itemId item to update
     * @return the item's min check operator
     */
    public OperatorType getSelectedItemMinCheckOperator(String key, String itemId){
        return selectedItemsContainerForCodebookDatasetLanguageMap.get(key).getItemMinCheckFieldValue(itemId);
    }

    /**
     * returns the item's max check operator
     * @param key codebook+version+language
     * @param itemId item to update
     * @return the item's max check operator
     */
    public OperatorType getSelectedItemMaxCheckOperator(String key, String itemId){
        return selectedItemsContainerForCodebookDatasetLanguageMap.get(key).getItemMaxCheckFieldValue(itemId);
    }

    /**
     * returns the item's max value
     * @param key codebook+version+language
     * @param itemId item to update
     * @return the item's max value
     */
    public String getSelectedItemMaxValue(String key, String itemId){
        return selectedItemsContainerForCodebookDatasetLanguageMap.get(key).getSelectedItemMaxValue(itemId);
    }

    /**
     * update the width value
     * @param key codebook+version+language
     * @param itemId item to update
     * @param value the new units value
     */
    public void updateItemUnitsValue(String key, String itemId, String value){
        List<String> allKeys = RunSettings.getInstance().getAllLanguageKeysForMainKey(key);
        allKeys.forEach(t-> {
            selectedItemsContainerForCodebookDatasetLanguageMap.get(t).updateItemUnitsValue(itemId,value);
        });
    }

    /**
     * returns the item's units value
     * @param key codebook+version+language
     * @param itemId item to update
     * @return the item's units value
     */
    public String getSelectedItemUnitsValue(String key, String itemId){
        return selectedItemsContainerForCodebookDatasetLanguageMap.get(key).getSelectedItemUnitsValue(itemId);
    }

    /**
     * returns a summary of the (number of) selected codebooks and items, which we use on the last page of the wizard
     * @return a summary string
     */
    public String getSummary(){
        int nrItemsSelected=0;
        int nrCodebookdatasetIdLanguages=0;
        String mainLanguage = selectedCodebooksTracker.getMainSimpleLanguage();
        List<String> mainLanguageKeys = selectedCodebooksTracker.getAllKeysForSimpleLanguage(mainLanguage);
        String allLanguages = selectedCodebooksTracker.getAllSimpleLanguages().stream().map(t -> LanguageHelper.getDescriptiveName(t)).collect(Collectors.joining(", "));

        for(String key: mainLanguageKeys){
            if (selectedItemsContainerForCodebookDatasetLanguageMap.containsKey(key)) {
                nrCodebookdatasetIdLanguages++;
                SelectedItemsContainer selectedItemsContainerForCodebookdatasetIdLanguage = selectedItemsContainerForCodebookDatasetLanguageMap.get(key);
                List<String> selectedItemIds = selectedItemsContainerForCodebookdatasetIdLanguage.getSelectedItemsList();
                nrItemsSelected += selectedItemIds.size();
            }
        }

        // summaryPart1 = You've selected
        // summaryPart2 = item(s) from
        // summaryPart3 = codebook(s).
        // summaryPart4 = The CRF(s) will be generated in the following languages:
        // summaryPart5 = Press finish to generate your CRF(s).
        return I18N.getLanguageText("summaryPart1")+" "+
               nrItemsSelected+" "+I18N.getLanguageText("summaryPart2")+" "+
               nrCodebookdatasetIdLanguages+" "+I18N.getLanguageText("summaryPart3")+"\n"+
               I18N.getLanguageText("summaryPart4")+" "+allLanguages+"\n\n"+
               I18N.getLanguageText("summaryPart5");
    }

    /**
     * EDCs are expected to implement this method, which adds an EDC specific container
     * @param key codebook+datasetId+language
     */
    protected abstract void addSelectedItemContainerEDC(String key);

}
