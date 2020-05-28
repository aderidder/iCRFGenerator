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

package icrfgenerator.gui.wizard.page3;


import icrfgenerator.codebook.CodebookItem;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.resourcemanagement.ResourceManager;
import icrfgenerator.utils.GUIUtils;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Map;
import java.util.Set;

/**
 * Popup showing custom ART-DECOR properties
 */
class PropertiesDialog extends Dialog {
    private int rowNum;
    private static final int prefWidth = 450;
    private static final int prefHeight = 400;

    // our properties dialog
    private static PropertiesDialog propertiesDialog = new PropertiesDialog();

    /**
     * hides the properties dialog
     */
    static void hideWindow(){
        propertiesDialog.hide();
    }

    /**
     * show the properties dialog
     */
    static void showWindow(){
        if(!propertiesDialog.isShowing()) {
            propertiesDialog.show();
        }
        propertiesDialog.toFront();

    }

    /**
     * update the contents of the properties dialog
     * @param codebookItem the codebookItem, whose custom properties will be shown
     */
    static void updateWindow(CodebookItem codebookItem){
        propertiesDialog.setContent(codebookItem);
    }


    /**
     * constructor
     */
    private PropertiesDialog(){
        buildDialog();
    }

    /**
     * Builds the dialog
     */
    private void buildDialog(){
        initModality(Modality.WINDOW_MODAL);
        setTitle("Custom properties");
        getDialogPane().setPrefSize(prefWidth, prefHeight);
        initStyle(StageStyle.UTILITY);
        getDialogPane().getStylesheets().add(ResourceManager.getResourceStyleSheet("style.css"));
        getDialogPane().getStyleClass().add("fillBackground");

        // add an ok button
        getDialogPane().getButtonTypes().addAll(ButtonType.OK);
    }

    /**
     * sets the dialog's content to the codebookItem's properties
     * @param codebookItem
     */
    private void setContent(CodebookItem codebookItem){
        Map<String, String> propertiesMap = codebookItem.getArtDecorPropertiesMap();
        // if the codebookItem either has a blank Id or no custom properties, set the contents to a text message
        if(codebookItem.getId().equalsIgnoreCase("") || propertiesMap.size() == 0){
            getDialogPane().setContent(new Label(I18N.getLanguageText("edcNoCustomProperties")));
        }
        else {
            buildGridPane(propertiesMap);
        }
    }

    /**
     * fills the gridpane, adding a row for each property
     * @param propertiesMap the map containing the property and its value
     */
    private void buildGridPane(Map<String, String> propertiesMap){
        rowNum=0;
        GridPane gridPane =  GUIUtils.createGridPane();
        // get the keys (property names)
        Set<String> set = propertiesMap.keySet();
        // sort the keys and add a row for each entry
        set.stream().sorted().forEach(t -> addRow(gridPane, t, propertiesMap.get(t), rowNum++));

        // create a scrollpane and set the content of the dialog
        ScrollPane scrollPane = new ScrollPane(gridPane);
        GridPane.setVgrow(gridPane, Priority.ALWAYS);
        scrollPane.getStyleClass().add("fillBackground");
        getDialogPane().setContent(scrollPane);
    }

    /**
     * adds a single row (property and value) to the gridpane
     * @param key    property name
     * @param value  property value
     * @param rowNum row number
     */
    private void addRow(GridPane gridPane, String key, String value, int rowNum){
        Label label = new Label(key);
        label.setPrefWidth(150);
        gridPane.add(label,0, rowNum);

        TextArea textArea = new TextArea(value);
        textArea.setPrefWidth(235);
        textArea.setPrefHeight(100);
        textArea.setEditable(false);
        gridPane.add(textArea,1, rowNum);
    }


    /**
     * move the dialog to the front
     */
    private void toFront(){
        ((Stage) getDialogPane().getScene().getWindow()).toFront();
    }
}

