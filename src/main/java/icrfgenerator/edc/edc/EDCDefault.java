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

package icrfgenerator.edc.edc;

import java.util.HashMap;
import java.util.Map;

/**
 * default implementation of the EDC interface
 */
public abstract class EDCDefault implements EDC {
    private Map<String, Integer> itemNameCounterMap = new HashMap<>();
    private String edcName;

    EDCDefault(String edcName){
        this.edcName = edcName;
    }


    /**
     * in case where items are inherited, like the zibs, an item name may use its uniqueness
     * this method ensures an item is unique
     * @param itemName name of the item which we want to be unique
     * @return either the itemName or the itemName with a trailing number
     */
    String makeUnique(String itemName){
        if(!itemNameCounterMap.containsKey(itemName)){
            itemNameCounterMap.put(itemName, 1);
            return itemName;
        }
        else{
            int counter = itemNameCounterMap.get(itemName)+1;
            itemNameCounterMap.put(itemName, counter);
            return itemName+"_"+(counter);
        }
    }

    /**
     * reset the unique item-name tracker
     */
    private void resetUnique(){
        itemNameCounterMap.clear();
    }

    /**
     * returns the name of the EDC
     * @return the name of the EDC
     */
    @Override
    public final String getEDCName(){
        return edcName;
    }

    /**
     * setup the EDC
     */
    @Override
    final public void setup(){
        resetUnique();
        setupEDC();
    }

    /**
     * to be implemented for each EDC to ensure each can do its own local setup
     */
    abstract void setupEDC();
}
