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
import icrfgenerator.edc.edc.edcspecificpane.edccodelistpane.CodelistPane;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.utils.GUIUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * Default implementation of the EDCSpecificPane
 * All EDCs are expected to extend this class
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
     * A user clicks on the checkbox of an item in a different tree item thereby selecting the item
     * This implies:
     * 1. highlight the item
     *  a. build the Pane
     * 2. select the item
     *  a. stores the values
     *  b. enable the items
     * @param key codebook + datasetId + language
     * @param codebookItem item for which to build the pane
     */
    @Override
    public final void highlightingSelecting(String key, CodebookItem codebookItem){
        logger.log(Level.DEBUG, "Highlighting and selecting an item");
        buildPane(key, codebookItem);
        storeAllValues();
        enableFields();
    }

    @Override
    /**
     * A user clicks on the checkbox of an item in a different tree item thereby deselecting the item
     * This implies:
     * 1. highlight the item
     *  a. build the Pane
     *  b. load the stored values
     * 2. deselect the item (stores the values)
     *  a. remove stored values
     *  b. disable the items
     * @param key codebook + datasetId + language
     * @param codebookItem item for which to build the pane
     */
    public final void highlightingDeselecting(String key, CodebookItem codebookItem){
        logger.log(Level.DEBUG, "Highlighting and deselecting an item");
        buildPane(key, codebookItem);
        loadStoredValuesEDC();
        removeStoredValues();
        disableFields();
    }

    /**
     * A user highlights an item that was previously selected
     * This implies:
     * 1. highlight the item
     *  a. build the Pane
     *  b. load the stored values
     *  c. enable the items
     * @param key codebook + datasetId + language
     * @param codebookItem item for which to build the pane
     */
    @Override
    public final void highlightingSelected(String key, CodebookItem codebookItem){
        logger.log(Level.DEBUG, "Highlighting a selected item");
        buildPane(key, codebookItem);
        loadStoredValuesEDC();
        enableFields();
    }

    /**
     * A user highlights an item that was previously not selected
     * This implies:
     * 1. highlight the item
     *  a. build the Pane
     *  b. disable the items
     * @param key codebook + datasetId + language
     * @param codebookItem item for which to build the pane
     */
    @Override
    public final void highlightingDeselected(String key, CodebookItem codebookItem){
        logger.log(Level.DEBUG, "Highlighting a deselected item");
        buildPane(key, codebookItem);
        disableFields();
    }

    /**
     * A user clicks on the checkbox of an already highlighted item, selecting the item
     * This implies:
     * 1. select the item
     *  a. store the values
     *  b. enable the items
     * @param key codebook + datasetId + language
     * @param codebookItem item for which to build the pane
     */
    @Override
    public final void highlightedSelecting(String key, CodebookItem codebookItem){
        logger.log(Level.DEBUG, "Already highlighted and select");
        storeAllValues();
        enableFields();
    }

    /**
     * A user clicks on the checkbox of an already highlighted item, deselecting the item
     * This implies:
     * 1. select the item
     *  a. remove the stored values
     *  b. disable the items
     * @param key codebook + datasetId + language
     * @param codebookItem item for which to build the pane
     */
    @Override
    public final void highlightedDeselecting(String key, CodebookItem codebookItem){
        logger.log(Level.DEBUG, "Already highlighted and deselect");
        removeStoredValues();
        disableFields();
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
     * called when the user highlights a non-leaf node
     */
    @Override
    public final void setInfoPane(){
        borderPane.setTop(null);
        borderPane.setCenter(new Label("Select a leaf node to set an item's properties"));
    }

    /**
     * build the content of the right-side pane
     * @param key codebook + datasetId + language
     * @param codebookItem item for which to build the pane
     */
    private void buildPane(String key, CodebookItem codebookItem){
        this.codebookItem = codebookItem;
        this.itemId = codebookItem.getId();
        this.key = key;

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
        Label descriptionLabel = GUIUtils.createWrappedLabel(I18N.getLanguageText("edcDescription")+" "+codebookItem.getDescription(), prefWidth);
        gridPane.add(descriptionLabel,0,++rowNum,colSpan,1);

        rowNum++;

        // create a label with the codesystem's name and the code of the item in this codesystem
        Label codeSystemLabel = new Label(I18N.getLanguageText("edcOntology")+" "+codebookItem.getCodeSystem() +" - "+codebookItem.getCode());
        gridPane.add(codeSystemLabel,0,++rowNum,colSpan,1);

        // create a label with de description of the code in the codesystem
        Label codeDescriptionLabel = GUIUtils.createWrappedLabel(I18N.getLanguageText("edcCodeDescription")+" "+codebookItem.getCodeDescription(), prefWidth);
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
     * store all the values for the item
     */
    private void storeAllValues(){
        RunSettings.getInstance().addSelectedItem(key, itemId);
        selectAllCodeListItems();
        storeAllValuesEDC();
    }

    /**
     * remove the values for the item
     */
    private void removeStoredValues(){
        deselectAllCodeListItems();
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
        }
        enableFieldsEDC();
        codelistPane.enableFields();
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
        codelistPane = new CodelistPane(key, codebookItem);
//        ScrollPane scrollPane = new ScrollPane(codelistPane);
//        scrollPane.setPadding(new Insets(5,0,15,0));
//        borderPane.setCenter(codelistPane.getView());
        borderPane.setCenter(codelistPane);

//        codelistPane = new CodelistPane(key, codebookItem, borderPane);

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
     * create the top pane which contains EDC-specific fields
     */
    abstract void setupTopPaneEDC(GridPane gridPane, int rowNum);

    /**
     * add listeners to the fields
     */
    abstract void addListenersEDC();

}
