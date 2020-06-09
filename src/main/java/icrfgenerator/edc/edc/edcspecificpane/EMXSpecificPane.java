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

import icrfgenerator.edc.edc.edcdefinitions.EMXDefinition;
import icrfgenerator.edc.edc.edcrunsettings.emx.EMXRunSettings;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.settings.runsettings.RunSettings;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;


/**
 * right side pane for castor
 * this allow EDC specific content
 */
public class EMXSpecificPane extends EDCSpecificPaneDefault {
    private ComboBox<String> dataTypesComboBox;

    private TextField minTextField;
    private TextField maxTextField;
//    private TextField widthTextField;
    private CheckBox nillableCheckBox;
    private CheckBox aggregateableCheckBox;

    public EMXSpecificPane(){

    }


    @Override
    /**
     * create the top pane for the borderpane
     * this pane contain our EDC specific fields
     */
    void setupTopPaneEDC(GridPane gridPane, int rowNum){
        // add all the other items


        // create the combobox for the data types and add it
        gridPane.add(new Label(I18N.getLanguageText("emxDataType")), 0, ++rowNum);
//        gridPane.add(new Label("emxDataType"), 0, ++rowNum);
        dataTypesComboBox  = new ComboBox<>();
        dataTypesComboBox.setPrefWidth(100);
        dataTypesComboBoxSetup();
        gridPane.add(dataTypesComboBox, 1, rowNum);

        gridPane.add(new Label(I18N.getLanguageText("emxNillable")), 0, ++rowNum);
//        gridPane.add(new Label("emxNillable"), 0, ++rowNum);
        nillableCheckBox = new CheckBox();
        gridPane.add(nillableCheckBox, 1, rowNum);

        gridPane.add(new Label(I18N.getLanguageText("emxAggregateable")), 2, rowNum);
//        gridPane.add(new Label("emxAggregateable"), 2, rowNum);
        aggregateableCheckBox = new CheckBox();
        gridPane.add(aggregateableCheckBox, 3, rowNum);


        Label minLabel = new Label(I18N.getLanguageText("emxMin"));
        gridPane.add(minLabel,0,++rowNum);
        minTextField = new TextField();
        minTextField.setMaxWidth(40);
        gridPane.add(minTextField, 1, rowNum);

        Label maxLabel = new Label(I18N.getLanguageText("emxMax"));
        gridPane.add(maxLabel,2,rowNum);
        maxTextField = new TextField();
        maxTextField.setMaxWidth(40);
        gridPane.add(maxTextField, 3, rowNum);
    }


    /**
     * add listeners to the fields
     */
    @Override
    void addListenersEDC(){
        dataTypesComboBox.setOnAction(event -> {
            dataTypeChange();
            checkConditionalEnables();
        });
        minTextField.textProperty().addListener((observable, oldValue, newValue) -> minFieldChange());
        maxTextField.textProperty().addListener((observable, oldValue, newValue) -> maxFieldChange());
        nillableCheckBox.setOnAction(event -> {
            nillableFieldChange();
            checkConditionalEnables();
        });
        aggregateableCheckBox.setOnAction(event -> {
            aggregateableFieldChange();
            checkConditionalEnables();
        });

    }


    @Override
    void loadStoredValuesEDC() {
        EMXRunSettings runSettings = (EMXRunSettings) RunSettings.getInstance();
        dataTypesComboBox.setValue(runSettings.getSelectedItemDataType(key,itemId));
        nillableCheckBox.setSelected(runSettings.getSelectedItemNillableValue(key, itemId));
        aggregateableCheckBox.setSelected(runSettings.getSelectedItemAggregateableValue(key, itemId));
        minTextField.setText(runSettings.getSelectedItemMinValue(key, itemId));
        maxTextField.setText(runSettings.getSelectedItemMaxValue(key, itemId));
        codelistPane.setSelectedFields();
    }


    /**
     * setup for the dataTypes combobox
     */
    private void dataTypesComboBoxSetup(){
        // if the codebook item has a codelist, add fields such as radio, single-select, etc
        if(codebookItem.hasCodeList()){
            dataTypesComboBox.setItems(FXCollections.observableList(EMXDefinition.getFieldTypesWithCodeList()));
        }
        // if the codebook doesn't have a codelist, add fields such as text and textarea
        else{
            dataTypesComboBox.setItems(FXCollections.observableList(EMXDefinition.getFieldTypesWithoutCodeList()));
        }

        // attempt to find the item's datatype
        String dataType = codebookItem.getDataType();
        if(!dataType.equalsIgnoreCase("")){
            // attempt to convert it to Castor's data type
            String fieldType = EMXDefinition.convertDataTypeToEDCFieldType(dataType);
            dataTypesComboBox.getSelectionModel().select(fieldType);
        }
        else{
            // if there is no datatype for this item, select the first item in the list
            dataTypesComboBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * disable fields
     */
    @Override
    void disableFieldsEDC(){
        dataTypesComboBox.setDisable(true);
        setDisableAggregateable(true);
        setDisableNillable(true);
        setDisableMinMax(true);
        codelistPane.disableFields();
    }

    /**
     * enabled fields
     */
    @Override
    void enableFieldsEDC(){
        dataTypesComboBox.setDisable(false);
//        setDisableAggregateable(false);
//        setDisableNillable(false);
        checkConditionalEnables();
    }

    /**
     * store the changes in the runsettings
     */
    @Override
    void storeAllValuesEDC(){
        dataTypeChange();
        nillableFieldChange();
        aggregateableFieldChange();
        minFieldChange();
        maxFieldChange();
    }

    /**
     * the field type has changed. Update its value and perform additional actions
     */
    private void dataTypeChange(){
        String selectedItem = dataTypesComboBox.getSelectionModel().getSelectedItem();
        EMXRunSettings runSettings = (EMXRunSettings) RunSettings.getInstance();
        runSettings.updateItemDataType(key, itemId, selectedItem);
    }

    /**
     * check whether fields that depend on the fieldType's value should be enabled
     */
    private void checkConditionalEnables(){
        if(mustEnableNillable()){
            setDisableNillable(false);
        }
        else{
            setDisableNillable(true);
        }
        if(mustEnableAggregateable()){
            setDisableAggregateable(false);
        }
        else{
            setDisableAggregateable(true);
        }


        if (mustEnableMinMax()) {
            setDisableMinMax(false);
        }
        else {
            clearMinMax();
            setDisableMinMax(true);
        }
    }

    private boolean mustEnableNillable(){
        Boolean isSelected = aggregateableCheckBox.isSelected();
        if(isSelected==null){
            return false;
        }
        return !isSelected;
    }

    private boolean mustEnableAggregateable(){
        Boolean isSelected = nillableCheckBox.isSelected();
        if(isSelected==null){
            return false;
        }
        return !isSelected;
    }


    /**
     * en/disable the required field
     * @param disable true/false
     */
    private void setDisableNillable(boolean disable){
        nillableCheckBox.setDisable(disable);
    }

    /**
     * en/disable the required field
     * @param disable true/false
     */
    private void setDisableAggregateable(boolean disable){
        aggregateableCheckBox.setDisable(disable);
    }

    /**
     * update the stored nillable value
     */
    private void nillableFieldChange(){
        EMXRunSettings runSettings = (EMXRunSettings) RunSettings.getInstance();
        runSettings.updateItemNillableValue(key, itemId, nillableCheckBox.isSelected());
    }

    /**
     * update the stored aggregateable value
     */
    private void aggregateableFieldChange(){
        EMXRunSettings runSettings = (EMXRunSettings) RunSettings.getInstance();
        runSettings.updateItemAggregateableValue(key, itemId, aggregateableCheckBox.isSelected());
    }


    /**
     * clear the min/max fields
     */
    private void clearMinMax(){
        maxTextField.setText("");
        minTextField.setText("");
    }

    /**
     * disable the min/max fields
     * @param disable true/false
     */
    private void setDisableMinMax(boolean disable){
        minTextField.setDisable(disable);
        maxTextField.setDisable(disable);
    }

    /**
     * returns whether the min/max fields should be enabled/disabled
     * @return true/false
     */
    private boolean mustEnableMinMax(){
        String selectedItem = dataTypesComboBox.getSelectionModel().getSelectedItem();
        if(selectedItem==null){
            return false;
        }
        return selectedItem.equalsIgnoreCase("int")||selectedItem.equalsIgnoreCase("long");
    }

    /**
     * update the stored Min value
     */
    private void minFieldChange(){
        EMXRunSettings runSettings = (EMXRunSettings) RunSettings.getInstance();
        runSettings.updateItemMinValue(key, itemId, minTextField.getText());
    }

    /**
     * update the stored Max value
     */
    private void maxFieldChange(){
        EMXRunSettings runSettings = (EMXRunSettings) RunSettings.getInstance();
        runSettings.updateItemMaxValue(key, itemId, maxTextField.getText());
    }
}
