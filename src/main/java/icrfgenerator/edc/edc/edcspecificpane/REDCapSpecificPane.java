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
public class REDCapSpecificPane extends EDCSpecificPaneDefault {

    private ComboBox<String> fieldTypesComboBox;
    private ComboBox<String> textValidationComboBox;

    private TextField minTextField;
    private TextField maxTextField;

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
        textValidationComboBoxSetup();
        gridPane.add(textValidationComboBox, 1, rowNum);

        Label minLabel = new Label(I18N.getLanguageText("redCapMin"));
        gridPane.add(minLabel,2,rowNum);
        minTextField = new TextField();
        minTextField.setMaxWidth(40);
        gridPane.add(minTextField, 3, rowNum);

        Label maxLabel = new Label(I18N.getLanguageText("redCapMax"));
        gridPane.add(maxLabel,4,rowNum);
        maxTextField = new TextField();
        maxTextField.setMaxWidth(40);
        gridPane.add(maxTextField, 5, rowNum);

        setDisableMinMax(true);
        borderPane.setTop(gridPane);
    }

    /**
     * add the listeners
     */
    void addListenersEDC(){
        fieldTypesComboBox.setOnAction(event -> {
            fieldTypeChange();
            checkConditionalEnables();
        });
        textValidationComboBox.setOnAction(event -> {
            textValidationChange();
            checkConditionalEnables();
        });
        minTextField.textProperty().addListener((observable, oldValue, newValue) -> minFieldChange());
        maxTextField.textProperty().addListener((observable, oldValue, newValue) -> maxFieldChange());
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
     * create the text validiation combobox
     */
    private void textValidationComboBoxSetup(){
        // since it's a validation field, at this moment we're not guessing the initial value based on the Art-Decor
        // datatype. Maybe in the future?
        textValidationComboBox.setItems(FXCollections.observableList(REDCapDefinition.getTextValidationOptionsList()));
    }

    @Override
    void loadStoredValuesEDC() {
        REDCapRunSettings runSettings = (REDCapRunSettings) RunSettings.getInstance();
        fieldTypesComboBox.setValue(runSettings.getSelectedItemFieldType(key,itemId));
        textValidationComboBox.setValue(runSettings.getSelectedItemTextValidationType(key, itemId));
        minTextField.setText(runSettings.getSelectedItemMinValue(key, itemId));
        maxTextField.setText(runSettings.getSelectedItemMaxValue(key, itemId));
        codelistPane.setSelectedFields();
    }

    /**
     * disable fields
     */
    @Override
    void disableFieldsEDC(){
        fieldTypesComboBox.setDisable(true);
        textValidationComboBox.setDisable(true);
        setDisableMinMax(true);
        codelistPane.disableFields();
    }

    /**
     * enable fields
     */
    @Override
    void enableFieldsEDC(){
        fieldTypesComboBox.setDisable(false);
        checkConditionalEnables();
    }

    /**
     * store values
     */
    @Override
    void storeAllValuesEDC(){
        fieldTypeChange();
        textValidationChange();
        maxFieldChange();
        minFieldChange();
    }

    /**
     * field type has changed, store the changes and perform additional actions
     */
    private void fieldTypeChange(){
        REDCapRunSettings runSettings = (REDCapRunSettings) RunSettings.getInstance();
        runSettings.updateItemFieldType(key, itemId, fieldTypesComboBox.getSelectionModel().getSelectedItem());
    }

    private void checkConditionalEnables(){
        if(fieldTypesComboBox.getSelectionModel().getSelectedItem().equalsIgnoreCase("text")){
            textValidationComboBox.setDisable(false);
        }
        else{
            textValidationComboBox.getSelectionModel().select("");
            textValidationComboBox.setDisable(true);
        }
        if (mustEnableMinMax()) {
            setDisableMinMax(false);
        } else {
            clearMinMax();
            setDisableMinMax(true);
        }
    }

    /**
     * update the stored Min value
     */
    private void minFieldChange(){
        REDCapRunSettings runSettings = (REDCapRunSettings) RunSettings.getInstance();
        runSettings.updateItemMinValue(key, itemId, minTextField.getText());
    }

    /**
     * update the stored Max value
     */
    private void maxFieldChange(){
        REDCapRunSettings runSettings = (REDCapRunSettings) RunSettings.getInstance();
        runSettings.updateItemMaxValue(key, itemId, maxTextField.getText());
    }

    /**
     * Check whether the min/max fields must be enabled
     */
    private boolean mustEnableMinMax(){
        String selectedItem = textValidationComboBox.getSelectionModel().getSelectedItem();
        if(selectedItem==null){
            return false;
        }
        return selectedItem.equalsIgnoreCase("number") || selectedItem.equalsIgnoreCase("integer");
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

    /**
     * clear the min and max field
     */
    private void clearMinMax(){
        maxTextField.setText("");
        minTextField.setText("");
    }

    /**
     * disable the min and max field
     */
    private void setDisableMinMax(boolean disable){
        minTextField.setDisable(disable);
        maxTextField.setDisable(disable);
    }
}
