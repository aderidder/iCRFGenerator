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

import icrfgenerator.settings.GlobalSettings;
import icrfgenerator.utils.ArtDecorCalls;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * meta data for a codebook, e.g. name, languages, dataset versions, etc.
 */
class CodebookMetaData {
    private static final Logger logger = LogManager.getLogger(CodebookMetaData.class.getName());

    private Map<String, DatasetMetaData> idToDatasetMetaDataMap = new HashMap<>();
    private String codebook;

    CodebookMetaData(String codebook){
        this.codebook = codebook;
        retrieveMetaData();
    }

    /**
     * get a (sorted) list of the dataset identifiers present for this codebook
     * @return a sorted list with dataset identifiers
     */
    List<String> getCodebookDatasetIdentifiers(){
        List<DatasetMetaData> datasetMetaDataList = new ArrayList<>(idToDatasetMetaDataMap.values());
        Collections.sort(datasetMetaDataList);
        return datasetMetaDataList.stream().map(t->t.getId()).collect(Collectors.toList());
    }

    /**
     * returns the name of the dataset
     * @param id the id of the dataset
     * @return the name of the dataset
     */
    String getDatasetName(String id){
        return idToDatasetMetaDataMap.get(id).getName();
    }

    /**
     * returns the version of the dataset
     * @param id the id of the dataset
     * @return the version of the dataset
     */
    String getDatasetVersion(String id){
        return idToDatasetMetaDataMap.get(id).getVersion();
    }

    /**
     * returns the effective date of the dataset
     * @param id the id of the dataset
     * @return the effective date of the dataset
     */
    String getDatasetEffectiveDate(String id){
        return idToDatasetMetaDataMap.get(id).getEffectiveDate();
    }

    /**
     * returns a list with all available languages for a codebook version
     * @return a list with all available languages for a codebook version
     */
    List<String> getDatasetLanguages(String datasetIdentifier){
        return idToDatasetMetaDataMap.get(datasetIdentifier).getLanguages();
    }

    /**
     * retrieve metadata online, allowing us to know which versions are available for a codebooks and which
     * languages for each version
     */
    private void retrieveMetaData(){
        // retrieve the codebook's prefix, which we need to generate the URI
        String codebookPrefix = GlobalSettings.getCodebookPrefix(codebook);

        String uri = ArtDecorCalls.getProjectIndexURI(codebook, codebookPrefix);
        logger.log(Level.INFO, "Attempting to retrieve which version of codebook {} are available using {}", codebook, uri);

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // open a url connection
            URL url = new URL(uri);
            URLConnection urlConnection = url.openConnection();

            // set the connection timeout as specified in the global settings
            urlConnection.setConnectTimeout(GlobalSettings.getMetaDataConnectionTimeout());
            urlConnection.setReadTimeout(GlobalSettings.getMetaDataReadTimeout());

            // get a stream to read data from
            Document dom = documentBuilder.parse(urlConnection.getInputStream());

            // get the root element
            Element documentElement = dom.getDocumentElement();

            // get a nodelist of elements
            NodeList nodeList = documentElement.getElementsByTagName("dataset");

            // loop over the nodelist
            if(nodeList != null) {
                parseNodeList(nodeList, uri);
            }
        }
        catch (Exception e){
            throw new RuntimeException("Exception occurred while attempting to retrieve which version are available for the codebook: "+e.getMessage());
        }
    }

    private void parseNodeList(NodeList nodeList, String uri){
        for(int i=0; i<nodeList.getLength(); i++) {
            // retrieve the interesting meta data for the codebook
            Element element = (Element) nodeList.item(i);
            String version = element.getAttribute("versionLabel");
            String id = element.getAttribute("id");
            String name = element.getAttribute("displayName");
            String statusCode = element.getAttribute("statusCode");
            String effectiveDate = element.getAttribute("effectiveDate");

            // only store non-deprecated datasets
            if(!statusCode.equalsIgnoreCase("deprecated")) {
                DatasetMetaData datasetMetaData = new DatasetMetaData(id, version, name, effectiveDate, findLanguages(element));

                if (idToDatasetMetaDataMap.containsKey(id)) {
                    logger.log(Level.ERROR, "The meta data table already contains data for this id. This is unexpected. Skipping this one.\n" +
                            "If you feel this should be debugged please report the id {} and URI {}", id, uri);
                }
                else {
                    idToDatasetMetaDataMap.put(id, datasetMetaData);
                }
            }
            logger.log(Level.INFO, "versionlabel found: {} id found: {}", element.getAttribute("versionLabel"),element.getAttribute("id"));
        }
    }

//    private void retrieveMetaData(){
//        // retrieve the codebook's prefix, which we need to generate the URI
//        String protocolPrefix = GlobalSettings.getCodebookPrefix(codebook);
//
//        String uri = ArtDecorCalls.getProjectIndexURI(protocolPrefix);
//        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//
//        logger.log(Level.INFO, "Attempting to retrieve which version of codebook {} are available using {}", codebook, uri);
//
//        try {
//            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//            Document dom = documentBuilder.parse(uri);
//            // get the root element
//            Element documentElement = dom.getDocumentElement();
//
//            // get a nodelist of elements
//            NodeList nodeList = documentElement.getElementsByTagName("dataset");
//
//            // loop over the nodelist
//            if(nodeList != null) {
//                for(int i=0; i<nodeList.getLength(); i++) {
//                    // retrieve the interesting meta data for the codebook
//                    Element element = (Element) nodeList.item(i);
//                    String version = element.getAttribute("versionLabel");
//                    String id = element.getAttribute("id");
//                    String name = element.getAttribute("displayName");
//                    String statusCode = element.getAttribute("statusCode");
//                    String effectiveDate = element.getAttribute("effectiveDate");
//
//                    // only store non-deprecated datasets
//                    if(!statusCode.equalsIgnoreCase("deprecated")) {
//                        DatasetMetaData datasetMetaData = new DatasetMetaData(id, version, name, effectiveDate, findLanguages(element));
//
//                        if (idToDatasetMetaDataMap.containsKey(id)) {
//                            logger.log(Level.ERROR, "The meta data table already contains data for this id. This is unexpected... Skipping this one.\n" +
//                                    "If you feel this should be debugged please report the id {} and URI {}", id, uri);
//                        }
//                        else {
//                            idToDatasetMetaDataMap.put(id, datasetMetaData);
//                        }
//                    }
//                    logger.log(Level.INFO, "versionlabel found: {} id found: {}", element.getAttribute("versionLabel"),element.getAttribute("id"));
//                }
//            }
//        } catch (Exception e){
//            throw new RuntimeException("Exception occurred while attempting to retrieve which version are available for the codebook: "+e.getMessage());
//        }
//    }




    /**
     * looks for languages for a version
     * @param element element
     * @return list of languages found for the version
     */
    private List<String> findLanguages(Element element){
        List<String> languages = new ArrayList<>();
        NodeList nodeList = element.getElementsByTagName("desc");
        if(nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element descElement = (Element) nodeList.item(i);
                String language = descElement.getAttribute("language");
                if(!GlobalSettings.isLowQualityCodebookLanguage(codebook, language)) {
                    languages.add(language);
                }
                else{
                    System.err.println("Skipping codebook "+codebook+ " in language "+language+" due to low-quality...");
                }
            }
        }
        return languages;
    }
}

/**
 * class for keeping track of metadata
 * there should be exactly 1 for each id with status non-deprecated (final / draft)
 */
class DatasetMetaData implements Comparable<DatasetMetaData>{
    // 2017-09-26T13:26:31
    private static SimpleDateFormat dateFormatter1=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static SimpleDateFormat dateFormatter2=new SimpleDateFormat("dd-MMM-yyyy");

    private String version;
    private String id;
    private String name;
    private List<String> languages;
    private Date effectiveDate;

    DatasetMetaData(String id, String version, String name, String effectiveDate, List<String> languages) {
        this.id = id;
        this.version = version;
        this.name = name;
        this.languages = languages;

        // try to parse the effective date according to the expected format
        try {
            this.effectiveDate = dateFormatter1.parse(effectiveDate);
        } catch (ParseException e) {
            // if that fails, set it to a default date
            try {
                this.effectiveDate = dateFormatter2.parse("01-01-1900");
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * returns the dataset version
     * @return the dataset version
     */
    String getVersion(){
        return version;
    }

    /**
     * returns the dataset id
     * @return the dataset id
     */
    String getId(){
        return id;
    }

    /**
     * returns the dataset name
     * @return the dataset name
     */
    String getName(){
        return name;
    }

    /**
     * returns the dataset available languages
     * @return the dataset available languages
     */
    List<String> getLanguages(){
        return languages;
    }

    /**
     * returns the dataset effective date
     * @return the dataset effective date
     */
    String getEffectiveDate(){
        return dateFormatter2.format(effectiveDate);
    }

    @Override
    public int compareTo(DatasetMetaData datasetMetaData) {
        if(datasetMetaData.effectiveDate.compareTo(this.effectiveDate)==0){
            return this.name.compareTo(datasetMetaData.name);
        }
        else {
            return datasetMetaData.effectiveDate.compareTo(this.effectiveDate);
        }
    }
}

