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

package icrfgenerator.edc.edc.edcspecificpane.edccodelistpane;

import icrfgenerator.codebook.CodebookItem;
import icrfgenerator.codebook.CodebookManager;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.utils.GUIUtils;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

/**
 * the generally used codelist pane, which allows a user to select / deselect codelist items for an item
 */
public class CodelistPane extends GridPane {
    private String key;
    private String itemId;
    private CodebookItem codebookItem;
    private List<CheckBox> checkBoxesList = new ArrayList<>();

    /**
     * constructor
     * @param key codebook + datasetid + language
     * @param codebookItem item id
     */
    public CodelistPane(String key, CodebookItem codebookItem){
        this.key = key;
        this.itemId = codebookItem.getId();
        this.codebookItem = codebookItem;

        setHgap(20);
        setVgap(5);

        setupPane();
    }

    /**
     * enable the fields in the pane
     */
    public void enableFields(){
        getChildren().forEach(t->t.setDisable(false));
    }

    /**
     * disable the fields in the pane
     */
    public void disableFields(){
        getChildren().forEach(t->t.setDisable(true));
    }

    /**
     * select the codes that were previously selected
     */
    public void setSelectedFields(){
        RunSettings runSettings = RunSettings.getInstance();
        List<String> selectedCodes = runSettings.getSelectedItemSelectedTerminologyCodes(key, codebookItem.getId());
        // filter the checkboxes that are selected and set their selected property to true
        checkBoxesList.stream().filter(t->selectedCodes.contains(t.getText())).forEach(t->t.setSelected(true));
    }

    /**
     * deselect all codes
     */
    public void deselectAllCheckBoxes(){
        RunSettings runSettings = RunSettings.getInstance();
        checkBoxesList.forEach(t->t.setSelected(false));
        checkBoxesList.forEach(t->runSettings.removeSelectedItemTerminology(key, itemId, t.getText()));
    }

    /**
     * select all codes
     */
    public void selectAllCheckBoxes(){
        RunSettings runSettings = RunSettings.getInstance();
        checkBoxesList.forEach(t->t.setSelected(true));
        checkBoxesList.forEach(t->runSettings.addSelectedItemTerminology(key, itemId, t.getText()));
    }

    /**
     * add event handlers to all checkboxes
     */
    private void addEventHandlers(){
        checkBoxesList.forEach(t->addCodeCheckBoxEvent(key, codebookItem.getId(), t, t.getText()));
    }


    /**
     * creates the content of the center pane in our dialog
     */
    private void setupPane(){
        if(CodebookManager.getInstance().codebookItemHasCodeList(key, itemId)) {
            setContent();
            addEventHandlers();
            disableFields();
        }
    }

    /**
     * set the codelist pane's content
     */
    private void setContent(){
        // retrieve the codes, the values and the codesystems for this item
        List<String> codesList = codebookItem.getCodesList();
        List<String> valuesList = codebookItem.getValuesList();
        List<String> codeSystemList = codebookItem.getCodeSystemList();

        // add the headers
        addHeaderLabels();

        int rowNum = 3;
        // add rows with a Code - Value - CodeSystem
        for(int i=0; i<codesList.size(); i++){
            String code = codesList.get(i);
            String value = valuesList.get(i);

            // add a checkbox to allow a user to select this row
            CheckBox codeCheckBox = new CheckBox(code);
            codeCheckBox.setWrapText(true);
            codeCheckBox.setPrefWidth(125);
            checkBoxesList.add(codeCheckBox);
            add(codeCheckBox, 1, rowNum);

            // add a label for the codesystem
            add(GUIUtils.createWrappedLabel(codeSystemList.get(i), 125), 2, rowNum);

            // add a label for the code description
            add(GUIUtils.createWrappedLabel(value, 450), 3, rowNum);

            rowNum++;
        }
    }

    /**
     * add event which fires when the checkbox is selected / deselected
     * @param key codebook+version+language
     * @param itemId name of the item for which we're setting the codelist
     * @param codeCheckBox the checkbox to which we're adding the event
     * @param code the selected code
     */
    private static void addCodeCheckBoxEvent(String key, String itemId, CheckBox codeCheckBox, String code){
        RunSettings runSettings = RunSettings.getInstance();
        codeCheckBox.setOnAction(event -> {
            // if the checkbox is selected add the terminology
            if(codeCheckBox.isSelected()){
                runSettings.addSelectedItemTerminology(key, itemId, code);
            }
            // otherwise, remove the selected terminology
            else{
                runSettings.removeSelectedItemTerminology(key, itemId, code);
            }
        });
    }

    /**
     * adds the header labels
     */
    private void addHeaderLabels(){
        add(new Label("Code"), 1, 2);
        add(new Label("Codesystem"), 2, 2);
        add(new Label("Label"), 3, 2);
    }


}
