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

import icrfgenerator.settings.runsettings.RunSettings;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * default implementation of the EDC interface
 */
public abstract class EDCDefault implements EDC {
    private final Map<String, Integer> itemNameCounterMap = new HashMap<>();
    private final String edcName;

    EDCDefault(String edcName){
        this.edcName = edcName;
    }

    /**
     * Generate crfs
     * @param file base filename
     */
    @Override
    public void generateCRFs(File file){
        RunSettings runSettings = RunSettings.getInstance();
        List<String> languageList = runSettings.getAllLanguages();
        for(String language:languageList) {
            List<String> keys = runSettings.getAllKeysForLanguage(language);
            setup();
            generateCRF(keys, language);
            writeFile(getLanguageFile(file, language));
        }
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
     * change the base filename to one with a language
     * e.g. myfile.xlsx --> myfile_en.xlsl
     * @param file     base file
     * @param language language to add to the name
     * @return the new file with the language in the name
     */
    private File getLanguageFile(File file, String language){
        String fileName = file.getPath();
        String noExtension = fileName.substring(0, fileName.lastIndexOf("."));
        String extension = fileName.substring(fileName.lastIndexOf("."));
        return new File(noExtension+"_"+language+extension);
    }

    /**
     * in case where items are inherited, like the zibs, an item name may use its uniqueness
     * this method ensures an item is unique
     * ToDo: is still an issue with the new approach?
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
     * todo: do we still need this?
     */
    private void resetUnique(){
        itemNameCounterMap.clear();
    }

    /**
     * setup the EDC
     */
    final public void setup(){
        resetUnique();
        setupEDC();
    }

    /**
     * to be implemented for each EDC to ensure each can do its own local setup
     */
    abstract void setupEDC();
    abstract void generateCRF(List<String> keys, String language);
    abstract void writeFile(File file);

}
