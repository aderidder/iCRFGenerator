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

import icrfgenerator.edc.edc.edcdefinitions.ODMDefinition;
import icrfgenerator.edc.edc.edcrunsettings.odm.ODMRunSettings;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.settings.runsettings.RunSettings;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * right side pane for ODM
 * this allows EDC specific content
 */
public class ODMSpecificPane extends EDCSpecificPaneDefaultCommonFields {

    private ComboBox<String> dataTypesComboBox;

    public ODMSpecificPane(){
    }

    /**
     * setup the edc-specific top-pane
     */
    void setupTopPaneEDC(GridPane gridPane, int rowNum){
        gridPane.add(new Label(I18N.getLanguageText("odmDataType")), 0, ++rowNum);
        dataTypesComboBox = new ComboBox<>();
        dataTypesComboBox.setPrefWidth(100);
        dataTypesComboBoxSetup();
        gridPane.add(dataTypesComboBox, 1, rowNum);

        setupTopPaneCommonFields(gridPane, rowNum);
    }

    /**
     * add the listeners
     */
    @Override
    void addListenersEDC_local(){
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
        ODMRunSettings runSettings = (ODMRunSettings) RunSettings.getInstance();
        dataTypesComboBox.setValue(runSettings.getSelectedItemDataType(key,itemId));
    }

    /**
     * disable fields
     */
    @Override
    void disableFieldsEDC_local(){
        dataTypesComboBox.setDisable(true);
    }

    /**
     * enable fields
     */
    @Override
    void enableFieldsEDC_local(){
        dataTypesComboBox.setDisable(false);
    }

    /**
     * store changes
     */
    @Override
    void storeAllValuesEDC_local(){
        dataTypeChange();
    }

    /**
     * store all values due to a group-select
     */
    @Override
    void storeAllValuesEDCGroupSelect_local(){
        storeDefaultDataType();
    }

    /**
     * returns whether the min/max fields should be enabled/disabled
     * @return true/false
     */
    @Override
    boolean checkMustEnableMinMaxUnits_local(){
        String selectedItem = dataTypesComboBox.getSelectionModel().getSelectedItem();
        if(selectedItem==null){
            return false;
        }
        return selectedItem.equalsIgnoreCase("integer")||selectedItem.equalsIgnoreCase("float");
    }

    /**
     * used by the group select to ensure the default value is stored
     */
    private void storeDefaultDataType(){
        ODMRunSettings runSettings = (ODMRunSettings) RunSettings.getInstance();
        String dataType = codebookItem.getItemDataType();
        // If there is no datatype, select the first value from either the fieldTypesWithoutCodelist or fieldTypesWithoutCodelist
        if(dataType.equalsIgnoreCase("")){
            if(codebookItem.hasCodeList()){
                runSettings.updateItemDataType(key, itemId, ODMDefinition.getDataTypesWithCodeList().get(0));
            }
            else{
                runSettings.updateItemDataType(key, itemId, ODMDefinition.getDataTypesWithoutCodeList().get(0));
            }
        }
        else {
            runSettings.updateItemDataType(key, itemId, ODMDefinition.convertDataTypeToEDCDataType(dataType));
        }
    }

    /**
     * handle data type change
     */
    private void dataTypeChange(){
        RunSettings.getInstance().updateItemDataType(key, itemId, dataTypesComboBox.getSelectionModel().getSelectedItem());
    }

    /**
     * create the data types combobox
     */
    private void dataTypesComboBoxSetup(){
        // if the codebook item has a codelist, add fields such as radio, single-select, etc
        if(codebookItem.hasCodeList()){
            dataTypesComboBox.setItems(FXCollections.observableList(ODMDefinition.getDataTypesWithCodeList()));
        }
        // if the codebook doesn't have a codelist, add fields such as text and textarea
        else{
            dataTypesComboBox.setItems(FXCollections.observableList(ODMDefinition.getDataTypesWithoutCodeList()));
        }

        // attempt to find the item's datatype
        String dataType = codebookItem.getItemDataType();
        if(!dataType.equalsIgnoreCase("")){
            // attempt to convert it to Castor's data type
            String convertedDataType = ODMDefinition.convertDataTypeToEDCDataType(dataType);
            dataTypesComboBox.getSelectionModel().select(convertedDataType);
        }
        else{
            // if there is no datatype for this item, select the first item in the list
            dataTypesComboBox.getSelectionModel().selectFirst();
        }
    }
}
