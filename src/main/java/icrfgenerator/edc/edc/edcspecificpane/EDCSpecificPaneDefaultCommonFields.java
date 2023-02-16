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

import icrfgenerator.codebook.CodebookItem;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.types.OperatorType;
import icrfgenerator.utils.StringUtils;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * Adds the commonly used fields to the pane. Currently: min, max, units, required
 * The SpecificPanes should extend this unless they don't use these fields for some reason.
 */
abstract class EDCSpecificPaneDefaultCommonFields extends EDCSpecificPaneDefault {
    private static final Logger logger = LogManager.getLogger(EDCSpecificPaneDefaultCommonFields.class.getName());

    private CheckBox requiredCheckBox;
    private TextField unitsTextField;
    private TextField minTextField;
    private TextField maxTextField;

    private ComboBox<OperatorType> minCheckField;
    private ComboBox<OperatorType> maxCheckField;

    EDCSpecificPaneDefaultCommonFields(){
    }

    /**
     * setup the edc-specific top-pane
     */
    void setupTopPaneCommonFields(GridPane gridPane, int rowNum){
        gridPane.add(new Label(I18N.getLanguageText("commonFieldsRequired")), 0, ++rowNum);
        requiredCheckBox = new CheckBox();
        gridPane.add(requiredCheckBox, 1, rowNum);

        gridPane.add(new Label(I18N.getLanguageText("commonFieldsValueMustBe")), 0,++rowNum);
        minCheckField = new ComboBox<>(FXCollections.observableList(OperatorType.getGTTypes()));
        minCheckField.setMaxWidth(75);
        gridPane.add(minCheckField, 1, rowNum);

        minTextField = new TextField();
        minTextField.setMaxWidth(75);
        gridPane.add(minTextField, 2, rowNum);

        gridPane.add( new Label(I18N.getLanguageText("commonFieldsValueMustBe")), 0,++rowNum);
        maxCheckField = new ComboBox<>(FXCollections.observableList(OperatorType.getLTTypes()));
        maxCheckField.setMaxWidth(75);
        gridPane.add(maxCheckField, 1, rowNum);

        maxTextField = new TextField();
        maxTextField.setMaxWidth(75);
        gridPane.add(maxTextField, 2, rowNum);

        gridPane.add(new Label(I18N.getLanguageText("commonFieldsUnits")), 0, ++rowNum);
        unitsTextField = new TextField();
        unitsTextField.setMaxWidth(75);
        gridPane.add(unitsTextField, 1, rowNum);

        setMinMaxUnitsDefaults();
    }

    /**
     * add the listeners
     */
    void addListenersCommonFields(){
        requiredCheckBox.setOnAction(e->requiredFieldChange());
        minTextField.textProperty().addListener(e->minFieldChange());
        minCheckField.setOnAction(e->minCheckFieldChange());
        maxTextField.textProperty().addListener(e->maxFieldChange());
        maxCheckField.setOnAction(e->maxCheckFieldChange());
        unitsTextField.textProperty().addListener(e->unitsFieldChange());
    }

    /**
     * set the default values
     */
    private void setMinMaxUnitsDefaults() {
        maxTextField.setText(getDotCorrectedMinMax(codebookItem, codebookItem.getMax()));
        maxCheckField.getSelectionModel().select(codebookItem.getMaxCheckOperator());
        minTextField.setText(getDotCorrectedMinMax(codebookItem, codebookItem.getMin()));
        minCheckField.getSelectionModel().select(codebookItem.getMinCheckOperator());
        unitsTextField.setText(codebookItem.getUnits());
    }

    /**
     * load the values that are stored for the standard fields
     */
    void loadStoredValuesCommonFieldsValues(){
        RunSettings runSettings = RunSettings.getInstance();
        requiredCheckBox.setSelected(runSettings.getSelectedItemRequiredValue(key, itemId));
        minTextField.setText(runSettings.getSelectedItemMinValue(key, itemId));
        minCheckField.getSelectionModel().select(runSettings.getSelectedItemMinCheckOperator(key, itemId));
        maxTextField.setText(runSettings.getSelectedItemMaxValue(key, itemId));
        maxCheckField.getSelectionModel().select(runSettings.getSelectedItemMaxCheckOperator(key, itemId));
        unitsTextField.setText(runSettings.getSelectedItemUnitsValue(key, itemId));
    }

    /**
     * disable fields
     */
    void disableFieldsCommonFields(){
        setDisableRequired(true);
        setDisableMinMaxUnits(true);
        codelistPane.disableFields();
    }

    /**
     * enable fields
     */
    void enableFieldsCommonFields(){
        setDisableRequired(false);
        checkMustEnableMinMaxUnits();
    }

    /**
     * store changes
     */
    void storeAllValuesCommonFields(){
        requiredFieldChange();
        minFieldChange();
        minCheckFieldChange();
        maxFieldChange();
        maxCheckFieldChange();
        unitsFieldChange();
    }

    /**
     * store all values when a group is selected
     */
    void storeAllValuesCommonFieldsGroupSelect(){
        RunSettings runSettings = RunSettings.getInstance();
        runSettings.updateItemRequiredValue(key, itemId, false);
        runSettings.updateItemMinValue(key, itemId, getDotCorrectedMinMax(codebookItem, codebookItem.getMin()));
        runSettings.updateItemMinCheckFieldValue(key, itemId, codebookItem.getMinCheckOperator());
        runSettings.updateItemMaxValue(key, itemId, getDotCorrectedMinMax(codebookItem, codebookItem.getMax()));
        runSettings.updateItemMaxCheckFieldValue(key, itemId, codebookItem.getMaxCheckOperator());
        runSettings.updateItemUnitsValue(key, itemId, codebookItem.getUnits());
    }

    /**
     * in some cases a check value has a dot, e.g. v<2.0
     * if the value's precision is set to 0, remove the ".0"
     * @param codebookItem codebookItem
     * @param value        value
     * @return dot-removed value (if necessary)
     */
    String getDotCorrectedMinMax(CodebookItem codebookItem, String value){
        if(codebookItem.getPrecision().equalsIgnoreCase("0")){
             value = StringUtils.removeDot(value);
        }
        return value;
    }

    /**
     * check whether the min / max / units fields must be enabled
     * this depends on EDC-specific fields, e.g. whether the datatype is real (enable) or string (disable)
     */
    void checkMustEnableMinMaxUnits(){
        if (checkMustEnableMinMaxUnits_local()) {
            setDisableMinMaxUnits(false);
        }
        else {
            clearMinMaxUnits();
            setDisableMinMaxUnits(true);
        }
    }

    /**
     * clear the min, max and units fields
     */
    private void clearMinMaxUnits(){
        maxTextField.setText("");
        maxCheckField.getSelectionModel().clearSelection();
        minTextField.setText("");
        minCheckField.getSelectionModel().clearSelection();
        unitsTextField.setText("");
    }

    /**
     * update the stored Required value
     */
    private void requiredFieldChange(){
        RunSettings.getInstance().updateItemRequiredValue(key, itemId, requiredCheckBox.isSelected());
    }

    /**
     * disable the min/max fields
     * @param disable true/false
     */
    private void setDisableMinMaxUnits(boolean disable){
        minTextField.setDisable(disable);
        minCheckField.setDisable(disable);
        maxTextField.setDisable(disable);
        maxCheckField.setDisable(disable);
        unitsTextField.setDisable(disable);
    }

    /**
     * enable / disable the required checkbox
     * @param disable true/false
     */
    private void setDisableRequired(boolean disable){
        requiredCheckBox.setDisable(disable);
    }

    /**
     * update the stored Min value
     */
    private void minFieldChange(){
        RunSettings.getInstance().updateItemMinValue(key, itemId, minTextField.getText());
    }

    /**
     * update the min operatorType
     * also, if the operatorType is set to blank, clear the min value
     */
    private void minCheckFieldChange(){
        OperatorType operatorType = minCheckField.getSelectionModel().getSelectedItem();
        if(operatorType==null) operatorType = OperatorType.NONE;

        RunSettings.getInstance().updateItemMinCheckFieldValue(key, itemId, operatorType);

        if(operatorType.equals(OperatorType.NONE)){
            minTextField.setText("");
        }
    }

    /**
     * update the stored Max value
     */
    private void maxFieldChange(){
        RunSettings.getInstance().updateItemMaxValue(key, itemId, maxTextField.getText());
    }

    /**
     * update the max operatorType
     * also, if the operatorType is set to blank, clear the max value
     */
    private void maxCheckFieldChange(){
        OperatorType operatorType = maxCheckField.getSelectionModel().getSelectedItem();

        if(operatorType==null) operatorType = OperatorType.NONE;

        RunSettings.getInstance().updateItemMaxCheckFieldValue(key, itemId, operatorType);
        if(operatorType.equals(OperatorType.NONE)){
            maxTextField.setText("");
        }
    }

    /**
     * update the stored width field
     */
    private void unitsFieldChange(){
        RunSettings.getInstance().updateItemUnitsValue(key, itemId, unitsTextField.getText());
    }

    /**
     * called by EDCSpecificPaneDefault when the pane is built
     * calls the EDC to add its listeners and then adds the listeners to the common fields
     */
    @Override
    final void addListenersEDC(){
        addListenersEDC_local();
        addListenersCommonFields();
    }

    /**
     * called by EDCSpecificPaneDefault in several locations
     * calls the EDC to load stored values and then loads values for the common fields
     */
    @Override
    final void loadStoredValuesEDC(){
        loadStoredValuesEDC_local();
        loadStoredValuesCommonFieldsValues();
    }

    /**
     * called by EDCSpecificPaneDefault when fields must be disabled
     * calls the EDC to disable its fields and then disables the common fields
     */
    @Override
    final void disableFieldsEDC(){
        disableFieldsEDC_local();
        disableFieldsCommonFields();
    }

    /**
     * called by EDCSpecificPaneDefault when fields must be enabled
     * calls the EDC to enable its fields and then enables the common fields
     */
    @Override
    final void enableFieldsEDC(){
        enableFieldsEDC_local();
        enableFieldsCommonFields();
    }

    /**
     * called by EDCSpecificPaneDefault when an item is selected
     * calls the EDC to store all values for its fields and then stores all values for the common fields
     */
    @Override
    final void storeAllValuesEDC(){
        storeAllValuesEDC_local();
        storeAllValuesCommonFields();
    }

    /**
     * called by EDCSpecificPaneDefault when an item is selected as part of a group select
     * calls the EDC to store all values for its fields and then stores all values for the common fields
     */
    @Override
    final void storeAllValuesEDCGroupSelect(){
        storeAllValuesEDCGroupSelect_local();
        storeAllValuesCommonFieldsGroupSelect();
    }

    abstract boolean checkMustEnableMinMaxUnits_local();
    abstract void addListenersEDC_local();
    abstract void loadStoredValuesEDC_local();
    abstract void disableFieldsEDC_local();
    abstract void enableFieldsEDC_local();
    abstract void storeAllValuesEDC_local();
    abstract void storeAllValuesEDCGroupSelect_local();

}
