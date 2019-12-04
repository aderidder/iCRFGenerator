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
 * this allow EDC specific content
 */
public class CastorSpecificPane extends EDCSpecificPaneDefault {
    private ComboBox<String> fieldTypesComboBox;

    private TextField minTextField;
    private TextField maxTextField;
    private TextField widthTextField;
    private CheckBox requiredCheckBox;

    public CastorSpecificPane(){

    }


    @Override
    /**
     * create the top pane for the borderpane
     * this pane contain our EDC specific fields
     */
    void setupTopPaneEDC(GridPane gridPane, int rowNum){
        // add all the other items
        gridPane.add(new Label(I18N.getLanguageText("castorFieldType")), 0, ++rowNum);
        fieldTypesComboBox = new ComboBox<>();
        fieldTypesComboBox.setPrefWidth(100);
        fieldTypesComboBoxSetup();
        gridPane.add(fieldTypesComboBox, 1, rowNum);

        gridPane.add(new Label(I18N.getLanguageText("castorRequired")), 2, rowNum);
        requiredCheckBox = new CheckBox();
        gridPane.add(requiredCheckBox, 3, rowNum);

        Label minLabel = new Label(I18N.getLanguageText("castorMin"));
        gridPane.add(minLabel,0,++rowNum);
        minTextField = new TextField();
        minTextField.setMaxWidth(40);
        gridPane.add(minTextField, 1, rowNum);

        Label maxLabel = new Label(I18N.getLanguageText("castorMax"));
        gridPane.add(maxLabel,2,rowNum);
        maxTextField = new TextField();
        maxTextField.setMaxWidth(40);
        gridPane.add(maxTextField, 3, rowNum);

        gridPane.add(new Label(I18N.getLanguageText("castorWidth")), 0, ++rowNum);
        widthTextField = new TextField();
        widthTextField.setMaxWidth(40);
        widthTextField.setTooltip(new Tooltip(I18N.getLanguageText("castorWidthHelp")));
        gridPane.add(widthTextField, 1, rowNum);

    }


    /**
     * add listeners to the fields
     */
    @Override
    void addListenersEDC(){
        fieldTypesComboBox.setOnAction(event -> {
            fieldTypeChange();
            checkConditionalEnables();
        });
        minTextField.textProperty().addListener((observable, oldValue, newValue) -> minFieldChange());
        maxTextField.textProperty().addListener((observable, oldValue, newValue) -> maxFieldChange());
        requiredCheckBox.setOnAction(event -> requiredFieldChange());
        widthTextField.textProperty().addListener((observable, oldValue, newValue) -> widthFieldChange());
    }


    @Override
    void loadStoredValuesEDC() {
        CastorRunSettings runSettings = (CastorRunSettings) RunSettings.getInstance();
        fieldTypesComboBox.setValue(runSettings.getSelectedItemFieldType(key,itemId));
        requiredCheckBox.setSelected(runSettings.getSelectedItemRequiredValue(key, itemId));
        minTextField.setText(runSettings.getSelectedItemMinValue(key, itemId));
        maxTextField.setText(runSettings.getSelectedItemMaxValue(key, itemId));
        widthTextField.setText(runSettings.getSelectedItemWidthValue(key, itemId));
        codelistPane.setSelectedFields();
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
        String dataType = codebookItem.getDataType();
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

    /**
     * disable fields
     */
    @Override
    void disableFieldsEDC(){
        fieldTypesComboBox.setDisable(true);
        setDisableRequired(true);
        setDisableWidth(true);
        setDisableMinMax(true);
        codelistPane.disableFields();
    }

    /**
     * enabled fields
     */
    @Override
    void enableFieldsEDC(){
        fieldTypesComboBox.setDisable(false);
        setDisableRequired(false);
        checkConditionalEnables();
    }

    /**
     * store the changes in the runsettings
     */
    @Override
    void storeAllValuesEDC(){
        fieldTypeChange();
        requiredFieldChange();
        minFieldChange();
        maxFieldChange();
        widthFieldChange();
    }

    /**
     * the field type has changed. Update its value and perform additional actions
     */
    private void fieldTypeChange(){
        String selectedItem = fieldTypesComboBox.getSelectionModel().getSelectedItem();
        CastorRunSettings runSettings = (CastorRunSettings) RunSettings.getInstance();
        runSettings.updateItemFieldType(key, itemId, selectedItem);
    }

    /**
     * check whether fields that depend on the fieldType's value should be enabled
     */
    private void checkConditionalEnables(){
        if (mustEnableMinMax()) {
            setDisableMinMax(false);
        }
        else {
            clearMinMax();
            setDisableMinMax(true);
        }
        if(mustEnableWidth()){
            setDisableWidth(false);
        }
        else{
            clearWidth();
            setDisableWidth(true);
        }
    }

    /**
     * en/disable the required field
     * @param disable true/false
     */
    private void setDisableRequired(boolean disable){
        requiredCheckBox.setDisable(disable);
    }

    /**
     * update the stored Required value
     */
    private void requiredFieldChange(){
        CastorRunSettings runSettings = (CastorRunSettings) RunSettings.getInstance();
        runSettings.updateItemRequiredValue(key, itemId, requiredCheckBox.isSelected());
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
        String selectedItem = fieldTypesComboBox.getSelectionModel().getSelectedItem();
        if(selectedItem==null){
            return false;
        }
        return selectedItem.equalsIgnoreCase("numeric");
    }

    /**
     * update the stored Min value
     */
    private void minFieldChange(){
        CastorRunSettings runSettings = (CastorRunSettings) RunSettings.getInstance();
        runSettings.updateItemMinValue(key, itemId, minTextField.getText());
    }

    /**
     * update the stored Max value
     */
    private void maxFieldChange(){
        CastorRunSettings runSettings = (CastorRunSettings) RunSettings.getInstance();
        runSettings.updateItemMaxValue(key, itemId, maxTextField.getText());
    }

    /**
     * clear the width field
     */
    private void clearWidth(){
        widthTextField.setText("");
    }

    /**
     * en/disables the width field
     * @param disable true/false
     */
    private void setDisableWidth(boolean disable){
        widthTextField.setDisable(disable);
    }

    /**
     * check whether to enable the width field
     * @return true/false
     */
    private boolean mustEnableWidth(){
        String selectedItem = fieldTypesComboBox.getSelectionModel().getSelectedItem();
        if(selectedItem==null){
            return false;
        }
        return selectedItem.equalsIgnoreCase("numeric") || selectedItem.equalsIgnoreCase("string");
    }

    /**
     * update the stored width field
     */
    private void widthFieldChange(){
        CastorRunSettings runSettings = (CastorRunSettings) RunSettings.getInstance();
        runSettings.updateItemWidthValue(key, itemId, widthTextField.getText());
    }
}
