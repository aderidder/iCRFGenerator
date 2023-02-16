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
import icrfgenerator.types.NodeType;
import icrfgenerator.edc.edc.edcspecificpane.edccodelistpane.CodelistPane;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.utils.GUIUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * Default implementation of the EDCSpecificPane
 * All EDCs are expected to extend this or the EDCSpecificPaneDefaultStandardFields class
 */
abstract class EDCSpecificPaneDefault implements EDCSpecificPane {
    private static final Logger logger = LogManager.getLogger(EDCSpecificPaneDefault.class.getName());

    String itemId;
    String key;
    CodebookItem codebookItem;
    CodelistPane codelistPane;
    BorderPane borderPane = new BorderPane();

    private Button buttonSelectAll;
    private Button buttonSelectNone;


    EDCSpecificPaneDefault(){

    }

    /**
     * If the item is a group item, set the borderpane to show a message
     * @param key codebook + datasetId + language
     * @param codebookItem item for which to build the pane
     */
    @Override
    public void showInfoGroup(String key, CodebookItem codebookItem){
        borderPane.setTop(null);
        if(codebookItem.getItemDescription().equalsIgnoreCase("")){
            borderPane.setCenter(new Label("Select a leaf node to set an item's properties"));
        }
        else {
            borderPane.setCenter(new Label(codebookItem.getItemDescription()));
        }
    }

    /**
     * If the item is an info leaf (no real content), set the borderpane to show a message
     * @param key codebook + datasetId + language
     * @param codebookItem item for which to build the pane
     */
    @Override
    public void showInfoLeaf(String key, CodebookItem codebookItem){
        borderPane.setTop(null);
        borderPane.setCenter(new Label(codebookItem.getItemDescription()));
    }

    /**
     * Show the item with either enabled fields and values or disabled fields
     * @param key codebook + datasetId + language
     * @param codebookItem item for which to build the pane
     */
    @Override
    public void showItem(String key, CodebookItem codebookItem){
        buildPane(key, codebookItem);
        if(RunSettings.getInstance().itemIsSelected(key, codebookItem.getId())){
            loadStoredValuesEDC();
            enableFields();
        }
        else{
            disableFields();
        }
    }

    /**
     * Select the item
     * @param key codebook + datasetId + language
     * @param codebookItem item selected
     */
    @Override
    public void singleSelectItem(String key, CodebookItem codebookItem){
        RunSettings runSettings = RunSettings.getInstance();
        buildPane(key, codebookItem);
        // if item already stored, load its stored values
        if(runSettings.itemIsSelected(key, codebookItem.getId())) {
            loadStoredValuesEDC();
        }
        // else add it as selected, select/store all codelist items and store all other values
        else{
            runSettings.addSelectedItem(key, itemId);
            selectAllCodeListItems();
            storeAllValuesEDC();
        }
        enableFields();
    }

    /**
     * deselect a single item
     * @param key codebook + datasetId + language
     * @param codebookItem item deselected
     */
    @Override
    public void singleDeselectItem(String key, CodebookItem codebookItem){
        buildPane(key, codebookItem);
        loadStoredValuesEDC();
        removeStoredValues();
        disableFields();
    }

    /**
     * select an item due to a group-select
     * @param key codebook + datasetId + language
     * @param codebookItem item selected
     */
    @Override
    public void groupSelectItem(String key, CodebookItem codebookItem){
        RunSettings runSettings = RunSettings.getInstance();
        storeRefs(key, codebookItem);

        if(!runSettings.itemIsSelected(key, itemId) && codebookItem.getNodeType().equals(NodeType.LEAFITEM)) {
            runSettings.addSelectedItem(key, itemId);
            storeAllValuesEDCGroupSelect();
            if(codebookItem.hasCodeList()){
                CodelistPane.codelistGroupSelect(key, codebookItem);
            }
        }
    }

    /**
     * deselect an item due to a group-select
     * @param key codebook + datasetId + language
     * @param codebookItem item deselected
     */
    @Override
    public void groupDeselectItem(String key, CodebookItem codebookItem){
        this.key = key;
        this.itemId = codebookItem.getId();

        if(RunSettings.getInstance().itemIsSelected(key, itemId)) {
            removeStoredValues();
        }
    }

    /**
     * returns the right side pane
     * @return the right side pane
     */
    @Override
    public final Pane getPane(){
        return borderPane;
    }


    /**
     * store some stuff used throughout
     * @param key codebook + datasetId + language
     * @param codebookItem codebookItem
     */
    private void storeRefs(String key, CodebookItem codebookItem){
        this.codebookItem = codebookItem;
        this.itemId = codebookItem.getId();
        this.key = key;
    }

    /**
     * build the content of the right-side pane
     * @param key codebook + datasetId + language
     * @param codebookItem item for which to build the pane
     */
    private void buildPane(String key, CodebookItem codebookItem){
        storeRefs(key, codebookItem);

        setupTopPane();
        setupCenterPane();
        setupBottomPane();

        // add the listeners
        addListenersEDC();
    }

    /**
     * the standard part of the top pane, with the item name, description, ontology things
     */
    private void setupTopPane(){
        int rowNum = 0;
        int colSpan = 6;
        int prefWidth = 650;

        // create the gridpane with some padding on the left side
        GridPane gridPane = GUIUtils.createGridPane();
        gridPane.setPadding(new Insets(10,0,0,10));

        // create a label with the item name
        Label itemLabel = new Label(I18N.getLanguageText("edcItem")+" "+codebookItem.getItemName());
        gridPane.add(itemLabel,0,rowNum,colSpan,1);

        // create a label with the description of the item
        Label descriptionLabel = GUIUtils.createWrappedLabel(I18N.getLanguageText("edcDescription")+" "+codebookItem.getItemDescription(), prefWidth);
        gridPane.add(descriptionLabel,0,++rowNum,colSpan,1);

        rowNum++;

        // create a label with the codesystem's name and the code of the item in this codesystem
        Label codeSystemLabel = new Label(I18N.getLanguageText("edcOntology")+" "+codebookItem.getCodeSystemForItem() +" - "+codebookItem.getCodeForItem());
        gridPane.add(codeSystemLabel,0,++rowNum,colSpan,1);

        // create a label with de description of the code in the codesystem
        Label codeDescriptionLabel = GUIUtils.createWrappedLabel(I18N.getLanguageText("edcCodeDescription")+" "+codebookItem.getItemCodeDescription(), prefWidth);
        gridPane.add(codeDescriptionLabel,0,++rowNum,colSpan,1);

        // add a separator for visual purposes
        gridPane.add(new Separator(),0,++rowNum,colSpan,1);

        // add EDC-specific things to the gridpane
        setupTopPaneEDC(gridPane, rowNum);

        // add another separator beneath the EDC specific items
        int nrRows = gridPane.getRowCount();
        gridPane.add(new Separator(), 0, ++nrRows, colSpan, 1);

        // add the grid pane to the borderpane
        borderPane.setTop(gridPane);
    }

    /**
     * remove the values for the item
     */
    private void removeStoredValues(){
        if(codebookItem.hasCodeList()) {
            deselectAllCodeListItems();
        }
        RunSettings.getInstance().removeSelectedItem(key, itemId);
    }

    /**
     * general disable fields
     */
    private void disableFields(){
        buttonSelectAll.setDisable(true);
        buttonSelectNone.setDisable(true);
        disableFieldsEDC();
    }

    /**
     * general enable fields
     */
    private void enableFields(){
        if(codebookItem.hasCodeList()){
            buttonSelectAll.setDisable(false);
            buttonSelectNone.setDisable(false);
            codelistPane.enableFields();
        }
        enableFieldsEDC();
    }

    /**
     * deselect all codelist items
     */
    private void deselectAllCodeListItems(){
        codelistPane.deselectAllCheckBoxes();
    }

    /**
     * select all codelist items
     */
    private void selectAllCodeListItems(){
        codelistPane.selectAllCheckBoxes();
    }

    /**
     * setup the center pane, which contains the codelist pane
     */
    private void setupCenterPane(){
        codelistPane = CodelistPane.getCodelistPane(key, codebookItem);
        borderPane.setCenter(codelistPane);
    }

    /**
     * setup the bottom pane, which contains the select all / none buttons
     */
    private void setupBottomPane(){
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(0,0,0,10));
        hBox.setSpacing(5);

        // create the select all button
        buttonSelectAll = new Button(I18N.getLanguageText("edcSelectAll"));
        buttonSelectAll.setDisable(true);
        buttonSelectAll.setOnAction(event -> selectAllCodeListItems());

        // create the select none button
        buttonSelectNone = new Button(I18N.getLanguageText("edcSelectNone"));
        buttonSelectNone.setDisable(true);
        buttonSelectNone.setOnAction(event -> deselectAllCodeListItems());

        // add the buttons to the hbox
        hBox.getChildren().addAll(buttonSelectAll, buttonSelectNone);
        borderPane.setBottom(hBox);
    }


    /**
     * load the values that are currently stored
     */
    abstract void loadStoredValuesEDC();

    /**
     * enable fields in the right-side pane
     */
    abstract void enableFieldsEDC();

    /**
     * disable fields in the right-side pane
     */
    abstract void disableFieldsEDC();

    /**
     * store all the values
     */
    abstract void storeAllValuesEDC();

    /**
     * store all values due to a group-select
     */
    abstract void storeAllValuesEDCGroupSelect();

    /**
     * create the top pane which contains EDC-specific fields
     */
    abstract void setupTopPaneEDC(GridPane gridPane, int rowNum);

    /**
     * add listeners to the fields
     */
    abstract void addListenersEDC();

}
