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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * REDCap EDC
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
    public void generateCRF(List<String> keys, String language) {
        REDCapRunSettings runSettings = (REDCapRunSettings) RunSettings.getInstance();
        CodebookManager codebookManager = CodebookManager.getInstance();

        // Add the record id as the first row beneath the header; this is a REDCap requirement
        createFirstRow();

        for (String key : keys) {
            // retrieve the selected items for the key and add each item to the template
            List<String> selectedItemIds = runSettings.getSelectedItemIdentifiers(key);
            for (String itemId : selectedItemIds) {
                // check whether the selected item can have an associated codelist
                if (codebookManager.codebookItemHasCodeList(key, itemId)) {
                    createTerminologyRow(key, itemId);
                }
                else {
                    createNonTerminologyRow(key, itemId);
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
     * this now also includes a detection for :// (as used by e.g. "http://") to remove the link, as redcap
     * dislikes special characters in its code field. Instead, the link is now added to the annotation field
     * @param key codebook + datasetId + language
     * @param itemId id of the item
     * @return a string array with the formatted codelist string and an annotation string
     */
    private String[] formatCodesValues(String key, String itemId){
        String [] formatted = new String [2];
        CodebookManager codebookManager = CodebookManager.getInstance();
        StringBuilder stringBuilder = new StringBuilder();
        // retrieve the selected terminology codes for the item
        List<String> selectedCodeValues = RunSettings.getInstance().getSelectedItemSelectedTerminologyCodes(key, itemId);

        Map<String, String> linkMap = new HashMap<>();

        // loop over the codes
        for(String code:selectedCodeValues){
            String codesystem="";
            String label = codebookManager.getValueForOptionCode(key, itemId, code).replaceAll("\"", "'");

            Pattern pattern = Pattern.compile("(.*/\\W*)(\\w+)");
            Matcher matcher = pattern.matcher(code);
            if(matcher.matches()){
                // fetch the codesystem for the code, split the code into the link part and its normal code.
                codesystem = codebookManager.getCodesystemForOptionCode(key, itemId, code);
                codesystem = codesystem.replaceAll("\\W", "");
                String link = matcher.group(1);
                code = matcher.group(2);
                // store in the linkmap
                linkMap.put(codesystem, codesystem+":"+link);
                codesystem+="_";
            }

            code = code.replaceAll("\\W", "");
            // retrieve the label for a code and create the redcap codelist format
            stringBuilder.append(codesystem).append(code).append(", ").append(label).append(" | ");

        }

        // delete the trailing " | "
        if(stringBuilder.length()>0) {
            stringBuilder.delete(stringBuilder.length() - 3, stringBuilder.length());
        }

        // add the formatted string to the array
        formatted[1] = linkMap.size()>0 ? String.join(" | ", linkMap.values()):"";
        formatted[0] = stringBuilder.toString();
        return formatted;
    }

    /**
     * create a row for an item which has terminology
     */
    private void createTerminologyRow(String key, String itemId){
        String [] data = new String[ItemColumns.values().length];
        addGeneralValues(data, key, itemId);
        addTerminologyValues(data, key, itemId);
        addData(data);
    }

    /**
     * create a row for an item which does not have terminology
     */
    private void createNonTerminologyRow(String key, String itemId){
        String [] data = new String[ItemColumns.values().length];
        addGeneralValues(data, key, itemId);
        addNonTerminologyValues(data, key, itemId);
        addData(data);
    }

    /**
     * values which exist in both the terminology and nonTerminology rows
     * @param data   array to which to add the data
     * @param key    key
     * @param itemId itemId
     */
    private void addGeneralValues(String [] data, String key, String itemId){
        REDCapRunSettings runSettings = (REDCapRunSettings) RunSettings.getInstance();
        String itemName = CodebookManager.getInstance().getItemName(key, itemId);

        data[ItemColumns.FIELD_NAME.index] = StringUtils.addQuotationMarks(StringUtils.removeSpacesFromString(itemName)).toLowerCase();
        data[ItemColumns.FORM_NAME.index] = formName;
        data[ItemColumns.FIELD_TYPE.index] = runSettings.getSelectedItemFieldType(key, itemId);
        data[ItemColumns.FIELD_LABEL.index] = StringUtils.addQuotationMarks(itemName);
        data[ItemColumns.FIELD_NOTES.index] =  runSettings.getSelectedItemUnitsValue(key, itemId);
        data[ItemColumns.REQUIRED_FIELD.index] = runSettings.getSelectedItemRequiredValue(key, itemId)?"y":"";
    }

    /**
     * add values that are only relevant for terminology rows
     * @param data   array to which to add the data
     * @param key    key
     * @param itemId itemId
     */
    private void addNonTerminologyValues(String [] data, String key, String itemId){
        REDCapRunSettings runSettings = (REDCapRunSettings) RunSettings.getInstance();
        data[ItemColumns.TEXT_VALIDATION_TYPE_OR_SHOW_SLIDER_NUMBER.index] = runSettings.getSelectedItemTextValidationType(key, itemId);
        data[ItemColumns.TEXT_VALIDATION_MIN.index] = runSettings.getSelectedItemMinValue(key, itemId);
        data[ItemColumns.TEXT_VALIDATION_MAX.index] = runSettings.getSelectedItemMaxValue(key, itemId);
    }

    /**
     * add values that are only relevant for nonTerminology rows
     * @param data   array to which to add the data
     * @param key    key
     * @param itemId itemId
     */
    private void addTerminologyValues(String [] data, String key, String itemId){
        String [] formatValues = formatCodesValues(key, itemId);
        data[ItemColumns.CHOICES_CALCULATIONS_OR_SLIDER_LABELS.index] = StringUtils.addQuotationMarks(formatValues[0]);
        data[ItemColumns.FIELD_ANNOTATION.index] = StringUtils.addQuotationMarks(formatValues[1]);
    }

    /**
     * create the first row which has the record id
     */
    private void createFirstRow(){
        String[] data = new String[ItemColumns.values().length];
        data[ItemColumns.FIELD_NAME.index] = "record_id";
        data[ItemColumns.FORM_NAME.index] = formName;
        data[ItemColumns.FIELD_TYPE.index] = "text";
        data[ItemColumns.FIELD_LABEL.index] = "Record ID";
        addData(data);
    }

    /**
     * add the data to the lines list, which is later written to a file
     * @param data the data
     */
    private void addData(String [] data){
        lines.add(String.join(",", data).replaceAll("null", ""));
    }


    private enum ItemColumns{
        FIELD_NAME(0),
        FORM_NAME(1),
        SECTION_HEADER(2),
        FIELD_TYPE(3),
        FIELD_LABEL(4),
        CHOICES_CALCULATIONS_OR_SLIDER_LABELS(5),
        FIELD_NOTES(6),
        TEXT_VALIDATION_TYPE_OR_SHOW_SLIDER_NUMBER(7),
        TEXT_VALIDATION_MIN(8),
        TEXT_VALIDATION_MAX(9),
        IDENTIFIER(10),
        BRANCHING_LOGIC(11),
        REQUIRED_FIELD(12),
        CUSTOM_ALIGNMENT(13),
        QUESTION_NUMBE(14),
        MATRIX_GROUP_NAME(15),
        MATRIX_RANKING(16),
        FIELD_ANNOTATION(17);

        private final int index;

        ItemColumns(int index){
            this.index = index;
        }
    }

}
