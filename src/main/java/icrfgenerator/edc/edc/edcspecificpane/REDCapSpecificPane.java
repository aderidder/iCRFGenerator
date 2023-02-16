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

package icrfgenerator.edc.edc.edcspecificpane;

import icrfgenerator.edc.edc.edcdefinitions.REDCapDefinition;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.edc.edc.edcrunsettings.redcap.REDCapRunSettings;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * Right side pane for REDCap
 */
public class REDCapSpecificPane extends EDCSpecificPaneDefaultCommonFields {

    private ComboBox<String> fieldTypesComboBox;
    private ComboBox<String> textValidationComboBox;

    public REDCapSpecificPane(){

    }

    /**
     * the pane with the redcap-specific items
     */
    void setupTopPaneEDC(GridPane gridPane, int rowNum){
        // create the combobox for the field types and add it
        gridPane.add(new Label(I18N.getLanguageText("redCapFieldType")), 0, ++rowNum);
        fieldTypesComboBox = new ComboBox<>();
        fieldTypesComboBox.setPrefWidth(100);
        fieldTypesComboBoxSetup();
        gridPane.add(fieldTypesComboBox, 1, rowNum);

        gridPane.add(new Label(I18N.getLanguageText("redCapTextValidationType")), 0, ++rowNum);
        textValidationComboBox = new ComboBox<>();
        textValidationComboBox .setPrefWidth(100);
        gridPane.add(textValidationComboBox, 1, rowNum);

        setupTopPaneCommonFields(gridPane, rowNum);

        borderPane.setTop(gridPane);

        textValidationComboBoxSetup();
    }

    /**
     * add the listeners
     */
    @Override
    void addListenersEDC_local(){
        fieldTypesComboBox.setOnAction(event -> {
            fieldTypeChange();
            checkConditionalEnables();
        });
        textValidationComboBox.setOnAction(event -> {
            textValidationChange();
            checkConditionalEnables();
        });
    }

    /**
     * load values
     */
    @Override
    void loadStoredValuesEDC_local() {
        REDCapRunSettings runSettings = (REDCapRunSettings) RunSettings.getInstance();
        fieldTypesComboBox.setValue(runSettings.getSelectedItemFieldType(key,itemId));
        textValidationComboBox.setValue(runSettings.getSelectedItemTextValidationType(key, itemId));
    }

    /**
     * disable fields
     */
    @Override
    void disableFieldsEDC_local(){
        fieldTypesComboBox.setDisable(true);
        textValidationComboBox.setDisable(true);
    }

    /**
     * enable fields
     */
    @Override
    void enableFieldsEDC_local(){
        fieldTypesComboBox.setDisable(false);
        checkConditionalEnables();
    }

    /**
     * checks for local fields influencing each other
     */
    private void checkConditionalEnables(){
        // fieldType can be text or notes, or checkbox, singleselect, etc.
        // the textValidationsComboBox is enabled when the fieldType is text
        String selectedFieldType = fieldTypesComboBox.getSelectionModel().getSelectedItem();
        if(selectedFieldType.equalsIgnoreCase("text")){
            textValidationComboBox.setDisable(false);
        }
        else{
            textValidationComboBox.getSelectionModel().select("");
            textValidationComboBox.setDisable(true);
        }
        // check whether this means the min / max / units fields must also be enabled / disabled
        checkMustEnableMinMaxUnits();
    }

    /**
     * store values
     */
    @Override
    void storeAllValuesEDC_local(){
        fieldTypeChange();
        textValidationChange();
    }

    /**
     * store default values when a group select is performed
     */
    @Override
    void storeAllValuesEDCGroupSelect_local(){
        storeDefaultFieldType();
        REDCapRunSettings runSettings = (REDCapRunSettings) RunSettings.getInstance();
        runSettings.updateItemTextValidationType(key, itemId, REDCapDefinition.convertDataTypeToEDCValidationType(codebookItem.getItemDataType()));
    }


    /**
     * determines whether min/max/units fields must be enabled based on the selected fieldType and the validationType
     */
    @Override
    boolean checkMustEnableMinMaxUnits_local() {
        // fieldType can be text or notes
        // if it's text, the min/max validations may be used, but only if we're dealing with number or integer (not date)
        String selectedFieldType = fieldTypesComboBox.getSelectionModel().getSelectedItem();
        if(selectedFieldType.equalsIgnoreCase("text")){
            String selectedValidation = textValidationComboBox.getSelectionModel().getSelectedItem();
            return selectedValidation.equalsIgnoreCase("number") || selectedValidation.equalsIgnoreCase("integer");
        }
        else{
            return false;
        }
    }

    /**
     * create the field types combobox
     */
    private void fieldTypesComboBoxSetup(){
        // if the codebook item has a codelist, add fields such as radio, single-select, etc, based on the EDC
        if(codebookItem.hasCodeList()){
            fieldTypesComboBox.setItems(FXCollections.observableList(REDCapDefinition.getFieldTypesWithCodeList()));
        }
        // if the codebook doesn't have a codelist, add fields such as text and textarea, based on the EDC
        else{
            fieldTypesComboBox.setItems(FXCollections.observableList(REDCapDefinition.getFieldTypesWithoutCodeList()));
        }

        fieldTypesComboBox.getSelectionModel().selectFirst();
    }

    /**
     * create the text validation combobox
     */
    private void textValidationComboBoxSetup(){
        textValidationComboBox.setItems(FXCollections.observableList(REDCapDefinition.getTextValidationOptionsList()));
        String validationType = REDCapDefinition.convertDataTypeToEDCValidationType(codebookItem.getItemDataType());
        textValidationComboBox.getSelectionModel().select(validationType);
    }


    private void storeDefaultFieldType(){
        REDCapRunSettings runSettings = (REDCapRunSettings) RunSettings.getInstance();
        // if the codebook item has a codelist, add fields such as radio, single-select, etc, based on the EDC
        if(codebookItem.hasCodeList()){
            runSettings.updateItemFieldType(key, itemId, FXCollections.observableList(REDCapDefinition.getFieldTypesWithCodeList()).get(0));
        }
        // if the codebook doesn't have a codelist, add fields such as text and textarea, based on the EDC
        else{
            runSettings.updateItemFieldType(key, itemId, FXCollections.observableList(REDCapDefinition.getFieldTypesWithoutCodeList()).get(0));
        }
    }

    /**
     * field type has changed, store the changes and perform additional actions
     */
    private void fieldTypeChange(){
        REDCapRunSettings runSettings = (REDCapRunSettings) RunSettings.getInstance();
        runSettings.updateItemFieldType(key, itemId, fieldTypesComboBox.getSelectionModel().getSelectedItem());
    }

    /**
     * update the text validation type value and check whether the min/max fields need to be changed
     */
    private void textValidationChange(){
        String selectedItem = textValidationComboBox.getSelectionModel().getSelectedItem();
        if(selectedItem!=null) {
            REDCapRunSettings runSettings = (REDCapRunSettings) RunSettings.getInstance();
            runSettings.updateItemTextValidationType(key, itemId, selectedItem);
        }
    }
}
