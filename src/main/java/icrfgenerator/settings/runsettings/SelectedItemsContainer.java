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

package icrfgenerator.settings.runsettings;


import java.util.*;

/**
 * keeps track of the items selected in a codebook+version+language
 * all EDCs are expected to create an extension of this class to allow for their specifics
 */
public abstract class SelectedItemsContainer {
    private List<String> selectedItemsList = new ArrayList<>();
    private String key;

    protected Map<String, ItemDetails> itemDetailsMap = new HashMap<>();

    /**
     * constructor for a selected items container
     * @param key codebook+datasetId+language
     */
    protected SelectedItemsContainer(String key){
        this.key = key;
    }

    /**
     * returns whether the item is selected
     * @param itemId id of the item
     * @return true/false
     */
    boolean isItemSelected(String itemId){
        return selectedItemsList.contains(itemId);
    }

    /**
     * returns the selected codes + values for an item
     * @param itemId item id
     * @return set with codes + values
     */
    List<String> getTerminologyCodes(String itemId){
        itemDetailsMap.get(itemId).getSelectedTerminologyCodes().sort(new SortByCode());
        return itemDetailsMap.get(itemId).getSelectedTerminologyCodes();
    }

    /**
     * add the selected code + value for the item
     * @param itemId id of the item
     * @param code selected code
     */
    void addTerminology(String itemId, String code){
        itemDetailsMap.get(itemId).addSelectedTerminologyCode(code);
    }

    /**
     * remove the selected code for the item
     * @param itemId id of the item
     * @param code code to remove
     */
    void removeTerminology(String itemId, String code){
        itemDetailsMap.get(itemId).removeSelectedTerminologyCode(code);
    }

    void removeTerminologies(String itemId){
        itemDetailsMap.get(itemId).removeSelectedTerminologyCode();
    }

    /**
     * add the item the user just selected
     * @param itemId id of item
     */
    void addItem(String itemId){
        if(!selectedItemsList.contains(itemId)){
            selectedItemsList.add(itemId);
        }
        if(!itemDetailsMap.containsKey(itemId)){
            addItemEDC(key, itemId);
        }
    }

    /**
     * remove the item, after the user deselected it
     * @param itemId id of item
     */
    void removeItem(String itemId){
        selectedItemsList.remove(itemId);
//        itemDetailsMap.remove(itemId);
    }

    /**
     * returns list of selected items
     * @return list of selected items
     */
    List<String> getSelectedItemsList(){
        return selectedItemsList;
    }

    /**
     * returns the name of an item, based on the itemId
     * @param itemId the identifier of the item
     * @return the name of the item
     */
//    String getSelectedItemItemName(String itemId){
//        return itemDetailsMap.get(itemId).getItemName();
//    }

    /**
     * abstract method which is necessary for every EDC to add an EDC-specific item
     * @param key codebook+datasetId+language
     * @param itemId id of the item
     */
    abstract protected void addItemEDC(String key, String itemId);


    /**
     * sort the selected terminology codes
     */
    class SortByCode implements Comparator<String> {
        @Override
        public int compare(String code1, String code2) {
            try{
                int code1Int = Integer.parseInt(code1);
                int code2Int = Integer.parseInt(code2);
                if(code1Int>code2Int) return 1;
                else if(code1Int<code2Int) return -1;
                return 0;
            }catch (Exception e) {
                return code1.compareToIgnoreCase(code2);
            }
        }
    }

}