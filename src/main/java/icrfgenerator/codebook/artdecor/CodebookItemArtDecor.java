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

package icrfgenerator.codebook.artdecor;

import icrfgenerator.codebook.CodebookItemDefault;
import icrfgenerator.types.NodeType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * an item in an ART-DECOR codebook
 */
public class CodebookItemArtDecor extends CodebookItemDefault {
    private static final Logger logger = LogManager.getLogger(CodebookItemArtDecor.class.getName());

    // helper-list for generating missing codelist values
    private final List<List<String>> toGenerateCodeListItemList = new ArrayList<>();

    public CodebookItemArtDecor(String id, String itemName, String description, NodeType nodeType){
        super(id, itemName, description, "", nodeType, "", "", "", new HashMap<>());
    }

    CodebookItemArtDecor(String id, String itemName, String description, String dataType, NodeType nodeType, Map<String, String> artDecorPropertiesMap){
        super(id, itemName, description, dataType, nodeType, "", "", "", artDecorPropertiesMap);
    }

    CodebookItemArtDecor(String id, String itemName, String description, String dataType, NodeType nodeType, String code, String codeSystem, String codeDescription, Map<String, String> artDecorPropertiesMap){
        super(id, itemName, description, dataType, nodeType, code, codeSystem, codeDescription, artDecorPropertiesMap);
    }

    /**
     * if there is a problems with a codelist, don't use it and give an error message for it
     */
    void setBrokenCodeList(){
        logger.log(Level.ERROR, "{} has codelist issues... skipping the codelist", itemName);
        codelistItemList.clear();
        dataType="string";
    }

    /**
     * Not all codelist items have a code in art-decor. This complicates things, as I was using the code
     * as my id and also an EDC needs a code. So when we encounter such an item, we store it in a separate list
     * and later on we generate codes for these items
     * We can't generate a code straight away, as if the codebook were to have 1, "", 2 we would end up with two 2's.
     * @param values the textvalue of the codelist item
     */
    void addToGenerateCodeListItem(List<String> values){
        toGenerateCodeListItemList.add(values);
    }

    /**
     * generate codelist items for the codelist items that do not have a code
     * In a older version I attempted to add to existing codes - literally maxInt+1. So e.g. if the codelist
     * contained "1", "5", I attempted to add "6" as the missing code.
     * However this turned out to be confusing, as in a list with SNOMED codes 2049294, 3049298, the missing
     * one became 3049299, which seemed to suggest the missing code was actually a SNOMED code.
     * Hence, I'm now creating a Code_nr, e.g. Code_1, so it's independent of the already existing codes.
     */
    void resolveToGenerateCodeListItems(){
        if(toGenerateCodeListItemList.size()>0) {
            for (int i = 0; i < toGenerateCodeListItemList.size(); i++) {
                String textvalue = toGenerateCodeListItemList.get(i).get(0);
                String displayName = toGenerateCodeListItemList.get(i).get(1);
                codelistItemList.add(new CodelistItem("Code_" + (i+1), textvalue, displayName, "Generated"));
            }
            codeDataType = "string";
        }
        toGenerateCodeListItemList.clear();
    }

}
