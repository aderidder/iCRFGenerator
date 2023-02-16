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

package icrfgenerator.codebook.shared;

import icrfgenerator.codebook.CodebookItem;

import java.util.ArrayList;
import java.util.List;

/**
 * a node in a tree structure
 * this used to store the codebook's tree structure
 */
public class CodebookStructureNode {
    private final List<CodebookStructureNode> children = new ArrayList<>();
    private final CodebookItem codebookItem;

    /**
     * create a new codebook node
     * @param codebookItem the codebookItem for which the node is created
     */
    public CodebookStructureNode(CodebookItem codebookItem){
        this.codebookItem = codebookItem;
    }

    /**
     * add a child to this node
     * @param codebookStructureNode the child
     */
    public void addChild(CodebookStructureNode codebookStructureNode){
        children.add(codebookStructureNode);
    }

    /**
     * returns a list of the children
     * @return a list of the children
     */
    public List<CodebookStructureNode> getChildren(){
        return children;
    }

    /**
     * returns this node's codebook item
     * @return this node's codebook item
     */
    public CodebookItem getValue(){
        return codebookItem;
    }

}

