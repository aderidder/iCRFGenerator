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

package icrfgenerator.settings;

import java.util.*;

/**
 * stores some global settings
 */
public class GlobalSettings {
    public static String server = "http://decor.nictiz.nl/services/";

    private static List<String> edcList = Arrays.asList("Castor", "REDCap", "OpenClinica 3");
    private static List<String> uiLanguages = Arrays.asList("en", "nl");

    // timeout settings; 5000 is 5 seconds.
    private static int metaDataConnectionTimeout = 5000;
    private static int metaDataReadTimeout = 15000;
    private static int codebookConnectionTimeout = 5000;
    private static int codebookReadTimeout = 45000;

    private static List<String> lowQualityProtocolLanguageCombinations = new ArrayList<>();
    private static Map<String, Source> protocolNameToSourceMap = new TreeMap<>();

    // initialise some basic codebooks parameters
    // TODO: add some functionality which will allow us to read codebook setting from a file or something like that
    static {
        protocolNameToSourceMap.put("RIVM bevolkingsonderzoeken", new Source("rivmsp-", false));
        protocolNameToSourceMap.put("PALGA Colonbiopt Protocol", new Source("ppcolbio-", false));
        protocolNameToSourceMap.put("PALGA Colonrectum Carcinoom Protocol", new Source("ppcolcar-", false));
        protocolNameToSourceMap.put("Cancer Core Europe", new Source("cce-", true));
        protocolNameToSourceMap.put("Zorginformatiebouwstenen 2017", new Source("zib2017bbr-", false));
        protocolNameToSourceMap.put("Basisgegevenssset Zorg 2017", new Source("bgz2017-", false));

        // TODO: improve how we do this, but I'm not yet sure about the granularity
        lowQualityProtocolLanguageCombinations.add("RIVM bevolkingsonderzoeken_en-US");
    }

    public static List<String> getUILanguages(){
        return uiLanguages;
    }

    public static List<String> getEdcList(){
        return edcList;
    }

    /**
     * returns a list of the known codebook names
     * @return a list of the known codebook names
     */
    public static List<String> getCodebookNames() {
        return new ArrayList<>(protocolNameToSourceMap.keySet());
    }

    /**
     * returns whether a codebook + language is marked as being of bad quality
     * @param protocol name of the codebook
     * @param language language of the codebook
     * @return true/false
     */
    public static boolean isLowQualityProtocolLanguage(String protocol, String language){
        return lowQualityProtocolLanguageCombinations.contains(protocol+"_"+language);
    }

    /**
     * get a prefix of a protocol, which is necessary for the art-decor calls
     * @param protocolName    name of the protocol
     * @return  prefix of the protocol
     */
    public static String getProtocolPrefix(String protocolName){
        return protocolNameToSourceMap.get(protocolName).getPrefix();
    }

    /**
     * some codebooks in Art-Decor have a group node which itself is also an item
     * this functions returns whether, for this protocol, groups should also be considered items.
     * @param protocolName
     * @return
     */
    public static boolean groupIsAnItem(String protocolName){
        return protocolNameToSourceMap.get(protocolName).isGroupIsAnItem();
    }

    public static int getMetaDataConnectionTimeout() {
        return metaDataConnectionTimeout;
    }

    public static int getMetaDataReadTimeout() {
        return metaDataReadTimeout;
    }

    public static int getCodebookConnectionTimeout() {
        return codebookConnectionTimeout;
    }

    public static int getCodebookReadTimeout() {
        return codebookReadTimeout;
    }
}

/**
 * Defintion of a codebook; stores its prefix and whether a non-leaf node should be considered an item or not
 */
class Source{
    private String prefix;
    private boolean groupIsAnItem;

    Source(String prefix, boolean groupIsAnItem){
        this.prefix = prefix;
        this.groupIsAnItem = groupIsAnItem;
    }

    String getPrefix() {
        return prefix;
    }

    boolean isGroupIsAnItem() {
        return groupIsAnItem;
    }
}