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

import icrfgenerator.edc.edc.edcdefinitions.CastorDefinition;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.edc.edc.edcrunsettings.castor.CastorRunSettings;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;


/**
 * right side pane for castor
 * this allows EDC specific content
 */
public class CastorSpecificPane extends EDCSpecificPaneDefaultCommonFields {
    private ComboBox<String> fieldTypesComboBox;
    private CheckBox enforceDecimalsCheckBox;

    public CastorSpecificPane(){

    }

    /**
     * create the top pane for the borderpane
     * this pane contain our EDC specific fields
     */
    @Override
    void setupTopPaneEDC(GridPane gridPane, int rowNum){
        // add all the other items
        gridPane.add(new Label(I18N.getLanguageText("castorFieldType")), 0, ++rowNum);
        fieldTypesComboBox = new ComboBox<>();
        fieldTypesComboBox.setPrefWidth(100);
        fieldTypesComboBoxSetup();
        gridPane.add(fieldTypesComboBox, 1, rowNum);

        setupTopPaneCommonFields(gridPane, rowNum);

        gridPane.add(new Label(I18N.getLanguageText("castorPrecision")), 2, rowNum);
        enforceDecimalsCheckBox = new CheckBox();
        gridPane.add(enforceDecimalsCheckBox, 3, rowNum);

        enforceDecimalsCheckBox.setSelected(Integer.parseInt(codebookItem.getPrecision())>0);
    }

    /**
     * add listeners to the fields
     */
    @Override
    void addListenersEDC_local(){
        fieldTypesComboBox.setOnAction(event -> {
            fieldTypeChange();
            checkConditionalEnables();
            checkMustEnableMinMaxUnits();
        });
        enforceDecimalsCheckBox.setOnAction(event -> enforceDecimalsFieldChange());
    }

    /**
     * load the values that are currently stored
     */
    @Override
    void loadStoredValuesEDC_local() {
        CastorRunSettings runSettings = (CastorRunSettings) RunSettings.getInstance();
        fieldTypesComboBox.setValue(runSettings.getSelectedItemFieldType(key,itemId));
        enforceDecimalsCheckBox.setSelected(runSettings.getSelectedItemEnforceDecimalsValue(key, itemId));
    }

    /**
     * disable fields
     */
    @Override
    void disableFieldsEDC_local(){
        fieldTypesComboBox.setDisable(true);
        enforceDecimalsCheckBox.setDisable(true);
    }

    /**
     * enabled fields
     */
    @Override
    void enableFieldsEDC_local(){
        fieldTypesComboBox.setDisable(false);
        checkConditionalEnables();
    }

    /**
     * enable / disable local fields
     */
    private void checkConditionalEnables(){
        if(checkMustEnableMinMaxUnits_local()){
            enforceDecimalsCheckBox.setDisable(false);
        }
        else{
            enforceDecimalsCheckBox.setSelected(false);
            enforceDecimalsCheckBox.setDisable(true);
        }
    }

    /**
     * store all values
     */
    @Override
    void storeAllValuesEDC_local(){
        fieldTypeChange();
        enforceDecimalsFieldChange();
    }

    /**
     * store values when an item is selected via groupselect
     */
    @Override
    void storeAllValuesEDCGroupSelect_local(){
        storeDefaultFieldType();
        CastorRunSettings runSettings = (CastorRunSettings) RunSettings.getInstance();
        runSettings.updateItemEnforceDecimalsValue(key, itemId, Integer.parseInt(codebookItem.getPrecision())>0);
    }

    /**
     * decides whether the min / max / units fields must be enabled or disabled
     * @return true/false
     */
    @Override
    boolean checkMustEnableMinMaxUnits_local(){
        String selectedItem = fieldTypesComboBox.getSelectionModel().getSelectedItem();
        if(selectedItem==null){
            return false;
        }
        return selectedItem.equalsIgnoreCase("numeric");
    }

    /**
     * used by the group select to ensure the default value is stored
     */
    private void storeDefaultFieldType(){
        CastorRunSettings runSettings = (CastorRunSettings) RunSettings.getInstance();
        String dataType = codebookItem.getItemDataType();
        // If there is no datatype, select the first value from either the fieldTypesWithoutCodelist or fieldTypesWithoutCodelist
        if(dataType.equalsIgnoreCase("")){
            if(codebookItem.hasCodeList()){
                runSettings.updateItemFieldType(key, itemId, CastorDefinition.getFieldTypesWithCodeList().get(0));
            }
            else{
                runSettings.updateItemFieldType(key, itemId, CastorDefinition.getFieldTypesWithoutCodeList().get(0));
            }
        }
        else {
            runSettings.updateItemFieldType(key, itemId, CastorDefinition.convertDataTypeToEDCFieldType(dataType));
        }
    }

    /**
     * update the fieldType
     */
    private void fieldTypeChange(){
        String selectedItem = fieldTypesComboBox.getSelectionModel().getSelectedItem();
        CastorRunSettings runSettings = (CastorRunSettings) RunSettings.getInstance();
        runSettings.updateItemFieldType(key, itemId, selectedItem);
    }

    /**
     * update the stored enforceDecimals value
     */
    private void enforceDecimalsFieldChange(){
        CastorRunSettings runSettings = (CastorRunSettings) RunSettings.getInstance();
        runSettings.updateItemEnforceDecimalsValue(key, itemId, enforceDecimalsCheckBox.isSelected());
    }

    /**
     * setup for the fieldTypes combobox
     */
    private void fieldTypesComboBoxSetup(){
        // if the codebook item has a codelist, add fields such as radio, single-select, etc
        if(codebookItem.hasCodeList()){
            fieldTypesComboBox.setItems(FXCollections.observableList(CastorDefinition.getFieldTypesWithCodeList()));
        }
        // if the codebook doesn't have a codelist, add fields such as text and textarea
        else{
            fieldTypesComboBox.setItems(FXCollections.observableList(CastorDefinition.getFieldTypesWithoutCodeList()));
        }

        // attempt to find the item's datatype
        String dataType = codebookItem.getItemDataType();
        if(!dataType.equalsIgnoreCase("")){
            // attempt to convert it to Castor's data type
            String fieldType = CastorDefinition.convertDataTypeToEDCFieldType(dataType);
            fieldTypesComboBox.getSelectionModel().select(fieldType);
        }
        else{
            // if there is no datatype for this item, select the first item in the list
            fieldTypesComboBox.getSelectionModel().selectFirst();
        }
    }
}
