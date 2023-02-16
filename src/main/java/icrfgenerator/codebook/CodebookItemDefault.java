package icrfgenerator.codebook;

import icrfgenerator.types.NodeType;
import icrfgenerator.types.OperatorType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * default variables for all codebook items
 */
abstract public class CodebookItemDefault implements CodebookItem{
    protected String id;
    protected String itemName;
    protected String dataType;
    protected String description;

    protected String min="";
    protected OperatorType minCheckOperator = OperatorType.NONE;
    protected String max="";
    protected OperatorType maxCheckOperator = OperatorType.NONE;
    protected String units="";
    protected String precision="0";

    protected String codeDataType = "integer";
    protected String codeSystem;
    protected String code;
    protected String codeDescription;

    protected NodeType nodeType;

    protected final Map<String, String> customPropertiesMap;
    protected final List<CodelistItem> codelistItemList = new ArrayList<>();

    public CodebookItemDefault(String id, String itemName, String description, String dataType, NodeType nodeType, String code, String codeSystem, String codeDescription, Map<String, String> customPropertiesMap){
        this.id = id;
        this.itemName = itemName;
        this.description = description;
        this.dataType = dataType;
        this.nodeType = nodeType;

        this.code = code;
        this.codeSystem = codeSystem;
        this.codeDescription = codeDescription;

        this.customPropertiesMap = customPropertiesMap;
    }

    /**
     * returns the nodeType LEAFITEM, LEAFINFOITEM, GROUPITEM
     * @return the nodeType
     */
    public NodeType getNodeType(){
        return nodeType;
    }

    /**
     * get the custom properties map
     * @return custom properties map
     */
    public Map<String, String> getCustomPropertiesMap(){
        return customPropertiesMap;
    }

    /**
     * get the item's id
     * @return the item's id
     */
    public String getId() {
        return id;
    }

    /**
     * returns the item's name
     * @return the item's name
     */
    public String getItemName(){
        return itemName;
    }

    /**
     * returns the item's data type
     * @return the item's data type
     */
    public String getItemDataType(){
        return dataType;
    }

    /**
     * DataType is defined by ART-DECOR's datatypes, since that's what we started with originally
     * These DataTypes are then converted to EDC-Speciic datatypes by the edcdefinitions package
     * So, to make sure this is compatible, new Codebook types (such as OpenEHR) must convert their
     * datatype to an ART-DECOR datatype.
     * @param dataType dataType for the item. Should be one of: code, integer, count, quantity, boolean, duration, date, string, identifier
     */
    public void setDataType(String dataType){
        this.dataType = dataType;
    }

    /**
     * if the item is a codelist, returns whether this codelist has integer or string requirements for its codes
     * @return the item's
     */
    public String getItemCodeDataType(){
        return codeDataType;
    }

    /**
     * returns the item's description
     * @return the  item's description
     */
    public String getItemDescription(){
        return description;
    }

    /**
     * returns the item's codesystem
     * @return the item's codesystem
     */
    public String getCodeSystemForItem(){
        return codeSystem;
    }

    /**
     * returns the item's code
     * @return the item's code
     */
    public String getCodeForItem(){
        return code;
    }

    /**
     * returns the item's code description
     * @return the item's code description
     */
    public String getItemCodeDescription(){
        return codeDescription;
    }

    /**
     * sets the item's description
     * @param description the new description
     */
    public void setDescription(String description){
        this.description = description;
    }

    /**
     * returns a list representation of all the codes in the codelistItem list
     * @return a list representation of all the codes in the codelistItem list
     */
    public List<String> getCodelistCodes(){
        return codelistItemList.stream().map(t->t.code).collect(Collectors.toList());
    }

    /**
     * returns a list representation of all the values in the codelistItem list
     * @return a list representation of all the values in the codelistItem list
     */
    public List<String> getCodelistValues(){
        return codelistItemList.stream().map(t->t.textvalue).collect(Collectors.toList());
    }

    /**
     * returns a list representation of the codesystem in the codelistItem list for each item
     * @return a list representation of the codesystem in the codelistItem list for each item
     */
    public List<String> getCodelistCodeSystems(){
        return codelistItemList.stream().map(t->t.codeSystem).collect(Collectors.toList());
    }

    /**
     * returns a list representation of the descriptions in the codelistItem list for each item
     * @return a list representation of the descriptions in the codelistItem list for each item
     */
    public List<String> getCodelistDescriptions(){
        return codelistItemList.stream().map(t->t.description).collect(Collectors.toList());
    }

    /**
     * get the text value for a code
     * @param code code
     * @return text value for the code
     */
    public String getValueForOptionCode(String code) {
        return codelistItemList.stream().filter(t->t.code.equalsIgnoreCase(code)).findFirst().get().textvalue;
    }

    /**
     * get displayName for a code
     * @param code code
     * @return text value for the code
     */
    public String getDescriptionForOptionCode(String code) {
        return codelistItemList.stream().filter(t->t.code.equalsIgnoreCase(code)).findFirst().get().description;
    }

    /**
     * get the codesystem for a code
     * @param code code
     * @return codesystem for the code
     */
    public String getCodesystemForOptionCode(String code){
        return codelistItemList.stream().filter(t->t.code.equalsIgnoreCase(code)).findFirst().get().codeSystem;
    }

    /**
     * returns whether the codebook item has a codelist
     * @return whether the codebook item has a codelist
     */
    public boolean hasCodeList(){
        return codelistItemList.size()>0;
    }

    /**
     * set the item's nodeType
     * @param nodeType the nodeType
     */
    public void setNodeType(NodeType nodeType){
        this.nodeType = nodeType;
    }

    /**
     * If we're adding a code, it's useful to know whether it's an integer code or a string code
     * This method attempts to convert a code to integer and if that fails, we know we're dealing with strings.
     * @param code the code that could be an int or a string
     */
    private void guessCodeType(String code){
        if(codeDataType.equalsIgnoreCase("integer")) {
            try {
                Integer.parseInt(code);
            } catch (NumberFormatException e) {
                codeDataType = "string";
            }
        }
    }

    /**
     * adds a codelistItem
     * @param code the code
     * @param textvalue the textual representation of the code
     * @param codesystem the codesystem to which the code belongs
     * @param description the name as specified independent of language
     */
    public void addCodelistItem(String code, String textvalue, String codesystem, String description) {
        // check whether we're dealing with integer or string codelist
        guessCodeType(code);
        codelistItemList.add(new CodelistItem(code, textvalue, codesystem, description));
    }

    /**
     * merge the codelist from one codebookItem into another
     * @param otherCodebookItem the second codecookItem
     */
    public void mergeCodebookItemCodeLists(CodebookItemDefault otherCodebookItem){
        this.codelistItemList.addAll(otherCodebookItem.codelistItemList);
    }

    @Override
    public String getUnits(){
        return units;
    }

    /**
     * sets the units to a new value
     * @param units the new units value
     */
    public void setUnits(String units){
        this.units = units;
    }


    // Todo: something with precision and width?
    //  Issues:
    //  * Width/Precision definition differs per EDC - e.g. is 20.0 width 4, or does the dot not count?
    //    Even worse - Castor uses width as a visual and requires enforce-decimals
    //  * If we do this for OpenEHR, we should look at this or ART-DECOR as well probably...
    @Override
    public String getPrecision(){
        return precision;
    }

    /**
     * update the precision
     * @param precision new precision value
     */
    public void setPrecision(String precision) {
        if(precision.equalsIgnoreCase("")) {
            precision = "0";
        }
        this.precision = precision;
    }

    @Override
    public String getMax(){
        return max;
    }

    /**
     * update the max value
     * @param max new max value
     */
    public void setMax(String max){
        this.max = max;
    }

    /**
     * update the max check operator
     * @param operator the new max check operator
     */
    public void setMaxCheckOperator(OperatorType operator){
        this.maxCheckOperator = operator;
    }

    @Override
    public OperatorType getMaxCheckOperator(){
        return maxCheckOperator;
    }

    @Override
    public String getMin(){
        return min;
    }

    /**
     * update the min value
     * @param min new min value
     */
    public void setMin(String min){
        this.min = min;
    }

    /**
     * update the min check operator
     * @param operator the new min check operator
     */
    public void setMinCheckOperator(OperatorType operator){
        this.minCheckOperator = operator;
    }

    @Override
    public OperatorType getMinCheckOperator(){
        return minCheckOperator;
    }

    @Override
    public String toString() {
        return itemName;
    }

    /**
     * sort the code list
     */
    public void sortCodeList(){
        codelistItemList.sort(new SortByCode());
    }

    /**
     * class for code list items
     */
    public class CodelistItem {
        public final String textvalue;
        public final String code;
        public final String codeSystem;
        public final String description;

        public CodelistItem(String code, String textvalue, String codeSystem, String description) {
            this.textvalue = textvalue;
            this.code = code;
            this.codeSystem = codeSystem;
            this.description = description;
        }
    }

    /**
     * class for sorting codelist items by code
     */
    public class SortByCode implements Comparator<CodelistItem> {
        @Override
        public int compare(CodelistItem codelistItem1, CodelistItem codelistItem2) {
            String code1 = codelistItem1.code;
            String code2 = codelistItem2.code;
            try{
                int code1Int = Integer.parseInt(code1);
                int code2Int = Integer.parseInt(code2);
                if(code1Int>code2Int) return 1;
                else if(code1Int<code2Int) return -1;
                return 0;
            } catch (Exception e) {
                return codelistItem1.code.compareToIgnoreCase(codelistItem2.code);
            }
        }
    }
}
