package icrfgenerator.codebook.openehr;

import icrfgenerator.codebook.CodebookItemDefault;
import icrfgenerator.types.NodeType;

import java.util.HashMap;

/**
 * an item in an openEHR codebook
 */
public class CodebookItemOpenEHR extends CodebookItemDefault {
    CodebookItemOpenEHR(String id, String itemName, String description, NodeType nodeType){
        this(id, itemName, description, "", nodeType, "", "", "");
    }

    CodebookItemOpenEHR(String id, String itemName, String description, String dataType, NodeType nodeType, String code, String codeSystem, String codeDescription){
        super(id, itemName, description, dataType, nodeType, code, codeSystem, codeDescription, new HashMap<>());
    }

}
