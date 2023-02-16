package icrfgenerator.codebook;

import icrfgenerator.types.NodeType;
import icrfgenerator.types.OperatorType;

import java.util.List;
import java.util.Map;

public interface CodebookItem {
    NodeType getNodeType();

    /**
     * returns the map with the custom ART-DECOR properties (property name - value)
     * @return tha map with the custom ART-DECOR properties
     */
    Map<String, String> getCustomPropertiesMap();

    /**
     * get the item's id
     * @return the item's id
     */
    String getId();

    /**
     * returns the item's name
     * @return the item's name
     */
    String getItemName();

    /**
     * returns the item's data type
     * @return the item's data type
     */
    String getItemDataType();

    /**
     * if the item is a codelist, returns whether this codelist has integer or string requirements for its codes
     * @return the item's
     */
    String getItemCodeDataType();


    /**
     * returns the codebook item's description
     * @return the codebook item's description
     */
    String getItemDescription();

    /**
     * returns the codebook item's codesystem
     * @return the codebook item's codesystem
     */
    String getCodeSystemForItem();

    /**
     * returns the codebook item's code
     * @return the codebook item's code
     */
    String getCodeForItem();

    /**
     * returns the codebook item's code description
     * @return the codebook item's code description
     */
    String getItemCodeDescription();

    /**
     * returns whether the codebook item has a codelist
     * @return whether the codebook item has a codelist
     */
    boolean hasCodeList();

    /**
     * returns a list representation of all the codes in the item's codelist
     * @return a list representation of all the codes in the item's codelist
     */
    List<String> getCodelistCodes();

    /**
     * returns a list representation of all the values in the item's codelist
     * @return a list representation of all the values in the item's codelist
     */
    List<String> getCodelistValues();

    /**
     * returns a list representation of the codesystems in the item's codelist
     * @return a list representation of the codesystems in the item's codelist
     */
    List<String> getCodelistCodeSystems();

    /**
     * returns a list representation of the descriptions in the item's codelist
     * @return a list representation of the descriptions in the item's codelist
     */
    List<String> getCodelistDescriptions();

    /**
     * get the text value for a code
     * @param code code
     * @return text value for the code
     */
    String getValueForOptionCode(String code);

    /**
     * get displayName for a code
     * @param code code
     * @return text value for the code
     */
    String getDescriptionForOptionCode(String code);

    /**
     * get the codesystem for a code
     * @param code code
     * @return codesystem for the code
     */
    String getCodesystemForOptionCode(String code);

    /**
     * add the parameter's codebookItem's codelist to this codebookItem's codelist
     * @param otherCodebookItem codebookItem whose codelist will be merged
     */
    void mergeCodebookItemCodeLists(CodebookItemDefault otherCodebookItem);

    /**
     * returns the units
     * @return the units
     */
    String getUnits();

    /**
     * returns the maximum value
     * @return the maximum value
     */
    String getMax();

    /**
     * returns the operator for the max check
     * @return the operator for the max check
     */
    OperatorType getMaxCheckOperator();

    /**
     * returns the minimum value
     * @return the minimum value
     */
    String getMin();

    /**
     * returns the operator for the min check
     * @return the operator for the min check
     */
    OperatorType getMinCheckOperator();

    /**
     * returns the decimal precision
     * @return the decimal precision
     */
    String getPrecision();

    /**
     * set the codebookItem's NodeType
     * @param nodeType the nodeType
     */
    void setNodeType(NodeType nodeType);
}
