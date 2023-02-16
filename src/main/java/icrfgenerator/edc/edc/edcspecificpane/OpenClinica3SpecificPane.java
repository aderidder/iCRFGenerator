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

import icrfgenerator.edc.edc.edcdefinitions.OpenClinica3Definition;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.edc.edc.edcrunsettings.openclinica3.OpenClinica3RunSettings;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * right side pane for OpenClinica 3 / LibreClinica
 * this allows EDC specific content
 */
public class OpenClinica3SpecificPane extends EDCSpecificPaneDefaultCommonFields {

    private ComboBox<String> fieldTypesComboBox;
    private ComboBox<String> dataTypesComboBox;

    public OpenClinica3SpecificPane(){

    }

    /**
     * create the top pane for the borderpane
     * this pane contain our EDC specific fields
     */
    @Override
    void setupTopPaneEDC(GridPane gridPane, int rowNum){
        // create the combobox for the field types and add it
        gridPane.add(new Label(I18N.getLanguageText("oc3FieldType")), 0, ++rowNum);
        fieldTypesComboBox = new ComboBox<>();
        fieldTypesComboBoxSetup();
        gridPane.add(fieldTypesComboBox, 1, rowNum);

        // create the combobox for the data types and add it
        gridPane.add(new Label(I18N.getLanguageText("oc3DataType")), 0, ++rowNum);
        dataTypesComboBox = new ComboBox<>();
        dataTypesComboBoxSetup();
        gridPane.add(dataTypesComboBox, 1, rowNum);

        setupTopPaneCommonFields(gridPane, rowNum);
    }

    /**
     * add the listeners
     */
    @Override
    void addListenersEDC_local(){
        // a change in the fieldTypesComboBox may alter the dataTypesComboBox
        // e.g. when fieldTypes becomes textarea, the only dataType is string
        fieldTypesComboBox.setOnAction(event -> {
            fieldTypeChange();
            dataTypesComboBoxSetup();
            checkMustEnableMinMaxUnits();
        });
        dataTypesComboBox.setOnAction(event -> {
            dataTypeChange();
            checkMustEnableMinMaxUnits();
        });
    }

    /**
     * load the values that are stored
     */
    @Override
    void loadStoredValuesEDC_local(){
        OpenClinica3RunSettings runSettings = (OpenClinica3RunSettings) RunSettings.getInstance();
        dataTypesComboBox.setValue(runSettings.getSelectedItemDataType(key,itemId));
        fieldTypesComboBox.setValue(runSettings.getSelectedItemFieldType(key,itemId));
    }

    /**
     * disable fields
     */
    @Override
    void disableFieldsEDC_local(){
        fieldTypesComboBox.setDisable(true);
        dataTypesComboBox.setDisable(true);
    }

    /**
     * enable fields
     */
    @Override
    void enableFieldsEDC_local(){
        fieldTypesComboBox.setDisable(false);
        dataTypesComboBox.setDisable(false);
    }

    /**
     * store changes
     */
    @Override
    void storeAllValuesEDC_local(){
        fieldTypeChange();
        dataTypeChange();
        storeAllValuesCommonFields();
    }

    @Override
    void storeAllValuesEDCGroupSelect_local(){
        OpenClinica3RunSettings runSettings = (OpenClinica3RunSettings) RunSettings.getInstance();
        storeDefaultFieldType();
        runSettings.updateItemDataType(key, itemId, OpenClinica3Definition.convertDataTypeToEDCDataType(codebookItem.getItemDataType(), codebookItem.getItemCodeDataType()));
    }

    @Override
    boolean checkMustEnableMinMaxUnits_local(){
        String selectedFieldTypeItem = fieldTypesComboBox.getSelectionModel().getSelectedItem();
        if(selectedFieldTypeItem.equalsIgnoreCase("text")) {
            String selectedDataTypeItem = dataTypesComboBox.getSelectionModel().getSelectedItem();
            if (selectedDataTypeItem == null) {
                return false;
            }
            return selectedDataTypeItem.equalsIgnoreCase("INT") || selectedDataTypeItem.equalsIgnoreCase("REAL");
        }
        return false;
    }

    /**
     * create the field types combobox
     */
    private void fieldTypesComboBoxSetup(){
        // if the codebook item has a codelist, add fields such as radio, single-select, etc, based on the EDC
        if(codebookItem.hasCodeList()){
            fieldTypesComboBox.setItems(FXCollections.observableList(OpenClinica3Definition.getFieldTypesWithCodeList()));
        }
        // if the codebook doesn't have a codelist, add fields such as text and textarea, based on the EDC
        else{
            fieldTypesComboBox.setItems(FXCollections.observableList(OpenClinica3Definition.getFieldTypesWithoutCodeList()));
        }
        fieldTypesComboBox.getSelectionModel().selectFirst();
    }

    /**
     * create / update the dataTypesComboBox
     * happens when the form is created as well as when the FieldTypes value is changed
     */
    private void dataTypesComboBoxSetup(){
        String datatype = codebookItem.getItemDataType();
        String fieldType = fieldTypesComboBox.getSelectionModel().getSelectedItem();

        dataTypesComboBox.setItems(FXCollections.observableList(OpenClinica3Definition.getDataTypesList(fieldType)));

        String codeDataType = codebookItem.getItemCodeDataType();
        String formattedDataType = OpenClinica3Definition.convertDataTypeToEDCDataType(datatype, codeDataType);
        if(dataTypesComboBox.getItems().contains(formattedDataType)){
            dataTypesComboBox.getSelectionModel().select(formattedDataType);
        }
        else {
            dataTypesComboBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * used by the group select to ensure the default value is stored
     */
    private void storeDefaultFieldType(){
        OpenClinica3RunSettings runSettings = (OpenClinica3RunSettings) RunSettings.getInstance();
        if(codebookItem.hasCodeList()){
            runSettings.updateItemFieldType(key, itemId, FXCollections.observableList(OpenClinica3Definition.getFieldTypesWithCodeList()).get(0));
        }
        else{
            runSettings.updateItemFieldType(key, itemId, FXCollections.observableList(OpenClinica3Definition.getFieldTypesWithoutCodeList()).get(0));
        }
    }

    /**
     * handle field type change
     */
    private void fieldTypeChange(){
        ((OpenClinica3RunSettings)RunSettings.getInstance()).updateItemFieldType(key, itemId, fieldTypesComboBox.getSelectionModel().getSelectedItem());
    }

    /**
     * handle data type change
     */
    private void dataTypeChange(){
        RunSettings.getInstance().updateItemDataType(key, itemId, dataTypesComboBox.getSelectionModel().getSelectedItem());
    }
}
