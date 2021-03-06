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
import icrfgenerator.edc.edc.edcdefinitions.CastorDefinition;
import icrfgenerator.edc.edc.edcspecificpane.CastorSpecificPane;
import icrfgenerator.edc.edc.edcspecificpane.EDCSpecificPane;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.edc.edc.edcrunsettings.castor.CastorRunSettings;
import icrfgenerator.utils.StringUtils;
import icrfgenerator.utils.XMLUtils;
import javafx.stage.FileChooser;


import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static icrfgenerator.edc.edc.CastorEDC.*;

/**
 * Castor EDC
 * Some useful information about export and importing of Castor forms can be found here:
 * https://helpdesk.castoredc.com/article/170-export-and-import-the-form-structure
 */
abstract public class CastorEDC extends EDCDefault {
    /**
     * generates an UUID as used by Castor
     *
     * @return a new random UUID
     */
    static String generateUUID() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    private static Map<String, Integer> codesystemMap = new HashMap<>();
    private static int codesystemCounter=1;

    static void addCodesystem(String codesystem){
        if(!codesystemMap.containsKey(codesystem)){
            codesystemMap.put(codesystem, codesystemCounter++);
        }
    }
    static String getCodesystemNumber(String codesystem){
        return String.valueOf(codesystemMap.get(codesystem));
    }

    private int fieldNumber = 1;
    private String dateCreated;
    private static final String updatedBy = "F578677A-5CA4-9222-1C4D-C295CF4C4135"; // My id in Castor

    private StringBuilder fullXML;

    protected CastorEDC() {
        super("Castor");
    }

    /**
     * generates a new instance of this EDC's rightside pane and returns it
     *
     * @return a new instance of this EDC's rightside pane
     */
    @Override
    public EDCSpecificPane generateRightSidePane() {
        return new CastorSpecificPane();
    }

    /**
     * EDC specific setup
     */
    @Override
    public void setupEDC() {

    }

    /**
     * get an xml filechooser filter
     *
     * @return a filechooser filter for xml files
     */
    @Override
    public FileChooser.ExtensionFilter getExtensionFilter() {
        return new FileChooser.ExtensionFilter("XML files: (*.xml)", "*.xml");
    }

    /**
     * generate the CRF based on what the user selected
     */
    @Override
    public void generateCRF() {
        setDateCreated();
        codesystemMap.clear();
        codesystemCounter=1;
        fieldNumber = 1;

        // create a couple of string buffers
        StringBuilder fieldsBuilder = new StringBuilder();
        StringBuilder optionListsBuilder = new StringBuilder();

        // get the runsettings and codebookmanager
        RunSettings runSettings = RunSettings.getInstance();
        CodebookManager codebookManager = CodebookManager.getInstance();

        // generate a unique id for the Castor Step
        String stepId = generateUUID();

        // retrieve the keys and loop over them
        List<String> keys = runSettings.getKeys();
        for (String key : keys) {
            // retrieve the selected items for the key and add each item to the template
            List<String> selectedItemIds = runSettings.getSelectedItemIdentifiers(key);
            for (String itemId : selectedItemIds) {
                // check whether the selected item can have an associated codelist
                if (codebookManager.codebookItemHasCodeList(key, itemId)) {
                    createTerminologyItem(fieldsBuilder, optionListsBuilder, key, itemId, stepId);
                } else {
                    createNonTerminologyItem(fieldsBuilder, key, itemId, stepId);
                }
            }
        }
        buildXML(fieldsBuilder, optionListsBuilder, stepId);
    }

    /**
     * write the filled template to a file
     *
     * @param file file to write to
     */
    @Override
    public void writeFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(XMLUtils.formatXMLString(fullXML.toString(), 2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Store date + time
     */
    private void setDateCreated() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        dateCreated = dtf.format(now); //e.g. 2016-11-16 12:08:43
    }

    /**
     * build the complete XML for Castor import
     *
     * @param fieldsBuilder      buffer with the complete fields XML part
     * @param optionListsBuilder buffer with the complete optionlist XML part
     * @param stepId             id of the step
     */
    private void buildXML(StringBuilder fieldsBuilder, StringBuilder optionListsBuilder, String stepId) {
        fullXML = new StringBuilder();
        String phaseId = generateUUID();
        // Not a clue why, but version has to be 3 for reports, whereas 1 is accepted for both the normal
        // crf and the survey. However, as every form from castor forms has version 3, we'll set it to
        // 3 to be on the safe side...
        int version = 3;


        fullXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").
                append("<xml>").
                append("<crf_id>").append(generateUUID()).append("</crf_id>").
                append("<optionLists>");
        fullXML.append(optionListsBuilder.toString());
        fullXML.append("</optionLists>").
                append("<version>").append(version).append("</version>").
                append("<metadata>1</metadata>").
                append("<date_created>").append(dateCreated).append("</date_created>").
                append("<created_by>The Registry in a Box iCRF Generator</created_by>").
                append("<lookupTable/>");
        buildXMLForFormPart1(fullXML, stepId, phaseId);
        fullXML.append("<fields>");
        fullXML.append(fieldsBuilder.toString());
        fullXML.append("</fields>");
        buildXMLForFormPart2(fullXML, stepId, phaseId);
        if(codesystemMap.size()>0) fullXML.append(getMetadataTypes());
        fullXML.append("</xml>");
    }

    private String getMetadataTypes(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<metadataTypes>");
        for (Map.Entry<String, Integer> entry : codesystemMap.entrySet()) {
            stringBuilder.
                    append("<mt_"+entry.getValue()+">").
                    append("<type_id>").append(entry.getValue()).append("</type_id>").
                    append("<name>").append(entry.getKey()).append("</name>").
                    append("<description>").append(entry.getKey()).append("</description>").
                    append("</mt_"+entry.getValue()+">");
        }
        stringBuilder.append("</metadataTypes>");
        return stringBuilder.toString();
    }

    /**
     * create an item which has associated terminology
     *
     * @param fieldsBuilder      buffer for the field related part of the XML string
     * @param optionListsBuilder buffer for the options related part of the XML string
     * @param key                codebook + datasetid + language
     * @param itemId             id of the item
     * @param stepId             id of the step
     */
    private void createTerminologyItem(StringBuilder fieldsBuilder, StringBuilder optionListsBuilder, String key, String itemId, String stepId) {
        String fieldId = generateUUID();

        // create an options group for castor
        CastorOptionsHelper castorOptionsHelper = new CastorOptionsHelper(key, itemId, fieldId);
        // get the codes for the item
        List<String> selectedCodes = RunSettings.getInstance().getSelectedItemSelectedTerminologyCodes(key, itemId);
        // add each code as an option tot the options group
        for (String code : selectedCodes) {
            castorOptionsHelper.addOption(code);
        }

        // get a string representation of the options group and add it to the optionList buffer, which we need later on when
        // we generate the XML
        optionListsBuilder.append(castorOptionsHelper.getOptionGroupString());

        // add a field definition
        addFieldXML(fieldsBuilder, key, itemId, stepId, fieldId, castorOptionsHelper.getOptionGroupFieldString(), castorOptionsHelper.getOptionGroupMetadataString(), castorOptionsHelper.getOptionGroupId());

    }

    /**
     * create an item which has no associated terminology
     *
     * @param fieldsBuilder buffer for the field related part of the XML string
     * @param key           codebook + datasetid + language
     * @param itemId        id of the item
     * @param stepId        id of the step
     */
    private void createNonTerminologyItem(StringBuilder fieldsBuilder, String key, String itemId, String stepId) {
        String fieldId = generateUUID();
        // add a field definition
        addFieldXML(fieldsBuilder, key, itemId, stepId, fieldId, "", "", "");
    }

    /**
     * generate the Field XML part for one field
     *
     * @param fieldsBuilder     buffer for the field related part of the XML string
     * @param key               codebook + datasetid + language
     * @param itemId            id of the item
     * @param stepId            id of the step
     * @param optionGroupString string for the options group as it should appear in the Field part
     * @param optionGroupId     id of the options group
     */
    private void addFieldXML(StringBuilder fieldsBuilder, String key, String itemId, String stepId, String fieldId, String optionGroupString, String metadataString, String optionGroupId) {
        // since we need all the details, grab the CastorRunSettings
        CastorRunSettings runSettings = (CastorRunSettings) RunSettings.getInstance();
        CodebookManager codebookManager = CodebookManager.getInstance();
        CodebookItem codebookItem = codebookManager.getCodebookItem(key, itemId);

        // retrieve / generate field details for Castor
        String fieldType = runSettings.getSelectedItemFieldType(key, itemId);
        String fieldDescription = CastorDefinition.getFieldTypeDescription(fieldType);
        String fieldLabel = codebookItem.getItemName();
        String fieldName = makeUnique(fieldLabel);
        String fieldRequired = runSettings.getSelectedItemRequiredValue(key, itemId) ? "1" : "0";
        String fieldRequiredDescr = fieldRequired.equalsIgnoreCase("1") ? "Required" : "Not required";
        String fieldMin = runSettings.getSelectedItemMinValue(key, itemId);
        String fieldMax = runSettings.getSelectedItemMaxValue(key, itemId);
        String fieldWidth = runSettings.getSelectedItemWidthValue(key, itemId);

        fieldsBuilder.
                append("<fi_").append(fieldId).append(">").
                append("<parent_id>").append(stepId).append("</parent_id>").
                append("<field_id>").append(fieldId).append("</field_id>").
                append("<field_number>").append(fieldNumber++).append("</field_number>").
                append("<field_number_display>1</field_number_display>").
                append("<field_label>").append(fieldLabel).append("</field_label>").
                append("<field_is_alias>0</field_is_alias>").
                append("<field_variable_name>").append(fieldName).append("</field_variable_name>").
                append("<field_type>").append(fieldType).append("</field_type>").
                append("<field_type_description>").append(fieldDescription).append("</field_type_description>").
                append("<field_option_group>").append(optionGroupId).append("</field_option_group>").
                append("<field_custom_type></field_custom_type>").
                append("<field_required>").append(fieldRequired).append("</field_required>").
                append("<field_required_description>").append(fieldRequiredDescr).append("</field_required_description>").
                append("<field_min>").append(fieldMin).append("</field_min>").
                append("<field_max>").append(fieldMax).append("</field_max>").
                append("<field_min_label></field_min_label>").
                append("<field_max_label></field_max_label>").
                append("<field_length>").append(fieldWidth).append("</field_length>").
                append("<field_inclusion></field_inclusion>").
                append("<field_units></field_units>").
                append("<field_info></field_info>").
                append("<field_summary_template></field_summary_template>").
                append("<updated_by>").append(updatedBy).append("</updated_by>").
                append("<updated_on>").append(dateCreated).append("</updated_on>").
                append("<field_visibility></field_visibility>").
                append("<field_value></field_value>").
                append("<option_groups></option_groups>"). // ??
                append("<show_validation></show_validation>").
                append("<show_validation_type></show_validation_type>").
                append("<field_image></field_image>").
                append("<field_slider_step></field_slider_step>").
                append("<exclude_on_data_export>0</exclude_on_data_export>").
                append("<field_hidden>0</field_hidden>");
//                append("<encryption_enabled>false</encryption_enabled>");

                buildXMLForFormPart3(fieldsBuilder, fieldLabel);

                fieldsBuilder.append("<report_id></report_id>").
                append("<additional_config></additional_config>").
                append("<option_group>").
                append(optionGroupString).
                append("</option_group>").
                append("<dependencyChilds/>").
                append("<dependencyParents/>").
                append("<validation/>").
//                append("<field_label_parsed>").append(fieldLabel).append("</field_label_parsed>").
//                append(metadataString).
        append(addFieldMetaData(codebookItem, metadataString, fieldId)).
                append("</fi_").append(fieldId).append(">");
    }

    private String addFieldMetaData(CodebookItem codebookItem, String optionsMetadataString, String fieldId){
        String codeSystem = codebookItem.getCodeSystem();
        String metadata="";

        if(codeSystem.equalsIgnoreCase("") && optionsMetadataString.equalsIgnoreCase("")){
            return "";
        }
        else if(!codeSystem.equalsIgnoreCase("")) {
            String itemCode = codebookItem.getCode();
            addCodesystem(codeSystem);
            String metadataId = generateUUID();
            metadata = "<m_" + metadataId + ">" +
                    "<metadata_id>" + metadataId + "</metadata_id>" +
                    "<metadata_parent_id></metadata_parent_id>" +
                    "<element_id>" + fieldId + "</element_id>" +
                    "<element_type>1</element_type>" +
                    "<metadata_type>" + StringUtils.prepareValueForXML(getCodesystemNumber(codeSystem)) + "</metadata_type>" +
                    "<metadata_description>" + StringUtils.prepareValueForXML(codebookItem.getItemName()) + "</metadata_description>" +
                    "<metadata_value>" + itemCode + "</metadata_value>" +
                    "</m_" + metadataId + ">";
        }

        return "<metadata>"+optionsMetadataString+metadata+"</metadata>";

    }

    protected abstract void buildXMLForFormPart1(StringBuilder stringBuilder, String stepId, String phaseId);
    protected abstract void buildXMLForFormPart2(StringBuilder stringBuilder, String stepId, String phaseId);
    protected abstract void buildXMLForFormPart3(StringBuilder stringBuilder, String fieldLabel);

}

/**
 * Castor options group
 */
class CastorOptionsHelper {
    private String key;
    private String itemId;
    private String optionGroupId;
    private String fieldId;
    private List<Option> options = new ArrayList<>();

    /**
     * constructor
     * @param key codebook + datasetid + language
     * @param itemId id of the item
     */
    CastorOptionsHelper(String key, String itemId, String fieldId) {
        this.optionGroupId = generateUUID();
        this.fieldId = fieldId;
        this.key = key;
        this.itemId = itemId;
    }

    /**
     * returns the groupid
     * @return the groupid
     */
    String getOptionGroupId(){
        return optionGroupId;
    }

    /**
     * add a code as an Option
     * @param code a code
     */
    void addOption(String code){
        options.add(new Option(code, options.size()));
    }

    /**
     * convert this group into a string necessary for the options group part in the XML (the og tag)
     * @return string representation of the options group
     */
    String getOptionGroupString(){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("<og_").append(optionGroupId).append(">");
        stringBuilder.append(getOptionGroupTags());
        stringBuilder.append("<options>");
        for(Option option:options){
            stringBuilder.append(option.getOptionString());
        }
        stringBuilder.append("</options>");
        stringBuilder.append("</og_").append(optionGroupId).append(">");
        return stringBuilder.toString();
    }

    /**
     * converts this group into a string necessary for the fields part in the XML
     * @return string representation of the options group
     */
    String getOptionGroupFieldString(){
        StringBuilder stringBuilder = new StringBuilder();

        // add the general part for an options group
        stringBuilder.append(getOptionGroupTags());

        // add each option
        for(Option option:options){
            stringBuilder.append("<options>");
            stringBuilder.append(option.getOptionFieldString());
            stringBuilder.append("</options>");
        }
        return stringBuilder.toString();
    }

    String getOptionGroupMetadataString(){
        StringBuilder stringBuilder = new StringBuilder();

//        stringBuilder.append("<metadata>");
        // add each option
        for(Option option:options){
            stringBuilder.append(option.getOptionMetadataString(fieldId));
        }
//        stringBuilder.append("</metadata>");

        return stringBuilder.toString();
    }

    /**
     * returns the name with " Options" as suffix, which is used as the name of a group
     * @return the name for the options group
     */
    private String getGroupName(){
        return CodebookManager.getInstance().getItemName(key, itemId)+" Options";
    }

    /**
     * returns tags for the options group
     * @return tags for the options group
     */
    private String getOptionGroupTags(){
        String group_layout = "0";
        return "<option_group_id>" + optionGroupId + "</option_group_id>" +
               "<option_group_name>" + getGroupName() + "</option_group_name>" +
               "<option_group_description></option_group_description>" +
               "<option_group_layout>" + group_layout + "</option_group_layout>";
    }

    /**
     * a single option
     */
    class Option{
        String value;
        String codesystem;
        String code;
        String optionId;
        int order;

        /**
         * constructor
         * @param code the code of this option
         * @param order the order of this option in the list
         */
        Option(String code, int order){
            this.code = code;
            this.order = order;
            this.value = CodebookManager.getInstance().getValueForCode(key, itemId, code);
            this.codesystem = CodebookManager.getInstance().getCodesystemForCode(key, itemId, code);
            optionId = generateUUID();
            addCodesystem(codesystem);
        }

        /**
         * convert the option to a string used by the group part in the XML (the og tag)
         * @return string representation
         */
        String getOptionString(){
            return  "<op_"+optionId+">" +
                    getOptionFieldString()+
                    "</op_"+optionId+">";
        }

        /**
         * convert the option to a string used by the field part in the XML
         * @return string representation
         */
        String getOptionFieldString(){
            return  "<option_id>"+optionId+"</option_id>" +
                    "<option_group_id>"+optionGroupId+"</option_group_id>" +
                    "<option_name>"+StringUtils.prepareValueForXML(value)+"</option_name>" +
//                    "<option_value>"+code+"</option_value>" +
                    "<option_value>"+StringUtils.prepareValueForXML(value)+"</option_value>" +
                    "<option_group_order>"+order+"</option_group_order>";
        }

        String getOptionMetadataString(String fieldId){
            String metadataId = generateUUID();
            return "<m_"+metadataId+">" +
                   "<metadata_id>"+metadataId+"</metadata_id>" +
                   "<metadata_parent_id></metadata_parent_id>" +
                   "<element_id>"+fieldId+"</element_id>" +
                   "<element_type>1</element_type>" +
                   "<metadata_type>"+StringUtils.prepareValueForXML(getCodesystemNumber(codesystem))+"</metadata_type>" +
                   "<metadata_description>"+StringUtils.prepareValueForXML(value)+"</metadata_description>" +
                   "<metadata_value>"+code+"</metadata_value>" +
                   "</m_"+metadataId+">";

        }
    }
}


