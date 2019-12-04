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

package icrfgenerator.edc.edc;

import icrfgenerator.codebook.CodebookItem;
import icrfgenerator.codebook.CodebookManager;
import icrfgenerator.edc.edc.edcspecificpane.EDCSpecificPane;
import icrfgenerator.edc.edc.edcspecificpane.REDCapSpecificPane;
import icrfgenerator.resourcemanagement.ResourceManager;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.edc.edc.edcrunsettings.redcap.REDCapRunSettings;
import icrfgenerator.utils.StringUtils;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REDCap EDC
 * Kind of untested at the moment as I don't have a REDCap server
 *
 * The columns of a redcap csv are:
 * Variable / Field Name *
 * Form Name
 * Section Header
 * Field Type *
 * Field Label *
 * "Choices, Calculations, OR Slider Labels" **
 * Field Note
 * Text Validation Type OR Show Slider Number
 * Text Validation Min
 * Text Validation Max
 * Identifier?
 * Branching Logic (Show field only if...)
 * Required Field?
 * Custom Alignment
 * Question Number (surveys only)
 * Matrix Group Name
 */
public class REDCapEDC extends EDCDefault{
    private List<String> lines = new ArrayList<>();
    private static final String formName = "generatedcrf"; //must be lowercase for REDCap

    public REDCapEDC(){
        super("REDCap");
    }


    /**
     * generates a new instance of this EDC's rightside pane and returns it
     * @return a new instance of this EDC's rightside pane
     */
    @Override
    public EDCSpecificPane generateRightSidePane(){
        return new REDCapSpecificPane();
    }

    /**
     * EDC specific setup
     */
    @Override
    public void setupEDC(){
        readTemplate();
    }

    /**
     * get a csv filechooser filter
     * @return a filechooser filter for csv files
     */
    @Override
    public FileChooser.ExtensionFilter getExtensionFilter() {
        return new FileChooser.ExtensionFilter("CSV files: (*.csv)", "*.csv");
    }

    /**
     * generate the CRF based on what the user selected
     */
    @Override
    public void generateCRF() {
        REDCapRunSettings runSettings = (REDCapRunSettings) RunSettings.getInstance();
        CodebookManager codebookManager = CodebookManager.getInstance();

        // Add the record id as the first row beneath the header; this is a REDCap requirement
        createNonTerminologyRow("Record ID", "text", "", "", "");

        List<String> keys = runSettings.getKeys();
        for (String key : keys) {
            // retrieve the selected items for the key and add each item to the template
            List<String> selectedItemIds = runSettings.getSelectedItemIdentifiers(key);
            for (String itemId : selectedItemIds) {
                CodebookItem codebookItem = codebookManager.getCodebookItem(key, itemId);

                String fieldType = runSettings.getSelectedItemFieldType(key, itemId);
                String textValidationType = runSettings.getSelectedItemTextValidationType(key, itemId);
                String minValue = runSettings.getSelectedItemMinValue(key, itemId);
                String maxValue = runSettings.getSelectedItemMaxValue(key, itemId);
                String itemName = codebookItem.getItemName();
                // check whether the selected item can have an associated codelist
                if (codebookManager.codebookItemHasCodeList(key, itemId)) {
                    createTerminologyRow(itemName, fieldType, formatCodesValues(key, itemId));
                }
                else {
                    createNonTerminologyRow(itemName, fieldType, textValidationType, minValue, maxValue);
                }
            }
        }
    }

    /**
     * write the filled template to a file
     * @param file file to write to
     */
    @Override
    public void writeFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
            lines.forEach(t-> {
                try {
                    writer.write(t);
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the REDCap template from the resources folder
     */
    private void readTemplate(){
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceManager.getResourceTemplateInputStream("REDCapTemplate.csv")))){
            lines = reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * create a redcap string representation of the codes + labels for an item
     * @param key codebook + datasetId + language
     * @param itemId id of the item
     * @return a string for the codelist
     */
    private String formatCodesValues(String key, String itemId){
        CodebookManager codebookManager = CodebookManager.getInstance();
        StringBuilder stringBuilder = new StringBuilder();
        // retrieve the selected terminology codes for the item
        List<String> selectedCodeValues = RunSettings.getInstance().getSelectedItemSelectedTerminologyCodes(key, itemId);

        // loop over the codes
        for(String code:selectedCodeValues){
            // retrieve the label for a code and create the redcap codelist format
            stringBuilder.append(code).append(", ").append(codebookManager.getValueForCode(key, itemId, code)).append(" | ");
        }

        // delete the trailing " | "
        if(stringBuilder.length()>0) {
            stringBuilder.delete(stringBuilder.length() - 3, stringBuilder.length());
        }
        return stringBuilder.toString();
    }

    /**
     * creata a row for an item which has terminology
     * @param itemName name of the item
     * @param fieldType fieldtype of the item
     * @param codesValuesString codes & values string of the codelist
     */
    private void createTerminologyRow(String itemName, String fieldType, String codesValuesString){
        lines.add(StringUtils.addQuotationMarks(StringUtils.removeSpacesFromString(itemName)).toLowerCase() +","+formName+",,"+fieldType+","+ StringUtils.addQuotationMarks(itemName)+","+ StringUtils.addQuotationMarks(codesValuesString)+",,,,,,,,,,");
    }

    /**
     * creata a row for an item which does not have terminology
     * @param itemName name of the item
     * @param fieldType fieldtype of the item
     */
    private void createNonTerminologyRow(String itemName, String fieldType, String textValidationType, String minValue, String maxValue){
        lines.add(StringUtils.addQuotationMarks(StringUtils.removeSpacesFromString(itemName)).toLowerCase()+","+formName+",,"+fieldType+","+ StringUtils.addQuotationMarks(itemName)+",,,"+textValidationType+","+minValue+","+maxValue+",,,,,,");
    }
}
