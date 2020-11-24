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
import icrfgenerator.resourcemanagement.ResourceManager;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.utils.GUIUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * the generally used codelist pane, which allows a user to select / deselect codelist items for an item
 * Misschien aanpassen naar een javafx tableview? https://stackoverflow.com/questions/22582706/javafx-select-multiple-rows
 */

public class CodelistPane extends BorderPane{
    private ListView<ListData> listView;
    private String key;
    private String itemId;
    private CodebookItem codebookItem;

    // keep the listviews around to improve performance
    private static Map<String, ListView<ListData>> listViewMap = new HashMap<>();

    public static void reset(){
        listViewMap.clear();
    }

    /**
     * transforms codeList, valueList and codeSystemList entry into rows in an observableList for the ListView
     * @return observable list of the data
     */
    private ObservableList<ListData> transformDataToRows(CodebookItem codebookItem){
        ObservableList<ListData> observableList = FXCollections.observableArrayList();
        List<String> codesList = codebookItem.getCodesList();
        List<String> valuesList = codebookItem.getValuesList();
        List<String> codeSystemList = codebookItem.getCodeSystemList();

        for(int i=0; i<codesList.size(); i++){
            // create a ListData object with the code, codesystem and value
            observableList.add(new ListData(codesList.get(i), codeSystemList.get(i), valuesList.get(i)));
        }
        return observableList;
    }


    /**
     * constructor
     *
     * @param key          codebook + datasetid + language
     * @param codebookItem item id
     */
    public CodelistPane(String key, CodebookItem codebookItem) {
        this.key = key;
        this.itemId = codebookItem.getId();
        this.codebookItem = codebookItem;

        if(!listViewMap.containsKey(itemId)) {
            setup();
            listViewMap.put(itemId, listView);
        }
        else{
            listView = listViewMap.get(itemId);
        }

        setCenter(listView);
        setTop(addHeaderLabels());
    }

    /**
     * setup
     */
    private void setup(){
        RunSettings runSettings = RunSettings.getInstance();

        // create a listview based for the codebookItem and add some styling
        listView = new ListView<>(transformDataToRows(codebookItem));
        listView.setPadding(new Insets(5,0,0,5));
        listView.getStylesheets().add(ResourceManager.getResourceStyleSheet("style3.css"));

        // set the selectionmodel to allow for the selection of multiple items
        MultipleSelectionModel<ListData> selectionModel = listView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);

        // set the cell factory
        listView.setCellFactory(e -> {
            // use a custom list cell to allow it to display our ListData
            ListCell<ListData> cell = new CustomListCell();

            // normally multiple selection requires the user to press ctrl. To improve the user
            // experience, we add some functionality which allows multiple selection simply based on clicking items
            // without the need to press ctrl.
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    // check whether the clicked cell is currently selected
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        // if it is, clicking it means deselecting it. Remove it from the selection model
                        // and from the runsettings
                        selectionModel.clearSelection(index);
                        runSettings.removeSelectedItemTerminology(key, itemId, cell.getItem().getCode());
                    } else {
                        // if it is not, clicking it means selecting it. Add it to the selection model
                        // and to the runsettings
                        selectionModel.select(index);
                        runSettings.addSelectedItemTerminology(key, itemId, cell.getItem().getCode());
                    }
                }
                // prevent the event from triggering other stuff
                event.consume();
            });
            return cell;
        });

        // set the center and top of the borderpane
//        setCenter(listView);
//        setTop(addHeaderLabels());
    }

    /**
     * enable the fields in the pane
     */
    public void enableFields() {
        listView.setDisable(false);
    }

    /**
     * disable the fields in the pane
     */
    public void disableFields() {
        listView.setDisable(true);
        listView.getSelectionModel().clearSelection();
    }

    /**
     * select the codes that were previously selected
     * Can probably be removed, since we're now storing a current version of the listview, which
     * is set when the codelistpane is initialised
     */
    public void setSelectedFields() {
//        listView = listViewMap.get(itemId);
//        setCenter(listView);

//        // to be safe, we'll add a loading animation for the list
//        setCenter(GUIUtils.createLoadingPane(""));
//
//        RunSettings runSettings = RunSettings.getInstance();
//        MultipleSelectionModel<ListData> selectionModel = listView.getSelectionModel();
//
//        // retrieve the codes that were selected previously
//        List<String> selectedCodes = runSettings.getSelectedItemSelectedTerminologyCodes(key, codebookItem.getId());
//
//        // get the listview's items, which may have to be colored
//        List<ListData> itemList = listView.getItems();
//
//        // create an int array with indices which need to be selected
//        int [] index = new int[selectedCodes.size()];
//        int cnt=0;
//        for(int i=0; i<itemList.size(); i++){
//            // if the item was selected, store the item's index in the index array
//            if(selectedCodes.contains(itemList.get(i).getCode())){
//                index[cnt] = i;
//                cnt++;
//            }
//        }
//
//        // start a background task to select the incides in the selectionmodel, which is quicker
//        // alternatively, we could maybe store the selection model or complete list view?
//        Task<Void> task = new Task<>() {
//            @Override
//            protected Void call() {
//                selectionModel.selectIndices(-1, index);
//                return null;
//            }
//        };
//        new Thread(task).start();
//
//        // when the task is completed, update the center pane
//        task.setOnSucceeded(event -> setCenter(listView));
    }


    /**
     * deselect all codes
     */
    public void deselectAllCheckBoxes() {
        RunSettings runSettings = RunSettings.getInstance();
        runSettings.removeSelectedItemTerminologies(key, itemId);
        listView.getSelectionModel().clearSelection();
    }

    /**
     * select all codes
     */
    public void selectAllCheckBoxes() {
        RunSettings runSettings = RunSettings.getInstance();
        listView.getItems().forEach(t->{
            runSettings.addSelectedItemTerminology(key, itemId, t.getCode());
        });
        listView.getSelectionModel().selectAll();
    }


    /**
     * adds the header labels
     */
    private Node addHeaderLabels() {
        Text text1 = new Text("Code");
        text1.setWrappingWidth(100);
        Text text2 = new Text("Codesystem");
        text2.setWrappingWidth(100);
        Text text3 = new Text("Description");
        text3.setWrappingWidth(550);
        HBox hBox = new HBox(text1,text2, text3);
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(3,0,3,10));
        return hBox;
    }


    // custom cell whose values come from a custom thing
    private class CustomListCell extends ListCell<ListData> {
        private HBox content;
        private Text code;
        private Text codeSystem;
        private Text description;

        CustomListCell() {
            super();
            code = new Text();
            code.setWrappingWidth(100);

            codeSystem = new Text();
            codeSystem.setWrappingWidth(100);

            description = new Text();
            description.setWrappingWidth(550);

            content = new HBox(code, codeSystem, description);
            content.setSpacing(10);
        }

        @Override
        protected void updateItem(ListData item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null && !empty) {
                code.setText(item.getCode());
                codeSystem.setText(item.getCodeSystem());
                description.setText(item.getDescription());

                setGraphic(content);
            } else {
                setGraphic(null);
            }
        }
    }


    private static class ListData {
        private String code;
        private String codeSystem;
        private String description;

        public String getCode() {
            return code;
        }

        public String getCodeSystem() {
            return codeSystem;
        }

        public String getDescription(){
            return description;
        }

        ListData(String code, String codeSystem, String description) {
            this.code = code;
            this.codeSystem = codeSystem;
            this.description = description;
        }

    }
}
