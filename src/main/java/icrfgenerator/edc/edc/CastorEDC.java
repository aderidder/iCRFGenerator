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
import icrfgenerator.types.OperatorType;
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
 * <a href="https://helpdesk.castoredc.com/article/170-export-and-import-the-form-structure">...</a>
 */
abstract public class CastorEDC extends EDCDefault {
    /**
     * generates a UUID as used by Castor
     * @return a new random UUID
     */
    static String generateUUID() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    private static final String updatedBy = "F578677A-5CA4-9222-1C4D-C295CF4C4135"; // My id in Castor
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


    private String curLanguage;
    private int fieldNumber = 1;
    private String dateCreated;
    private StringBuilder fullXML;

    protected CastorEDC() {
        super("Castor");
    }

    /**
     * generates a new instance of this EDC's rightside pane and returns it
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
    public void generateCRF(List<String> keys, String language) {
        setDateCreated();
        codesystemMap.clear();
        codesystemCounter=1;
        fieldNumber = 1;
        curLanguage = language;

        // create a couple of string buffers
        StringBuilder fieldsBuilder = new StringBuilder();
        StringBuilder optionListsBuilder = new StringBuilder();

        // get the runsettings and codebookmanager
        RunSettings runSettings = RunSettings.getInstance();
        CodebookManager codebookManager = CodebookManager.getInstance();

        // generate a unique id for the Castor Step
        String stepId = generateUUID();

        // retrieve the keys and loop over them
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

    /**
     * returns metadataTypes xml section
     * @return metadataTypes xml section
     */
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
     * @param fieldsBuilder     buffer for the field related part of the XML string
     * @param key               codebook + datasetid + language
     * @param itemId            id of the item
     * @param stepId            id of the step
     * @param fieldId           uuid for the field
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
        String fieldName = makeUnique(StringUtils.removeSpacesFromString(fieldLabel));
        String fieldRequired = runSettings.getSelectedItemRequiredValue(key, itemId) ? "1" : "0";
        String fieldRequiredDescr = fieldRequired.equalsIgnoreCase("1") ? "Required" : "Not required";
        String fieldUnits = runSettings.getSelectedItemUnitsValue(key, itemId);
        String fieldEnforceDecimals = runSettings.getSelectedItemEnforceDecimalsValue(key, itemId) ? "1" : "0";

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
                append("<field_type_description>").append(StringUtils.prepareValueForXML(fieldDescription)).append("</field_type_description>").
                append("<field_option_group>").append(optionGroupId).append("</field_option_group>").
                append("<field_custom_type></field_custom_type>").
                append("<field_required>").append(fieldRequired).append("</field_required>").
                append("<field_required_description>").append(fieldRequiredDescr).append("</field_required_description>").
                append("<field_min></field_min>").
                append("<field_max></field_max>").
                append("<field_min_label></field_min_label>").
                append("<field_max_label></field_max_label>").
                append("<field_enforce_decimals>").append(fieldEnforceDecimals).append("</field_enforce_decimals>").
                append("<field_length></field_length>").
                append("<field_inclusion></field_inclusion>").
                append("<field_units>").append(fieldUnits).append("</field_units>").
                append("<field_info></field_info>").
                append("<field_summary_template></field_summary_template>").
                append("<updated_by>").append(updatedBy).append("</updated_by>").
                append("<updated_on>").append(dateCreated).append("</updated_on>").
                append("<field_visibility></field_visibility>").
                append("<field_value></field_value>").
                append("<option_groups></option_groups>"); // ??
        addValidations(fieldId, key, itemId, fieldsBuilder);
        fieldsBuilder.append("<field_image></field_image>").
                append("<field_slider_step></field_slider_step>").
                append("<exclude_on_data_export>0</exclude_on_data_export>").
                append("<field_hidden>0</field_hidden>");
                buildXMLForFormPart3(fieldsBuilder, fieldLabel);
                fieldsBuilder.append("<report_id></report_id>").
                append("<additional_config></additional_config>").
                append("<option_group>").
                append(optionGroupString).
                append("</option_group>").
                append("<dependencyChilds/>").
                append("<dependencyParents/>").
        append(addFieldMetaData(codebookItem, metadataString, fieldId)).
                append("</fi_").append(fieldId).append(">");
    }

    /**
     * generate validation tags for min and max values with the appropriate language-based messages
     * @param fieldId      uuid for the field
     * @param key          codebook + datasetid + language
     * @param itemId       id of the item
     * @param fieldBuilder buffer for the XML string
     */
    private void addValidations(String fieldId, String key, String itemId, StringBuilder fieldBuilder){
        RunSettings runSettings = RunSettings.getInstance();
        String fieldMin = runSettings.getSelectedItemMinValue(key, itemId);
        String fieldMax = runSettings.getSelectedItemMaxValue(key, itemId);

        if(fieldMin.equalsIgnoreCase("") && fieldMax.equalsIgnoreCase("")){
            fieldBuilder.append("<validation/>");
        }
        else{
            int cnt=0;
            OperatorType minCheckOperator = runSettings.getSelectedItemMinCheckOperator(key, itemId);
            OperatorType maxCheckOperator = runSettings.getSelectedItemMaxCheckOperator(key, itemId);
            fieldBuilder.append("<validation>");
            cnt = addValidation(fieldId, fieldMin, minCheckOperator, cnt, fieldBuilder);
            addValidation(fieldId, fieldMax, maxCheckOperator, cnt, fieldBuilder);
            fieldBuilder.append("</validation>");
        }
    }

    /**
     * Part of the code to add the validations XML
     * @param fieldId       uuid of the field
     * @param value         the value to check against
     * @param operatorType  opereratorType which specifies whether the check is e.g. GT, GTE, etc.
     * @param cnt           counter for field validation id
     * @param stringBuilder buffer for the XML string
     * @return updated cnt value
     */
    private int addValidation(String fieldId, String value, OperatorType operatorType, int cnt, StringBuilder stringBuilder){
        if(value.equalsIgnoreCase("") || operatorType.equals(OperatorType.NONE)){
            return cnt;
        }

        // As error messages we basically have in our Excel - Check: v<10 --> Value must be less than 10
        // Castor uses a different approach for the validation:
        // If this field is: > 10
        // Show a: warning
        // Message: Value must be less than 10
        // Therefore, we'll use a flipped version of the operator for the check and the regular one for the message
        OperatorType operatorTypeFlipped = OperatorType.flipOperator(operatorType);
        stringBuilder.append("<validation_"+cnt+">") //form_sync_id="EE875797-CAA4-4303-8D2E-143F8171F367">
                .append("<field_id>"+fieldId+"</field_id>")
                .append("<field_validation_id>"+cnt+"</field_validation_id>")
                .append("<field_validation_operator>"+ StringUtils.prepareValueForXML(operatorTypeFlipped.getDropdownLabel())+"</field_validation_operator>")
                .append("<field_validation_text>"+operatorType.getErrorMessage(curLanguage, value)+"</field_validation_text>")
                .append("<field_validation_type>warning</field_validation_type>")
                .append("<field_validation_value>"+value+"</field_validation_value>")
                .append("</validation_"+cnt+">");

        return ++cnt;
    }

    /**
     * returns metadata tag
     * @param codebookItem          codebookItem with information about the item
     * @param optionsMetadataString xml string with metadata information
     * @param fieldId               uuid of the field
     * @return metadata tag
     */
    private String addFieldMetaData(CodebookItem codebookItem, String optionsMetadataString, String fieldId){
        String codeSystem = codebookItem.getCodeSystemForItem();
        String metadata="";

        if(codeSystem.equalsIgnoreCase("") && optionsMetadataString.equalsIgnoreCase("")){
            return "";
        }
        else if(!codeSystem.equalsIgnoreCase("")) {
            String itemCode = codebookItem.getCodeForItem();
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
    private final String key;
    private final String itemId;
    private final String optionGroupId;
    private final String fieldId;
    private final List<Option> options = new ArrayList<>();

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
        String description;
        int order;

        /**
         * constructor
         * @param code the code of this option
         * @param order the order of this option in the list
         */
        Option(String code, int order){
            this.code = code;
            this.order = order;
            this.value = CodebookManager.getInstance().getValueForOptionCode(key, itemId, code);
            this.description = CodebookManager.getInstance().getDescriptionForOptionCode(key, itemId, code);
            this.codesystem = CodebookManager.getInstance().getCodesystemForOptionCode(key, itemId, code);
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
                    "<option_value>"+code+"</option_value>" +
//                    "<option_value>"+StringUtils.prepareValueForXML(value)+"</option_value>" +
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
                   "<metadata_description>"+StringUtils.prepareValueForXML(description)+"</metadata_description>" +
                   "<metadata_value>"+code+"</metadata_value>" +
                   "</m_"+metadataId+">";

        }
    }
}


