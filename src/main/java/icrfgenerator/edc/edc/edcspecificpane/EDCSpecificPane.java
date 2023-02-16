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
import javafx.scene.layout.Pane;

public interface EDCSpecificPane {

    /**
     * returns an EDC specific right-side pane
     * @return an EDC specific right-side pane
     */
    Pane getPane();

    /**
     * If the item is a group item, set the borderpane to show a message
     * @param key codebook + datasetId + language
     * @param codebookItem item for which to build the pane
     */
    void showInfoGroup(String key, CodebookItem codebookItem);

    /**
     * If the item is an info leaf (no real content), set the borderpane to show a message
     * @param key codebook + datasetId + language
     * @param codebookItem item for which to build the pane
     */
    void showInfoLeaf(String key, CodebookItem codebookItem);

    /**
     * Show the item with either enabled fields and values or disabled fields
     * @param key codebook + datasetId + language
     * @param codebookItem item for which to build the pane
     */
    void showItem(String key, CodebookItem codebookItem);

    /**
     * Select the item
     * @param key codebook + datasetId + language
     * @param codebookItem item selected
     */
    void singleSelectItem(String key, CodebookItem codebookItem);

    /**
     * deselect a single item
     * @param key codebook + datasetId + language
     * @param codebookItem item deselected
     */
    void singleDeselectItem(String key, CodebookItem codebookItem);

    /**
     * select an item due to a group-select
     * @param key codebook + datasetId + language
     * @param codebookItem item selected
     */
    void groupSelectItem(String key, CodebookItem codebookItem);

    /**
     * deselect an item due to a group-select
     * @param key codebook + datasetId + language
     * @param codebookItem item deselected
     */
    void groupDeselectItem(String key, CodebookItem codebookItem);


}
