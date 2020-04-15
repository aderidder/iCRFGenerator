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

package icrfgenerator.codebook;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * an item an the codebook
 */
public class CodebookItem {
    private static final Logger logger = LogManager.getLogger(CodebookItem.class.getName());

    private String id;
    private String itemName;
    private String dataType;
    private String description;
    private String codeDataType = "integer";
    private String nodeType;

    private String codeSystem;
    private String code;
    private String codeDescription;

    private List<CodelistItem> codelistItemList = new ArrayList<>();

    private List<String> toGenerateCodeListItemList = new ArrayList<>();

    public CodebookItem(String id, String itemName, String description, String nodeType){
        this(id, itemName, description, "", nodeType, "", "", "");
    }

    CodebookItem(String id, String itemName, String description, String dataType, String nodeType){
        this(id, itemName, description, dataType, nodeType, "", "", "");
    }

    CodebookItem(String id, String itemName, String description, String dataType, String nodeType, String code, String codeSystem, String codeDescription){
        this.id = id;
        this.itemName = itemName;
        this.description = description;
        this.dataType = dataType;
        this.nodeType = nodeType;

        this.code = code;
        this.codeSystem = codeSystem;
        this.codeDescription = codeDescription;
    }

    /**
     * if art-decor suggests the datatype is a codetype, we need to know whether this is integer or string, for e.g.
     * OpenClinica. this method attempts to convert a code to integer and if that fails, we know we're dealing with
     * strings.
     * @param code
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
     * if there a problems with a codelist, don't use it and give an error message for it
     */
    void setBrokenCodeList(){
        logger.log(Level.ERROR, "{} has codelist issues... skipping the codelist", itemName);
        codelistItemList.clear();
        dataType="string";
    }

    /**
     * get the text value for a code
     * @param code code
     * @return text value for the code
     */
    String getValueForCode(String code) {
        return codelistItemList.stream().filter(t->t.code.equalsIgnoreCase(code)).findFirst().get().textvalue;
    }

    String getCodesystemForCode(String code){
        return codelistItemList.stream().filter(t->t.code.equalsIgnoreCase(code)).findFirst().get().codeSystem;
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
    public String getDataType(){
        return dataType;
    }

    /**
     * if the item is a codelist, returns whether this codelist has integer or string requirements for its codes
     * @return the item's
     */
    public String getCodeDataType(){
        return codeDataType;
    }


    /**
     * returns the codebook item's description
     * @return the codebook item's description
     */
    public String getDescription(){
        return description;
    }

    /**
     * returns whether the codebook item has a codelist
     * @return whether the codebook item has a codelist
     */
    public boolean hasCodeList(){
        return codelistItemList.size()>0;
    }

    /**
     * adds a codelistItem
     * @param code the code
     * @param textvalue the textual representation of the code
     * @param codesystem the codesystem to which the code belongs
     */
    void addCodelistItem(String code, String textvalue, String codesystem) {
        // check whether we're dealing with integer or string codelist
        guessCodeType(code);
        codelistItemList.add(new CodelistItem(code, textvalue, codesystem));
    }

    /**
     * sort the code list
     */
    void sortCodeList(){
        codelistItemList.sort(new SortByCode());
    }
    /**
     * Not all codelist items have a code in art-decor. This complicates things, as I was using the code
     * as my id and also an EDC needs a code. So when we encounter such an item, we store it in a separate list
     * and later on we generate codes for these items
     * We can't generate a code straight away, as if the codebook were to have 1, "", 2 we would end up with two 2's.
     * @param textvalue the textvalue of the codelist item
     */
    void addToGenerateCodeListItem(String textvalue){
        toGenerateCodeListItemList.add(textvalue);
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
                codelistItemList.add(new CodelistItem("Code_" + (i+1), toGenerateCodeListItemList.get(i), "Generated"));
            }
            codeDataType = "string";
        }
        toGenerateCodeListItemList.clear();
    }

    /**
     * returns a list representation of all the codes in the codelistItem list
     * @return a list representation of all the codes in the codelistItem list
     */
    public List<String> getCodesList(){
        return codelistItemList.stream().map(t->t.code).collect(Collectors.toList());
    }

    /**
     * returns a list representation of all the values in the codelistItem list
     * @return a list representation of all the values in the codelistItem list
     */
    public List<String> getValuesList(){
        return codelistItemList.stream().map(t->t.textvalue).collect(Collectors.toList());
    }

    /**
     * returns a list representation of the codesystem in the codelistItem list for each item
     * @return a list representation of the codesystem in the codelistItem list for each item
     */
    public List<String> getCodeSystemList(){
        return codelistItemList.stream().map(t->t.codeSystem).collect(Collectors.toList());
    }

    public String getCodeSystem(){
        return codeSystem;
    }

    public String getCode(){
        return code;
    }

    public String getCodeDescription(){
        return codeDescription;
    }

    @Override
    public String toString() {
        return itemName;
    }


    /**
     * class for code list items
     */
    class CodelistItem {
        private String textvalue;
        private String code;
        private String codeSystem;

        CodelistItem(String code, String textvalue, String codeSystem) {
            this.textvalue = textvalue;
            this.code = code;
            this.codeSystem = codeSystem;
        }
    }

    /**
     * class for sorting codelist items by code
     */
    class SortByCode implements Comparator<CodelistItem> {
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
            }catch (Exception e) {
                return codelistItem1.code.compareToIgnoreCase(codelistItem2.code);
            }
        }
    }
}
