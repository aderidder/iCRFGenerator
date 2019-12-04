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
import icrfgenerator.codebook.CodebookManager;
import icrfgenerator.codebook.CodebookStructureNode;
import icrfgenerator.edc.edc.edcspecificpane.EDCSpecificPane;
import icrfgenerator.edc.edc.edcspecificpane.StateTracker;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.utils.GUIUtils;
import icrfgenerator.utils.GeneralUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.controlsfx.control.CheckTreeView;

import java.util.HashSet;
import java.util.Set;

/**
 * class which represents a tab for a single codebook language
 */
class LanguageTab extends Tab{
    private String key;
    private Page3 page3;
    private StateTracker stateTracker = new StateTracker();
    private ObservableSet<CheckBoxTreeItem<CodebookItem>> searchMatches = FXCollections.observableSet(new HashSet<>());

    /**
     * constructor for a new language tab
     * @param codebook  name of the codebook
     * @param datasetId id of the codebook dataset
     * @param language  language of the codebook
     * @param page3     reference to the page
     */
    LanguageTab(String codebook, String datasetId, String language, Page3 page3){
        super(language);
        this.page3 = page3;
        key = GeneralUtils.getCodebookItemsMapKey(codebook, datasetId, language);
        setId(key);
        setClosable(false);
        setupLanguageTabContent();
    }

    /**
     * create the tab's content
     */
    private void setupLanguageTabContent(){
        // setup the content
        BorderPane borderPane = new BorderPane();

        // get the EDC-specific pane and use it as the center in our border pane
        EDCSpecificPane edcSpecificPane = RunSettings.getInstance().getEDC().generateRightSidePane();
        borderPane.setCenter(edcSpecificPane.getPane());

        // generate the leftside pane (basically the tree and search functionality) and add it as the left of the borderpane
        Pane leftSidePane = generateLeftSidePane(edcSpecificPane);
        borderPane.setLeft(leftSidePane);

        // make sure the borderpane takes all space available to it
        borderPane.setPrefWidth(page3.getPrefWidth());

        // add the borderpane to the language tab
        setContent(borderPane);
    }


    /**
     * create the content for the left side of the borderpane
     * this consists of the tree and the search functionality for the tree
     * @param edcSpecificPane link to the edc-specific pane
     * @return the left side pane
     */
    private Pane generateLeftSidePane(EDCSpecificPane edcSpecificPane){
        // create the item tree
        CheckTreeView<CodebookItem> checkTreeView = createItemTree(edcSpecificPane);

        TextField searchTextField = new TextField();
        // create the eventHandler which searches the tree based on the textfield's text content
        EventHandler<ActionEvent> actionEventEventHandler = e -> searchCheckTree((CheckBoxTreeItem<CodebookItem>) checkTreeView.getRoot(), searchTextField.getText());
        searchTextField.setOnAction(actionEventEventHandler);

        Button searchButton = new Button(I18N.getLanguageText("languageTabSearch"));
        searchButton.setOnAction(actionEventEventHandler);

        Hyperlink collapseTreeLink = new Hyperlink(I18N.getLanguageText("languageTabCollapse"));
        collapseTreeLink.setOnAction(e->collapseCheckTree(checkTreeView.getRoot()));

        Hyperlink expandTreeLink = new Hyperlink(I18N.getLanguageText("languageTabExpand"));
        expandTreeLink.setOnAction(e->expandCheckTree(checkTreeView.getRoot()));

        // add everything to a gridpane
        GridPane gridPane = GUIUtils.createGridPane();
        gridPane.setVgap(1);
        gridPane.setPadding(new Insets(10,0,0,0));
        gridPane.setPrefHeight(page3.getPrefHeight());
        gridPane.add(searchTextField, 0, 0, 2, 1);
        gridPane.add(searchButton, 2, 0);
        gridPane.add(expandTreeLink, 0, 1);
        gridPane.add(collapseTreeLink, 1, 1);
        gridPane.add(checkTreeView, 0,2,3,1);

        return gridPane;
    }

    /**
     * perform a search in the tree
     * @param rootNode   root of our tree
     * @param searchText text to search for
     */
    private void searchCheckTree(CheckBoxTreeItem<CodebookItem> rootNode, String searchText){
        // clear search
        searchMatches.clear();

        // collapse the tree to ensure only branches and leafs with hits will be visible
        collapseCheckTree(rootNode);

        // search only if there's text in the textfield
        if (!searchText.isEmpty()) {
            // search for matching nodes and put them in searchMatches
            // or add to searchMatches straight away?
            Set<CheckBoxTreeItem<CodebookItem>> matches = new HashSet<>();
            searchMatchingItems(rootNode, matches, searchText);
            searchMatches.addAll(matches);
        }
        else{
            rootNode.setExpanded(true);
        }
    }

    /**
     * recursive search of the tree
     * @param searchNode  the node we're currently analysing
     * @param matches     a temporary set in which we store all the matches found
     * @param searchValue the value we're looking for
     */
    private void searchMatchingItems(CheckBoxTreeItem<CodebookItem> searchNode, Set<CheckBoxTreeItem<CodebookItem>> matches, String searchValue) {
        // check whether the itemname of the node we're looking at contains the search string. This is case insensitive.
        if (searchNode.getValue().getItemName().toLowerCase().contains(searchValue.toLowerCase())) {
            // add the node and ensure it is visible in the tree (expand all its parents)
            matches.add(searchNode);
            expandRoute(searchNode);
        }
        // search all the children for matches
        for (TreeItem<CodebookItem> child : searchNode.getChildren()) {
            searchMatchingItems((CheckBoxTreeItem<CodebookItem>)child, matches, searchValue);
        }
    }

    /**
     * expand all nodes from this node to the root of the tree
     * @param searchNode current node
     */
    private void expandRoute(TreeItem<CodebookItem> searchNode){
        searchNode.setExpanded(true);
        if(searchNode.getParent()!=null){
            expandRoute(searchNode.getParent());
        }
    }

    /**
     * collapses all nodes in the tree
     * @param treeItem current node
     */
    private void collapseCheckTree(TreeItem<CodebookItem> treeItem){
        treeItem.setExpanded(false);
        treeItem.getChildren().forEach(e->collapseCheckTree(e));
    }

    /**
     * expands all nodes in the tree
     * @param treeItem current node
     */
    private void expandCheckTree(TreeItem<CodebookItem> treeItem){
        treeItem.setExpanded(true);
        treeItem.getChildren().forEach(e->expandCheckTree(e));
    }

    /**
     * create the item tree which will allow a user to select items
     * @param edcSpecificPane pane which will be used for the actions
     * @return the item tree
     */
    private CheckTreeView<CodebookItem> createItemTree(EDCSpecificPane edcSpecificPane){
        // get a graphical tree representation
        CheckTreeView<CodebookItem> checkTreeView = new SearchableTreeView(CodebookManager.getInstance().getCodebookTree(key));
        checkTreeView.setPrefHeight(page3.getPrefHeight());

        // add listeners
        addTreeItemListener((CheckBoxTreeItem<CodebookItem>)checkTreeView.getRoot(), checkTreeView, edcSpecificPane);
        addTreeListener(checkTreeView, edcSpecificPane);
        return checkTreeView;
    }

    /**
     * listener when highlighting an item in the selection tree
     * @param checkTreeView the tree
     * @param edcSpecificPane the right side pane
     */
    private void addTreeListener(CheckTreeView<CodebookItem> checkTreeView, EDCSpecificPane edcSpecificPane){
        checkTreeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if(newValue!=null) {
                        // if we're dealing with a leaf, adjust the right side pane to show the details of this item
                        if (newValue.isLeaf()) {
                            stateTracker.highlightAction(newValue.getValue(), key, edcSpecificPane, ((CheckBoxTreeItem) newValue).isSelected());
                        }
                        // otherwise we show some information, stating that a leaf node should be selected
                        else {
                            stateTracker.highlightNonLeafNode(newValue.getValue(), edcSpecificPane);
                        }
                    }
                });
    }

    /**
     * listener for when selecting an item in the selection tree
     * @param checkBoxTreeItem the checkbox item which can be selected
     * @param checkTreeView the tree
     * @param edcSpecificPane the right side pane
     */
    private void addTreeItemListener(CheckBoxTreeItem<CodebookItem> checkBoxTreeItem, CheckTreeView<CodebookItem> checkTreeView, EDCSpecificPane edcSpecificPane){
        // add the listener to the checkbox
        checkBoxTreeItem.selectedProperty().addListener((observable, oldValue, newValue) -> {

            // check whether we're in a leaf
            if (checkBoxTreeItem.isLeaf()) {
                // update the right side pane for selecting / deselecting an item
                if (newValue) {
                    stateTracker.selectAction(checkBoxTreeItem.getValue(), key, edcSpecificPane);
                } else {
                    stateTracker.deSelectAction(checkBoxTreeItem.getValue(), key, edcSpecificPane);
                }
            }

            // when selecting a checkbox, first highlight the row in the tree to prevent confusion
            checkTreeView.getSelectionModel().select(checkBoxTreeItem);

            // check whether the user is allowed to proceed to to the next page
            page3.checkMayProceed();
        });

        // get the children and add listeners to them as well
        for(TreeItem<CodebookItem> child:checkBoxTreeItem.getChildren()){
            addTreeItemListener((CheckBoxTreeItem<CodebookItem>)child, checkTreeView, edcSpecificPane);
        }
    }



    /**
     * inner class for searchable tree representation
     */
    final class SearchableTreeView extends CheckTreeView<CodebookItem>{

        /**
         * constructor for the searchable tree
         * @param codebookStructureRootNode the root of the codebookstructure
         */
        SearchableTreeView(CodebookStructureNode codebookStructureRootNode){
            // create the tree view
            super(new CheckBoxTreeItem<>(codebookStructureRootNode.getValue()));
            setup(codebookStructureRootNode);
        }

        private void setup(CodebookStructureNode codebookStructureRootNode){
            // get the root node which we just created in the constructor
            CheckBoxTreeItem<CodebookItem> rootNode = (CheckBoxTreeItem<CodebookItem>) this.getRoot();
            // call recursive function which will add nodes to the checkbokx tree
            generateGUITree(codebookStructureRootNode, rootNode);
            // expand the root node
            rootNode.setExpanded(true);
            // set the cell factory of our view to the SearchHighlightingTreeCell to allow for our
            // custom styling for searching items
            setCellFactory(tv -> new SearchHighlightingTreeCell());
        }

        /**
         * recursive tree construction based on the codebook tree structure nodes
         * this function basically creates a checkboxtree representation of the codebookstructure tree
         * @param codebookStructureNode current tree structure node
         * @param checkBoxTreeItem      current checkbox tree node
         */
        private void generateGUITree(CodebookStructureNode codebookStructureNode, CheckBoxTreeItem<CodebookItem> checkBoxTreeItem){
            // for all children of the current node
            for(CodebookStructureNode childCodebookStructureNode:codebookStructureNode.getChildren()){
                // create a new CheckBoxTreeItem with a CodebookItem
                CheckBoxTreeItem<CodebookItem> childCheckBoxTreeItem = new CheckBoxTreeItem<>(childCodebookStructureNode.getValue());
                // add the children to our current checkBoxTreeIten
                checkBoxTreeItem.getChildren().add(childCheckBoxTreeItem);
                // recursive call
                generateGUITree(childCodebookStructureNode, childCheckBoxTreeItem);
            }
        }

        /**
         *  customised checkbox tree cells that allow for highlighting
         *  based on https://stackoverflow.com/questions/34914058/javafx-how-to-highlight-certain-items-in-a-treeview
         *  due to it being an inner class instead of a static nested class , we can directly access the searchMatches
         *  defined in the top class
         */
        final class SearchHighlightingTreeCell extends CheckBoxTreeCell<CodebookItem> {

            // keep reference to binding to prevent premature garbage collection:
            private BooleanBinding booleanBinding;

            SearchHighlightingTreeCell() {
                // define a specific style for a different state
                PseudoClass searchMatch = PseudoClass.getPseudoClass("search-match");

                // initialize binding. Evaluates to true if searchMatchesObservableSet contains the current treeItem.
                // treeItemProperty changes when e.g. the nodes are collapsed / expanded
                // searchMatches changes when we do the search (since items are added/removed from the list)
                // using only the treeItemProperty would probably suffice, since we're collapsing the tree and
                // opening the relevant nodes when we search
                booleanBinding = Bindings.createBooleanBinding(() ->
                                searchMatches.contains(getTreeItem()), // this is computed
                        treeItemProperty(), // when this changes
                        searchMatches // or when this changes
                );

                // add a listener to the booleanBinding to observe the changes
                // the listener updates pseudoclass
                booleanBinding.addListener((observable, oldValue, newValue) ->
                        pseudoClassStateChanged(searchMatch, newValue)
                );
            }
        }
    }
}
