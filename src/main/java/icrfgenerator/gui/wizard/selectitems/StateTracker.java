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

package icrfgenerator.gui.wizard.selectitems;

import icrfgenerator.codebook.CodebookItem;
import icrfgenerator.codebook.artdecor.CodebookItemArtDecor;
import icrfgenerator.types.NodeType;
import icrfgenerator.edc.edc.edcspecificpane.EDCSpecificPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is an attempt to make the states of the selection tree less confusing.
 * (De)Selecting an item: this means a user clicks on the select box
 * Highlighting an item: this means a user clicks on the row/item in the tree, but NOT on the select box
 * This means the following states can exist:
 * 1. Highlight a previously Selected item
 * 2. Highlight a previously Unselected item
 * 3. Select an already highlighted item
 * 4. Deselect an already highlighted item
 * 5. Select a not-yet highlighted item
 * 6. Deselect a not-yet highlighted item
 * In the case of numbers 5. and 6. the tree fires both a (de)select and a highlight event.
 */
class StateTracker {

    private static final Logger logger = LogManager.getLogger(StateTracker.class.getName());

    private CodebookItem triggerCodebookItem;
    private CodebookItem stateCodebookItem=new CodebookItemArtDecor("", "", "", NodeType.GROUPITEM);

    void setTriggerCodebookItem(CodebookItem codebookItem){
        this.triggerCodebookItem = codebookItem;
    }

    void showInfoGroup(CodebookItem codebookItem, String key, EDCSpecificPane edcSpecificPane){
        edcSpecificPane.showInfoGroup(key, codebookItem);
    }

    void showInfoLeaf(CodebookItem codebookItem, String key, EDCSpecificPane edcSpecificPane){
        edcSpecificPane.showInfoLeaf(key, codebookItem);
    }

    void showItem(CodebookItem codebookItem, String key, EDCSpecificPane edcSpecificPane){
        edcSpecificPane.showItem(key, codebookItem);
        stateCodebookItem = codebookItem;
    }

    void selectItem(CodebookItem codebookItem, String key, EDCSpecificPane edcSpecificPane){
        NodeType triggerItemNodeType = triggerCodebookItem.getNodeType();
        if(triggerItemNodeType.equals(NodeType.LEAFITEM)){
            edcSpecificPane.singleSelectItem(key, codebookItem);
        }
        else if(triggerItemNodeType.equals(NodeType.GROUPITEM)){
            edcSpecificPane.groupSelectItem(key, codebookItem);
        }
        else{
            // TBD do we need anything else? Probably not?
        }
    }

    void deselectItem(CodebookItem codebookItem, String key, EDCSpecificPane edcSpecificPane){
        NodeType triggerItemNodeType = triggerCodebookItem.getNodeType();
        if(triggerItemNodeType.equals(NodeType.LEAFITEM)){
            edcSpecificPane.singleDeselectItem(key, codebookItem);
        }
        else if(triggerItemNodeType.equals(NodeType.GROUPITEM)){
            edcSpecificPane.groupDeselectItem(key, codebookItem);
        }
        else{
            // TBD do we need anything else? Probably not?
        }
    }

    /**
     * returns the current state's codebookItem.
     * @return the current state's codebookItem
     */
    CodebookItem getStateCodebookItem(){
        return stateCodebookItem;
    }

}
