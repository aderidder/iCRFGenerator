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

import icrfgenerator.resourcemanagement.ResourceManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * stores some global settings
 */
public class GlobalSettings {
//    public static String server = "http://decor.nictiz.nl/services/";

    private static final String cacheDirName = "."+File.separator+"cache";
    private static final String codebookFileName = "codebooks.txt";

    private static final List<String> edcList = Arrays.asList("REDCap", "OpenClinica 3", "Castor - Step", "Castor - Report", "Castor - Survey", "Molgenis EMX");
    private static final List<String> uiLanguages = Arrays.asList("en", "nl");

    // timeout settings; 5000 is 5 seconds.
    private static final int metaDataConnectionTimeout = 5000;
    private static final int metaDataReadTimeout = 15000;
    private static final int codebookConnectionTimeout = 5000;
    private static final int codebookReadTimeout = 45000;

    private static final List<String> lowQualityCodebookLanguageCombinations = new ArrayList<>();
    private static final Map<String, Source> codebookNameToSourceMap = new TreeMap<>();

    /**
     * setup:
     * - create the cache directory if it doesn't exist
     * - copy the default codebook file to this directory if no codebooks.txt exists
     * - read the codebooks.txt file
     */
    static {
        setup();
    }

    /**
     * setup
     */
    private static void setup(){
        setupCacheDir();
        setupCodebookFile();
        readCodebookFile();
    }

    /**
     * create the cache dir if it doesn't exist
     */
    private static void setupCacheDir(){
        File cacheDir = new File(cacheDirName);
        if(!cacheDir.exists()){
            cacheDir.mkdir();
        }
    }

    /**
     * copy the default codebooks file it no codebookfile exists
     */
    private static void setupCodebookFile(){
        File codebookFile = new File(cacheDirName+File.separator+codebookFileName);
        if(!codebookFile.exists()){
            InputStream inputStream = ResourceManager.getResourceAsStream("/defaultCodebooks.txt");
            try {
                Files.copy(inputStream, Paths.get(codebookFile.getPath()));
            } catch (IOException ex) {
                throw new RuntimeException("Fatal error: Something went wrong while attempting to copy the default " +
                        "codebooks file.");
            }
        }
    }

    /**
     * read the codebooks file
     * a codebook file contains the following tab-separated values:
     * Required:
     * - name
     * - prefix
     * - server
     * - whether a group itself should be considered an item
     *
     * Optional:
     * - language to ignore (e.g. en-US). In case of multiple languages, these are comma-separated
     */
    private static void readCodebookFile(){
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(cacheDirName+File.separator+codebookFileName))){
            String line = bufferedReader.readLine();
            while(line != null) {
                // split the line, based on tabs. Multiple tabs are seen as one
                String[] splitLine = line.trim().split("\t+");
                // ignore a line if it starts with a # or if the length is too short
                if(!line.startsWith("#") && splitLine.length>2) {
                    // add the entry to the map.
                    codebookNameToSourceMap.put(splitLine[0], new Source(splitLine[1], splitLine[2], Boolean.valueOf(splitLine[3])));
                    // check whether there are languages which should be ignored
                    if(splitLine.length==5){
                        // check for multiple languages (comma separated)
                        String[] skipLanguages = splitLine[4].split(",");
                        for(String skipLanguage:skipLanguages){
                            lowQualityCodebookLanguageCombinations.add(splitLine[0]+"_"+skipLanguage.trim());
                        }
                    }
                }
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Fatal error: something is wrong with the codebooks.txt file.");
        }
    }

    /**
     * returns the cache directory
     * @return the cache directory
     */
    public static String getCacheDir(){
        return cacheDirName;
    }

    /**
     * returns the UI languages
     * @return the UI languages
     */
    public static List<String> getUILanguages(){
        return uiLanguages;
    }

    /**
     * returns the EDC list
     * @return the EDC list
     */
    public static List<String> getEdcList(){
        return edcList;
    }

    /**
     * returns a list of the known codebook names
     * @return a list of the known codebook names
     */
    public static List<String> getCodebookNames() {
        return new ArrayList<>(codebookNameToSourceMap.keySet());
    }

    /**
     * returns the server on which the codebook is stored
     * @param codebookName name of the codebook
     * @return server on which the codebook is stored
     */
    public static String getServer(String codebookName){
        return codebookNameToSourceMap.get(codebookName).getServer();
    }

    /**
     * returns whether a codebook + language is marked as being of bad quality
     * @param codebookName name of the codebook
     * @param language language of the codebook
     * @return true/false
     */
    public static boolean isLowQualityCodebookLanguage(String codebookName, String language){
        return lowQualityCodebookLanguageCombinations.contains(codebookName+"_"+language);
    }

    /**
     * get a prefix of a codebook, which is necessary for the art-decor calls
     * @param codebookName    name of the codebook
     * @return  prefix of the codebook
     */
    public static String getCodebookPrefix(String codebookName){
        return codebookNameToSourceMap.get(codebookName).getPrefix();
    }

    /**
     * some codebooks in ART-DECOR have a group node which itself is also an item
     * this functions returns whether, for this codebook, groups should also be considered items.
     * @param codebookName name of the codebook
     * @return whether group should be considered an item
     */
    public static boolean groupIsAnItem(String codebookName){
        return codebookNameToSourceMap.get(codebookName).isGroupIsAnItem();
    }

    /**
     * returns the connection timeout for metadata retrieval
     * @return the connection timeout for metadata retrieval
     */
    public static int getMetaDataConnectionTimeout() {
        return metaDataConnectionTimeout;
    }

    /**
     * returns the read timeout for metadata retrieval
     * @return the read timeout for metadata retrieval
     */
    public static int getMetaDataReadTimeout() {
        return metaDataReadTimeout;
    }

    /**
     * returns the connection timeout for codebook retrieval
     * @return the connection timeout for codebook retrieval
     */
    public static int getCodebookConnectionTimeout() {
        return codebookConnectionTimeout;
    }

    /**
     * returns the read timeout for codebook retrieval
     * @return the read timeout for codebook retrieval
     */
    public static int getCodebookReadTimeout() {
        return codebookReadTimeout;
    }
}

/**
 * Defintion of a codebook; stores its prefix and whether a non-leaf node should be considered an item or not
 */
class Source{
    private String server;
    private String prefix;
    private boolean groupIsAnItem;

    Source(String prefix, String server, boolean groupIsAnItem){
        this.prefix = prefix;
        this.groupIsAnItem = groupIsAnItem;
        this.server = server;
    }

    String getServer(){
        return server;
    }

    String getPrefix() {
        return prefix;
    }

    boolean isGroupIsAnItem() {
        return groupIsAnItem;
    }
}