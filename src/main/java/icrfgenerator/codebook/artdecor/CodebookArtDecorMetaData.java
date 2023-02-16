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

package icrfgenerator.codebook.artdecor;

import icrfgenerator.codebook.CodebookMetaDataDefault;
import icrfgenerator.settings.GlobalSettings;
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
import java.util.*;

/**
 * meta data for a codebook, e.g. name, languages, dataset versions, etc.
 */
public class CodebookArtDecorMetaData extends CodebookMetaDataDefault {
    private static final Logger logger = LogManager.getLogger(CodebookArtDecorMetaData.class.getName());
    private static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * constructor
     * @param codebookName name of the codebook
     */
    public CodebookArtDecorMetaData(String codebookName){
        super(codebookName, dateFormat);
        parseXMLMetaData();
    }

    /**
     * parse the metadata xml
     */
    private void parseXMLMetaData() {
        String uri = getMetaDataURI();

        logger.log(Level.DEBUG, "Attempting to retrieve metadata for codebook {} available using {}", codebookName, uri);

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
            Element roottElement = dom.getDocumentElement();

            parseData(roottElement);

        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while attempting to retrieve which version are available for the codebook: " + e.getMessage());
        }
    }

    /**
     * start parsing from the root element
     * @param documentElement the root element
     */
    private void parseData(Element documentElement) {
            // get a nodelist of elements
            NodeList nodeList = documentElement.getElementsByTagName("dataset");

            // loop over the nodelist
            if(nodeList != null) {
                parseNodeList(nodeList);
            }
    }

    /**
     * returns URI where the metadata can be found
     * @return URI where the metadata can be found
     */
    private String getMetaDataURI() {
        return GlobalSettings.getServer(codebookName)+"ProjectIndex?view=d&prefix="+GlobalSettings.getCodebookPrefix(codebookName)+"&format=xml";
    }


    /**
     * Parses the nodeList to find the codebook metadata
     * @param nodeList nodeList to parse
     */
    private void parseNodeList(NodeList nodeList){
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
                    logger.log(Level.ERROR, "The meta data table already contains data for id {}. This is unexpected. Skipping this one.\n", id);
                }
                else {
                    idToDatasetMetaDataMap.put(id, datasetMetaData);
                }
            }
            logger.log(Level.INFO, "versionlabel found: {} id found: {}", element.getAttribute("versionLabel"),element.getAttribute("id"));
        }
    }

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
                if(!GlobalSettings.isLowQualityCodebookLanguage(codebookName, language)) {
                    languages.add(language);
                }
                else{
                    System.err.println("Skipping codebook "+ codebookName + " in language "+language+" due to low-quality...");
                }
            }
        }
        return languages;
    }

}

