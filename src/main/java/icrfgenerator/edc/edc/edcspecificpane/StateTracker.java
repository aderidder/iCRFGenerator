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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is an attempt to make the states of the selection tree less confusing.
 * (De)Selecting an item: this means a user clicks on the select box
 * Highlighting an item: this means a user clicks on the row/item in the tree, but NOT on the select box
 *
 * This means the following states can exist:
 * 1. Highlight a previously Selected item
 * 2. Highlight a previously Unselected item
 * 3. Select an already highlighted item
 * 4. Deselect an already highlighted item
 * 5. Select a not-yet highlighted item
 * 6. Deselect a not-yet highlighted item
 *
 * In the case of numbers 5. and 6. the tree fires both a (de)select and a highlight event.
 */
public class StateTracker {

    private static final Logger logger = LogManager.getLogger(StateTracker.class.getName());

    private CodebookItem stateCodebookItem=new CodebookItem("","","","");
    private HighlightAction highlightAction = HighlightAction.NOACTION;
    private SelectAction selectAction = SelectAction.NOACTION;

    private enum SelectAction {
        SELECTING,
        DESELECTING,
        SELECTED,
        NOTSELECTED,
        NOACTION
    }

    private enum HighlightAction {
        NOACTION,
        HIGHLIGHTING
    }

    /**
     * Action that is performed when a non-leaf item in the tree is highlighted
     * @param codebookItem     the item that is highlighted
     * @param edcSpecificPane the right-side pane on which the action should take place
     */
    public void highlightNonLeafNode(CodebookItem codebookItem, EDCSpecificPane edcSpecificPane){
        stateCodebookItem = codebookItem;
        edcSpecificPane.setInfoPane();
    }

    /**
     * Action that is performed when a leaf item in the tree is highlighted
     * @param codebookItem     the item that is highlighted
     * @param key              codebook + datasetId + language
     * @param edcSpecificPane the right-side pane on which the action should take place
     * @param isSelected       indicator whether the item's checkbox is selected or not
     */
    public void highlightAction(CodebookItem codebookItem, String key, EDCSpecificPane edcSpecificPane, boolean isSelected){
        highlightAction = HighlightAction.HIGHLIGHTING;

        // if there is a select action, the user (de)selected an item in a not-yet highlighted row (5. or 6.)
        // if the select action is NOACTION, the user is only highlighting a row, which can either be a selected
        // item or a notselected item. (1. or 2.)
        if(selectAction==SelectAction.NOACTION){
            // make distinction between whether the highlighted item is already selected or not
            if(isSelected){
                // 1. Highlight a previously selected item
                selectAction = SelectAction.SELECTED;
            }
            else{
                // 2. Highlight a previously unselected item
                selectAction = SelectAction.NOTSELECTED;
            }
            stateCodebookItem = codebookItem;
        }

        execute(key, edcSpecificPane);
    }

    /**
     * Action that is performed when an item in the tree is selected (the user clicks on the checkbox)
     * @param codebookItem     the item that is selected
     * @param key              codebook + datasetId + language
     * @param edcSpecificPane the right-side pane on which the action should take place
     */
    public void selectAction(CodebookItem codebookItem, String key, EDCSpecificPane edcSpecificPane){
        selectAction = SelectAction.SELECTING;

        // if the item did not change, it's already highlighted.
        // 3. Selecting an already highlighted item
        if(codebookItem.getId().equalsIgnoreCase(stateCodebookItem.getId())){
            execute(key, edcSpecificPane);
        }
        else{
            // the item is not yet highlighted since it's a newly selected item.
            // the highlightAction will take care of this.
            // 5. Selecting a not-yet highlighted item
            stateCodebookItem = codebookItem;
        }
    }

    /**
     * Action that is performed when an item in the tree is deselected (the user clicks on the checkbox)
     * @param codebookItem     the item that is deselected
     * @param key              codebook + datasetId + language
     * @param edcSpecificPane the right-side pane on which the action should take place
     */
    public void deSelectAction(CodebookItem codebookItem, String key, EDCSpecificPane edcSpecificPane){
        selectAction = SelectAction.DESELECTING;

        // if the item did not change, it's already highlighted.
        // 4. Deselecting an already highlighted item
        if(codebookItem.getId().equalsIgnoreCase(stateCodebookItem.getId())){
            execute(key, edcSpecificPane);
        }
        else{
            // the item is not yet highlighted since it's a newly selected item.
            // the highlightAction will take care of the actual execution
            // 6. Deselecting a not-yet highlighted item
            stateCodebookItem = codebookItem;
        }
    }

    /**
     * Updates the right-side pane based on the highlight and select actions
     * @param key              codebook + datasetId + language
     * @param edcSpecificPane the right-side pane on which the action should take place
     */
    private void execute(String key, EDCSpecificPane edcSpecificPane){
        switch (highlightAction){
            case HIGHLIGHTING:
                switch (selectAction){
                    case SELECTING:
                        edcSpecificPane.highlightingSelecting(key, stateCodebookItem);
                        break;
                    case DESELECTING:
                        edcSpecificPane.highlightingDeselecting(key, stateCodebookItem);
                        break;
                    case SELECTED:
                        edcSpecificPane.highlightingSelected(key, stateCodebookItem);
                        break;
                    case NOTSELECTED:
                        edcSpecificPane.highlightingDeselected(key, stateCodebookItem);
                        break;
                    // highlightingOnly
                    case NOACTION:
                        printUnexpectedCombination();
                        break;
                }
                break;
            // select / deselect the item from an already highlighted treeitem
            case NOACTION:
                switch (selectAction){
                    case SELECTING:
                        edcSpecificPane.highlightedSelecting(key, stateCodebookItem);
                        break;
                    case DESELECTING:
                        edcSpecificPane.highlightedDeselecting(key, stateCodebookItem);
                        break;
                    // highlightedOnly, highlightedDeselected, highlightedSelected
                    case NOACTION:
                    case NOTSELECTED:
                    case SELECTED:
                        printUnexpectedCombination();
                        break;
                }
                break;
        }

        // reset
        highlightAction = HighlightAction.NOACTION;
        selectAction = SelectAction.NOACTION;
    }

    /**
     * print an error message if an unexpected combination occurs.
     * this'll provide us with some information which we can use to debug
     */
    private void printUnexpectedCombination(){
        logger.log(Level.ERROR, "Unexpected combination for {}. SelectAction = {}, HighlightAction = {}", stateCodebookItem.getItemName(), selectAction, highlightAction);
    }
}
