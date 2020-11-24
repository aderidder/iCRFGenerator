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

import java.util.ArrayList;
import java.util.List;

/**
 * details of a selected item
*/
public class ItemDetails{
    private String key;
    private String itemId;
    private List<String> codesList = new ArrayList<>();

    /**
     * itemdetails constructor
     * @param key used as a reference to the codebook
     * @param itemId this item's identifier
     */
    protected ItemDetails(String key, String itemId){
        this.key = key;
        this.itemId = itemId;
    }

    /**
     * clear the selected codes
     */
    void resetTerminology(){
        codesList.clear();
    }

    /**
     * the name of this item
     * @return the name of the item
     */
//    String getItemName(){
//        // as the codebook manager for the name of this item, based on the key (for the appropriate codebook) and our itemId
//        return CodebookManager.getInstance().getItemName(key, itemId);
//    }

    /**
     * add code & value the user just selected
     * @param code code
     */
    void addSelectedTerminologyCode(String code){
        if(!codesList.contains(code)){
            codesList.add(code);
        }
    }

    /**
     * remove the code the user deselected
     * @param code code
     */
    void removeSelectedTerminologyCode(String code){
        codesList.remove(code);
    }

    void removeSelectedTerminologyCode(){
        codesList.clear();
    }

    /**
     * get a list with the selected terminology codes
     * @return List with the selected terminology codes
     */
    List<String> getSelectedTerminologyCodes(){
        return codesList;
    }

}
