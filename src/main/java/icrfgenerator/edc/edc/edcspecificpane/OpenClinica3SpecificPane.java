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
import icrfgenerator.utils.GUIUtils;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * right side pane for OpenClinica 3
 * this allow EDC specific content
 */
public class OpenClinica3SpecificPane extends EDCSpecificPaneDefault {

    private ComboBox<String> fieldTypesComboBox;
    private ComboBox<String> dataTypesComboBox;

    public OpenClinica3SpecificPane(){

    }

    /**
     * setup the edc-specific top-pane
     */
    void setupTopPaneEDC(GridPane gridPane, int rowNum){
        // create the combobox for the field types and add it
        gridPane.add(new Label(I18N.getLanguageText("oc3FieldType")), 0, ++rowNum);
        fieldTypesComboBox = new ComboBox<>();
        fieldTypesComboBoxSetup();
        gridPane.add(fieldTypesComboBox, 1, rowNum);

        // create the combobox for the data types and add it
        gridPane.add(new Label(I18N.getLanguageText("oc3DataType")), 0, ++rowNum);
        dataTypesComboBox = GUIUtils.createComboBox("", OpenClinica3Definition.getSupportedDatatypes());
        dataTypesComboBoxSetup();
        gridPane.add(dataTypesComboBox, 1, rowNum);
    }

    /**
     * add the listeners
     */
    void addListenersEDC(){
        fieldTypesComboBox.setOnAction(event -> fieldTypeChange());
        dataTypesComboBox.setOnAction(event -> dataTypeChange());
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
     * create the data types combobox
     */
    private void dataTypesComboBoxSetup(){
        // get the datatype which, according to art-decor, the item has and convert it to the selected EDC
        String datatype = codebookItem.getDataType();
        String codeDataType = codebookItem.getCodeDataType();
        String formattedDataType = OpenClinica3Definition.convertDataTypeToEDCDataType(datatype, codeDataType);
        dataTypesComboBox.getSelectionModel().select(formattedDataType);
    }

    /**
     * load the values that are stored
     */
    void loadStoredValuesEDC(){
        OpenClinica3RunSettings runSettings = (OpenClinica3RunSettings) RunSettings.getInstance();
        dataTypesComboBox.setValue(runSettings.getSelectedItemDataType(key,itemId));
        fieldTypesComboBox.setValue(runSettings.getSelectedItemFieldType(key,itemId));
        codelistPane.setSelectedFields();
    }


    /**
     * disable fields
     */
    @Override
    void disableFieldsEDC(){
        fieldTypesComboBox.setDisable(true);
        dataTypesComboBox.setDisable(true);
        codelistPane.disableFields();
    }

    /**
     * enable fields
     */
    @Override
    void enableFieldsEDC(){
        fieldTypesComboBox.setDisable(false);
        dataTypesComboBox.setDisable(false);
    }

    /**
     * store changes
     */
    @Override
    void storeAllValuesEDC(){
        fieldTypeChange();
        dataTypeChange();
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
        ((OpenClinica3RunSettings)RunSettings.getInstance()).updateItemDataType(key, itemId, dataTypesComboBox.getSelectionModel().getSelectedItem());
    }
}
