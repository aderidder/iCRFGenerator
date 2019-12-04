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

//    void highlightingOnly(String key, CodebookItem codebookItem);
    void highlightingSelecting(String key, CodebookItem codebookItem);
    void highlightingDeselecting(String key, CodebookItem codebookItem);
    void highlightingSelected(String key, CodebookItem stateCodebookItem);
    void highlightingDeselected(String key, CodebookItem stateCodebookItem);

//    void highlightedOnly(String key, CodebookItem codebookItem);
    void highlightedSelecting(String key, CodebookItem codebookItem);
    void highlightedDeselecting(String key, CodebookItem codebookItem);
//    void highlightedSelected(String key, CodebookItem stateCodebookItem);
//    void highlightedDeselected(String key, CodebookItem stateCodebookItem);

    /**
     * behaviour when a non-leaf node is highlighted
     */
    void setInfoPane();

}
