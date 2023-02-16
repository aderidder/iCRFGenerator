package icrfgenerator.codebook;

import icrfgenerator.codebook.shared.CodebookStructureNode;

public interface Codebook {

    /**
     * returns a codebookItem
     * @param itemId id of the item
     * @return codebookItem
     */
    CodebookItem getCodebookItem(String itemId);

    /**
     * returns whether an item has a codelist
     * @param itemId id of the item
     * @return whether item has a codelist
     */
    boolean codebookItemHasCodeList(String itemId);

    /**
     * return's the item's name
     * @param itemId id of the item
     * @return the item's name
     */
    String getItemName(String itemId);

    /**
     * returns the text value for the code
     * @param itemId id of the item
     * @param code   code for which the value must be returned
     * @return the textvalue for the code
     */
    String getValueForOptionCode(String itemId, String code);

    /**
     * returns the codesystem for the code
     * @param itemId id of the item
     * @param code   code for which the codesystem must be returned
     * @return the codesystem for the code
     */
    String getCodesystemForOptionCode(String itemId, String code);

    /**
     * returns
     * @param itemId id of the item
     * @param code   code for which the display name must be returned
     * @return the display name
     */
    String getDescriptionForOptionCode(String itemId, String code);

    /**
     * returns the codesystem for an item
     * @param itemId id of the item
     * @return the codesystem for the item
     */
    String getCodeSystemForItem(String itemId);

    /**
     * returns the code for an item
     * @param itemId id of the item
     * @return code for the item
     */
    String getCodeForItem(String itemId);

    /**
     * returns the base node of the codebook's tree
     * @return the base node of the codebook's tree
     */
    CodebookStructureNode getCodebookTree();

}
