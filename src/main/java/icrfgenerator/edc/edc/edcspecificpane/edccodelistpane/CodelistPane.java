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
import icrfgenerator.settings.runsettings.RunSettings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.text.Text;


import java.util.*;

/**
 * the generally used codelist pane, which allows a user to select / deselect codelist items for an item
 * since it is expensive to create, especially if there are codelists with MANY items, once created, we keep the
 * listviews in a map
 * It works as follows:
 * - the codelistpane is a borderpane
 * - to the top we add the labels and to the center the customlistview with the options
 * - normally, interaction with this listview happens by a user clicking options.
 * - however, a codelist-selection can also change in the background, if the user selects a group-item
 * - in that case, the view is marked as Dirty, implying that if a user selects the item afterwards, its
 *   listview would be incorrect. The Dirty is thus used to update the listview
 */
public class CodelistPane extends BorderPane{

    // customlistview used by the codelistpane
    private CustomListView customListView;

    // track the dirtyviews and the listviews
    private static final Set<String> dirtyViewsList = new HashSet<>();
    private static final Map<String, CustomListView> listViewMap = new HashMap<>();

    /**
     * returns a codelistpane for usage in the frontend. If the codebookItem has a codelist a codelistpane
     * with a listview is returned; otherwise an empty pane is returned.
     * @param fullKey  codebook+datasetId+language
     * @param codebookItem codebookItem
     * @return the codelistpane
     */
    public static CodelistPane getCodelistPane(String fullKey, CodebookItem codebookItem){
        if(codebookItem.hasCodeList()) {
            String key = getUniqueKey(fullKey, codebookItem);
            if (!listViewMap.containsKey(key)) {
                listViewMap.put(key, new CustomListView(fullKey, codebookItem));
            }
            CodelistPane codelistPane = new CodelistPane(listViewMap.get(key), dirtyViewsList.contains(key));
            dirtyViewsList.remove(key);
            return codelistPane;
        }
        else{
            return new CodelistPane();
        }
    }

    /**
     * when a groupItem is selected, mark the underlying items as dirty
     * @param fullKey  codebook+datasetId+language
     * @param codebookItem codebookItem
     */
    public static void codelistGroupSelect(String fullKey, CodebookItem codebookItem){
        if(codebookItem.hasCodeList()) {
            String key = getUniqueKey(fullKey, codebookItem);
            dirtyViewsList.add(key);
            RunSettings runSettings = RunSettings.getInstance();
            runSettings.addSelectedItemTerminologies(fullKey, codebookItem.getId());
        }
    }

    /**
     * clear the list and the map
     */
    public static void reset(){
        dirtyViewsList.clear();
        listViewMap.clear();
    }

    /**
     * adds the itemId to the fullkey
     * @param fullKey      codebook+datasetId+language
     * @param codebookItem codebookItem
     * @return unique key
     */
    private static String getUniqueKey(String fullKey, CodebookItem codebookItem){
        return fullKey+"_"+codebookItem.getId();
    }

    /**
     * constructor for the CodelistPane
     */
    private CodelistPane(){
        setTop(addHeaderLabels());
    }

    /**
     * constructor for the CodelistPane
     * adds the customlistview to the pane and handles a dirty view
     * @param customListView the view with the selectable options
     * @param dirtyView      whether the selected options changed, without the view being aware of this
     */
    private CodelistPane(CustomListView customListView, boolean dirtyView) {
        this.customListView = customListView;
        setup(dirtyView);
    }

    /**
     * setup
     * @param dirtyView whether the selected options changed, without the view being aware of this
     */
    private void setup(boolean dirtyView){
        if(dirtyView){
            customListView.updateDirtyView();
        }

        setCenter(customListView);
        setTop(addHeaderLabels());
    }

    /**
     * enable the fields in the pane
     */
    public void enableFields() {
        if(customListView!=null) {
            customListView.enableFields();
        }
    }

    /**
     * disable the fields in the pane
     */
    public void disableFields() {
        if(customListView!=null) {
            customListView.disableFields();
        }
    }

    /**
     * deselect all codes
     */
    public void deselectAllCheckBoxes() {
        if (customListView != null) {
            customListView.deselectAllCheckBoxes();
        }
    }

    /**
     * select all codes
     */
    public void selectAllCheckBoxes() {
        if(customListView!=null) {
            customListView.selectAllCheckBoxes();
        }
    }

    /**
     * adds the header labels
     */
    private Node addHeaderLabels() {
        Text text1 = new Text("Code");
        text1.setWrappingWidth(100);
        Text text2 = new Text("Codesystem");
        text2.setWrappingWidth(100);
        Text text3 = new Text("Label");
        text3.setWrappingWidth(150);
        Text text4 = new Text("Description");
        text4.setWrappingWidth(400);
        HBox hBox = new HBox(text1, text2, text3, text4);
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(3,0,3,10));
        return hBox;
    }
}


