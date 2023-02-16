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
import icrfgenerator.types.OperatorType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * makes sure the iCRFSettings file exists
 * stores some global settings
 */
public class GlobalSettings {
    private static final String cacheDirName = "."+File.separator+"cache";
    private static final String icrfSettingsFilename = "iCRFSettings.xlsx";

    private static final List<String> edcList = Arrays.asList("REDCap", "LibreClinica / OpenClinica 3", "Castor - Step", "Castor - Report", "Castor - Survey", "ODM-XML", "Molgenis EMX");
    private static final List<String> uiLanguages = Arrays.asList("en", "nl");

    // timeout settings; 5000 is 5 seconds.
    private static final int metaDataConnectionTimeout = 15000;
    private static final int metaDataReadTimeout = 30000;
    private static final int codebookConnectionTimeout = 15000;
    private static int codebookReadTimeout = 120000;

    private static final Map<String, String> serverToOnlineURL = new HashMap<>();

    /**
     * setup:
     * - create the cache directory if it doesn't exist
     * - copy the default codebook file to this directory if no codebooks.txt exists
     * - read the codebooks.txt file
     */
    static {
        setupCacheDir();
        readiCRFSettingsFile();
        serverToOnlineURL.put("https://ckm.openehr.org/ckm/", "https://ckm.openehr.org/ckm/archetypes/");
        serverToOnlineURL.put("https://decor.nictiz.nl/services/", "https://decor.nictiz.nl/art-decor/decor-project--");

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
     * if there is no iCRFSettings file in the cache dir, copy the defaultiCRFSettings file there
     * @param file the file we're looking for
     * @throws IOException issue copying file
     */
    private static void copyFileToCacheDir(File file) throws IOException {
        if(!file.exists()){
            InputStream inputStream = ResourceManager.getResourceAsStream("/default"+file.getName());
            Files.copy(inputStream, Paths.get(file.getPath()));
        }
    }

    /**
     * read the iCRFSettings file
     */
    private static void readiCRFSettingsFile(){
        File settingsFilename = new File(cacheDirName+File.separator+icrfSettingsFilename);
        try{
            copyFileToCacheDir(settingsFilename);
            readiCRFSettingsFile(settingsFilename);
        } catch (IOException e) {
            throw new RuntimeException("Fatal error: Something went wrong while attempting to copy the default " +
                    "messages file.");
        }
    }

    /**
     * Opens the iCRFSettings Excel file and parses it
     * @param file the file
     */
    public static void readiCRFSettingsFile(File file) {
        try {
            Workbook workbook = WorkbookFactory.create(file);
            CodebookInfo.readCodebooks(workbook.getSheet("Codebooks"));
            OperatorType.readValidationMessages(workbook.getSheet("ValidationMessages"));
            LanguageHelper.readLanguageSheet(workbook.getSheet("Languages"));
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * returns a String which can be used to go to the codebook online
     * @param codebook name of the codebook
     * @return a String which can be used to go to the codebook online
     */
    public static String getOnlineURLString(String codebook){
        return serverToOnlineURL.get(getServer(codebook))+getCodebookPrefix(codebook);
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
        return CodebookInfo.getCodebookNames();
    }

    /**
     * returns the codebook's source
     * @param codebookName name of the codebook
     * @return the codebook's source
     */
    public static String getSource(String codebookName){
        return CodebookInfo.getSource(codebookName);
    }

    /**
     * returns a list with codebooks filtered by their source
     * @param sources the sources to filter by
     * @return a list with codebook names
     */
    public static List<String> getCodebookNamesFilteredBySource(List<String> sources){
        return CodebookInfo.getCodebookNamesFilteredBySource(sources);
    }

    /**
     * returns a list with codebooks filted by tags - only codebooks that have all tags are returned
     * @param tags list with tags
     * @return a list with codebook names
     */
    public static List<String> getCodebookNamesFilteredByTagAll(List<String> tags){
        return CodebookInfo.getCodebookNamesFilteredByTagAll(tags);
    }

    /**
     * returns a list with codebooks filted by tags - codebooks that have any of the tags are returned
     * @param tags list with tags
     * @return a list with codebook names
     */
    public static List<String> getCodebookNamesFilteredByTagAny(List<String> tags){
        return CodebookInfo.getCodebookNamesFilteredByTagAny(tags);
    }

    /**
     * returns a list with all tags
     * @return a list with all tags
     */
    public static List<String> getTags(){
        return CodebookInfo.getTags();
    }

    /**
     * returns the server on which the codebook is stored
     * @param codebookName name of the codebook
     * @return server on which the codebook is stored
     */
    public static String getServer(String codebookName){
        return CodebookInfo.getServer(codebookName);
    }

    /**
     * returns whether a codebook + language is marked as being of bad quality
     * @param codebookName name of the codebook
     * @param language language of the codebook
     * @return true/false
     */
    public static boolean isLowQualityCodebookLanguage(String codebookName, String language){
        return CodebookInfo.isLowQualityCodebookLanguage(codebookName, language);
    }

    /**
     * get a prefix of a codebook, which is necessary for the art-decor calls
     * @param codebookName    name of the codebook
     * @return  prefix of the codebook
     */
    public static String getCodebookPrefix(String codebookName){
        return CodebookInfo.getCodebookPrefix(codebookName);
    }

    /**
     * some codebooks in ART-DECOR have a group node which itself is also an item
     * this functions returns whether, for this codebook, groups should also be considered items.
     * @param codebookName name of the codebook
     * @return whether group should be considered an item
     */
    public static boolean groupIsAnItem(String codebookName){
        return CodebookInfo.groupIsAnItem(codebookName);
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

    /**
     * sets the codebook read timeout
     * @param codebookReadTimeout value (ms), which is multiplied by 1000 to get to seconds
     */
    public static void setCodebookReadTimeout(int codebookReadTimeout){
        GlobalSettings.codebookReadTimeout = codebookReadTimeout*1000;
    }
}
