package icrfgenerator.codebook;

import icrfgenerator.codebook.artdecor.CodebookItemArtDecor;
import icrfgenerator.codebook.shared.CodebookStructureNode;
import icrfgenerator.types.NodeType;
import icrfgenerator.settings.GlobalSettings;

import java.util.HashMap;
import java.util.Map;

/**
 * Codebook defaults extended by both openEHR and ART-DECOR codebooks
 */
abstract public class CodebookDefault implements Codebook{
    protected final Map<String, CodebookItem> codebookItemMap = new HashMap<>();

    protected String codebookName;
    protected String extendedLanguage;
    protected String datasetId;
    protected CodebookStructureNode root;

    protected boolean groupIsAnItem;

    protected CodebookDefault(String datasetId, String extendedLanguage, String codebookName){
        this.codebookName = codebookName;
        this.extendedLanguage = extendedLanguage;
        this.datasetId = datasetId;

        // create top node for keeping track of the tree structure of the codebook
        root =  new CodebookStructureNode(new CodebookItemArtDecor("-1", codebookName, codebookName, NodeType.GROUPITEM));

        // retrieve whether for this codebook an non-leaf node is itself an actual item
        groupIsAnItem = GlobalSettings.groupIsAnItem(codebookName);
    }

    @Override
    public CodebookItem getCodebookItem(String itemId){
        return codebookItemMap.get(itemId);
    }

    @Override
    public boolean codebookItemHasCodeList(String itemId){
        return codebookItemMap.get(itemId).hasCodeList();
    }

    @Override
    public String getItemName(String itemId){
        return codebookItemMap.get(itemId).getItemName();
    }

    @Override
    public String getValueForOptionCode(String itemId, String code){
        return codebookItemMap.get(itemId).getValueForOptionCode(code);
    }

    @Override
    public String getCodesystemForOptionCode(String itemId, String code){
        return codebookItemMap.get(itemId).getCodesystemForOptionCode(code);
    }

    @Override
    public String getDescriptionForOptionCode(String itemId, String code){
        return codebookItemMap.get(itemId).getDescriptionForOptionCode(code);
    }

    @Override
    public String getCodeSystemForItem(String itemId){
        return codebookItemMap.get(itemId).getCodeSystemForItem();
    }

    @Override
    public String getCodeForItem(String itemId){
        return codebookItemMap.get(itemId).getCodeForItem();
    }

    @Override
    public CodebookStructureNode getCodebookTree() {
        return root;
    }

    /**
     * addChild an item to the codebook and keep track of the codebook structure
     * @param codebookItem the item to addChild
     * @param parentNode the parent structure node
     * @return a new structure node which will serve as the new parent
     */
    protected CodebookStructureNode addItem(CodebookItem codebookItem, CodebookStructureNode parentNode){
        // addChild the item to the map
        codebookItemMap.put(codebookItem.getId(), codebookItem);
        // create a new codebookstructure node and addChild at as a child to the parentnode
        CodebookStructureNode codebookStructureNode = new CodebookStructureNode(codebookItem);
        parentNode.addChild(codebookStructureNode);
        return codebookStructureNode;
    }
}

