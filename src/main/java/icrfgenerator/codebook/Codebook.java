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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static icrfgenerator.utils.XMLUtils.*;

/**
 * This class attempts to construct a codebook from an Art-Decor XML file
 * It also keeps track of the tree structure that was present in Art-Decor, as we need
 * that to create the same representation in our GUI.
 *
 * The Art-Decor XML is complicated and differences between datasets/codebooks make parsing this structure
 * properly pretty hard. E.g. in RIVM's codebook the selection options are available in the
 * valueDomain, whereas in the BgZ these options are found in the valueSet. Similarly, the information
 * about the codesystem used is in some codebooks part of a concept element and in others it is found in
 * the terminologyAssociation element instead.
 * Hence, mistakes in the parsing probably exist and future codebooks may require parts of this class
 * to be rewritten.
 *
 * Mental note to self for when I need to test this again:
 * BgZ and RIVM seems to be the main variants at the moment
 * For BgZ, SoortTabakGebruik is a nice example
 * For RIVM, screening is a nice exmaple, as is ASA score, as the latter has a valueDomain AND a valueSet with options
 * RIVM also has codelists which do not have codes, e.g. Reden afmelding
 */
class Codebook {
    private static final Logger logger = LogManager.getLogger(Codebook.class.getName());
    private static int uniqueCounter=0;

    private List<String> detectInfiniteReferencing = new ArrayList<>();
    private Map<String, CodebookItem> codebookItemMap = new HashMap<>();

    private Map<String, String> codeSystemMap = new HashMap<>();
    private Map<String, TerminologyElement> terminologyAssociationMap = new HashMap<>();

    private String language;
    private CodebookStructureNode root;

    private boolean groupIsAnItem;

    /**
     * Codebook constructor
     * @param datasetId    id of the dataset, e.g. 2.16.840.1.113883.2.4.3.11.60.42.1.1
     * @param language     language of interest of the dataset, e.g. nl-NL
     * @param codebookName name of the codebook, e.g. Basisgegevensset Zorg 2017
     */
    Codebook(String datasetId, String language, String codebookName){
        this.language = language;
        // create top node for keeping track of the tree structure of the codebook
        root =  new CodebookStructureNode(new CodebookItem("-1", codebookName, codebookName, "group"));
        // retrieve whether for this codebook an non-leaf node is itself an actual item
        groupIsAnItem = GlobalSettings.groupIsAnItem(codebookName);

        // Get the XML file and create a codebook for it
        File xmlFile = getXMLFile(datasetId, language);
        Element rootElement = getRootElement(xmlFile);
        createBook(rootElement, root);
    }

    /**
     * unique id generator, which is basically adding a unique postfix to an id
     * this is necessary because of the repeats in the zibs and because of the dummy fields
     * @param id the id as stored in art-decor
     * @return the id with a "_"+counter value
     */
    private static String getUniqueId(String id){
        return id+"_"+uniqueCounter++;
    }

    /**
     * Downloads an XML file based on datasetId and language from Art-Decor if it doesn't
     * exists and then attempts to create a codebook for the local datafile
     * @param datasetId    id of the dataset, e.g. 2.16.840.1.113883.2.4.3.11.60.42.1.1
     * @param language     language of interest of the dataset, e.g. nl-NL
     */
    private static File getXMLFile(String datasetId, String language){
        // check whether the cache dir exists; if not, we create it
        String dir = ".\\cache\\";
        File fileDir = new File(dir);
        if(!fileDir.exists()){
            fileDir.mkdir();
        }
        // generate the file name and check whether it exists in the cache
        String fileName = dir+datasetId+language+".xml";
        File dataFile = new File(fileName);
        try {
            // check whether the file exists and download it if it doesn't
            // this is time-consuming, as Art-Decor is pretty slow in generating this file; hence we're saving it locally
            if(!dataFile.exists()){
                downloadXMLFile(datasetId, language, fileName);
            }
            logger.log(Level.INFO, "Opening the codebook xml file: "+dataFile.getName());
            return dataFile;
        } catch (IOException e) {
            throw new RuntimeException("There was an issue downloading the XML file... This is fatal...");
        }
    }


    /**
     * create a codebook for a locally stored xml file
     * @param file the xml file for which we're creating a codebook
     */
    private static Element getRootElement(File file){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(file);

            //get the root element
            return dom.getDocumentElement();
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("There was an issue finding the root element in the xml.. This is fatal...");
        }
    }

    /**
     * parses the codebook starting from the root element
     * @param element an element (starts as root element)
     * @param parentNode tree node to keep track of the structure
     */
    private void createBook(Element element, CodebookStructureNode parentNode) {
        // search for codesystem links
        addCodeSystem(element);


        // find the concept elements that are children of our current element
        List<Element> conceptList = getChildElementsWithName(element, "concept");

        // loop over all found children and handle each of them
        for (Element conceptElement : conceptList) {
            handleConceptElement(element, conceptElement, parentNode);
        }
    }

    /**
     * handles a single concept element
     * @param element an element
     * @param conceptElement a child concept of the element
     * @param parentNode tree node to keep track of the structure
     */
    private void handleConceptElement(Element element, Element conceptElement, CodebookStructureNode parentNode){
        if (hasValidStatusCode(conceptElement)) {
            if(isGroupElement(conceptElement)) {
                // check whether the item refers to another codebook. This happens e.g. in the ZIBs
                Element containsElement = getChildElementWithName(conceptElement, "contains");
                if(containsElement!=null){
                    handleReferencedCodebook(element, conceptElement, containsElement, parentNode);
                }

                // if we're dealing with a setting in which a group element should be a selectable item
                else if (groupIsAnItem) {
                    handleGroupIsAnItem(conceptElement, parentNode);
                }
                else {
                    CodebookItem groupItem = createGroupConceptElement(conceptElement);
                    CodebookStructureNode newParent = addItem(groupItem, parentNode);
                    createBook(conceptElement, newParent);
                }
            }
            else{
                CodebookItem itemItem = createItemElement(conceptElement);
                addItem(itemItem, parentNode);
            }
        }
    }

    /**
     * if a codebook has a reference tag, it's referring to an item (possible with subitems) in another codebook
     * here, we attempt to find which element in which codebook is referred to and open that codebook and continue
     * from the referred element
     * @param element         the parent element
     * @param conceptElement  the child element
     * @param containsElement the referrence element
     * @param parentNode      the structure node
     */
    private void handleReferencedCodebook(Element element, Element conceptElement, Element containsElement, CodebookStructureNode parentNode){
        // Retrieve the element that is referred to from the other dataset and continue by parsing
        // that element and its sub-elements
        Element refElement = getRefOtherCodebookElement(containsElement);

        // a ZIB can apparently reference itself (e.g. nl.zorg.LaboratoriumUitslag)
        // to prevent an infinite loop, track whether this branch has visited this id before
        // if it hasn't, continue
        String id = element.getAttribute("id");
        if(!detectInfiniteReferencing.contains(id)) {
            detectInfiniteReferencing.add(id);
            createBook(refElement, parentNode);
            detectInfiniteReferencing.remove(id);
        }
        else{
            // if we're in an infinite reference loop, create a dummy leaf.
            CodebookItem codebookItem = createDummyLeafElement(conceptElement);
            addItem(codebookItem, parentNode);
        }
    }

    /**
     * if a non-leaf node is considered an item, we handle it by creating a dummy non-leaf node
     * and moving the actual item down one level, to ensure it's a leaf-node
     * @param conceptElement the child element
     * @param parentNode     the structure node
     */
    private void handleGroupIsAnItem(Element conceptElement, CodebookStructureNode parentNode){
        // create a dummy item for the group and addChild it
        CodebookItem dummyGroupItem = createDummyGroupElement(conceptElement);
        CodebookStructureNode newParent = addItem(dummyGroupItem, parentNode);

        // addChild the actual item to the dummy group
        CodebookItem itemItem = createItemElement(conceptElement);
        addItem(itemItem, newParent);

        createBook(conceptElement, newParent);
    }

    /**
     * attempts to find the reference to another codebook
     * @param element the element we're looking at
     * @return the element in the other codebook
     */
    private Element getRefOtherCodebookElement(Element element){
        String ref = element.getAttribute("ref");
        String datasetId = element.getAttribute("datasetId");

        // get the xml file for the referred dataset and the rootelement
        File xmlFile = getXMLFile(datasetId, language);
        Element rootElement = getRootElement(xmlFile);

        // use xpath to quickly find our element of interest and return it
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            String expression = "//concept[@id='" + ref + "']";
            return (Element) xPath.compile(expression).evaluate(rootElement, XPathConstants.NODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * addChild an item to the codebook and keep track of the codebook structure
     * @param codebookItem the item to addChild
     * @param parentNode the parent structure node
     * @return a new structure node which will serve as the new parent
     */
    private CodebookStructureNode addItem(CodebookItem codebookItem, CodebookStructureNode parentNode){
        // addChild the item to the map
        codebookItemMap.put(codebookItem.getId(), codebookItem);
        // create a new codebookstructure node and addChild at as a child to the parentnode
        CodebookStructureNode codebookStructureNode = new CodebookStructureNode(codebookItem);
        parentNode.addChild(codebookStructureNode);
        return codebookStructureNode;
    }

    /**
     * create a group item based on the conceptElement
     * @param conceptElement the conceptElement
     * @return the newly created CodebookItem
     */
    private CodebookItem createGroupConceptElement(Element conceptElement){
        String id = getUniqueId(getAttributeValue(conceptElement, "id"));
        String name = getElementValue(conceptElement, "name");
        String description = getElementValue(conceptElement, "desc");
        return new CodebookItem(id, name, description, "group");
    }

    /**
     * create a dummy group item based on the conceptElement
     * @param conceptElement the conceptElement
     * @return the newly created CodebookItem
     */
    private CodebookItem createDummyGroupElement(Element conceptElement){
        String id = getUniqueId("DummyId");
        String name = getElementValue(conceptElement, "name")+" <dummy group>";
        String description = "generated dummy";
        return new CodebookItem(id, name, description, "group");
    }

    /**
     * create a dummy leaf item based on the conceptElement
     * @param conceptElement the conceptElement
     * @return the newly created CodebookItem
     */
    private CodebookItem createDummyLeafElement(Element conceptElement){
        String id = getUniqueId(getAttributeValue(conceptElement, "id"));
        String name = getElementValue(conceptElement, "name")+" - Infinite";
        String description = getElementValue(conceptElement, "desc")+ " - Infinite";
        return new CodebookItem(id, name, description, "string", "item");
    }


    private void addTerminologyAssociations(Element conceptElement){
        List<Element> terminologyAssociations = getChildElementsWithName(conceptElement, "terminologyAssociation");

        for (Element terminologyAssociation : terminologyAssociations) {
            String terminologyConceptId = getAttributeValue(terminologyAssociation, "conceptId");
            terminologyAssociationMap.put(terminologyConceptId, new TerminologyElement(terminologyAssociation));
        }
    }

    /**
     * create an item based on the conceptElement
     * @param conceptElement the conceptElement
     * @return the newly created CodebookItem
     */
    private CodebookItem createItemElement(Element conceptElement){
        CodebookItem codebookItem;
        String id = getAttributeValue(conceptElement, "id");
        String uniqueid = getUniqueId(id);
        String name = getElementValue(conceptElement, "name");
        String description = getElementValue(conceptElement, "desc");
        String datatype = "";

        // add the information from the terminologyAssociation tags to the Map.
        addTerminologyAssociations(conceptElement);

        // eventueel attribute type ophalen
        Element valueDomainElement = getChildElementWithName(conceptElement, "valueDomain");
        if(valueDomainElement!=null) {
            datatype = getAttributeValue(valueDomainElement, "type");
        }

        // attempt to find the terminology for the concept
        if(terminologyAssociationMap.containsKey(id)) {
            TerminologyElement terminologyElement = terminologyAssociationMap.get(id);
            codebookItem = new CodebookItem(uniqueid, name, description, datatype, "item", terminologyElement.getCode(), terminologyElement.getCodeSystemName(), terminologyElement.getDisplayName());
        }
        else{
            // a concept which itself does not have terminology although it's codelist can still have items
            // which are e.g. SNOMED of course
            codebookItem = new CodebookItem(uniqueid, name, description, datatype, "item");
        }

        // attempt to add a codelist to the item
        addCodelist(codebookItem, conceptElement);

        return codebookItem;
    }


    /**
     * find out whether a valueDomain or valueSet element has concepts
     * @param element valueDomain or valueSet element
     * @return true/false
     */
    private boolean elementHasConcepts(Element element){
        // the element itself is null, so false
        if(element==null) return false;
        // next, there should be a conceptList Element
        Element conceptListElement = getChildElementWithName(element, "conceptList");
        // if it doesn't exist, also false
        if(conceptListElement==null) return false;
        // finally, if the conceptListElement has a concept child, return true
        return getChildElementWithName(conceptListElement, "concept") != null;
    }


    /**
     * adds a value set to a codebookItem, if one exists
     * @param codebookItem   the codebookItem to which to addChild the value set
     * @param conceptElement the xml codebookItem element in which to look for the valueset
     */
    private void addCodelist(CodebookItem codebookItem, Element conceptElement){
        Element valueDomainElement = getChildElementWithName(conceptElement, "valueDomain");
        Element valueSetElement = getChildElementWithName(conceptElement, "valueSet");

        if(elementHasConcepts(valueDomainElement) && elementHasConcepts(valueSetElement)){
            // so far I've only encountered this in the RIVM codebook and since that one seems
            // to do better with valueDomains, we'll use the valueDomain here
            // nevertheless, this may have to change in the future if other codebooks behave
            // differently
            addValueDomainBasedCodelist(codebookItem, valueDomainElement);
        }
        else if(elementHasConcepts(valueDomainElement)){
            addValueDomainBasedCodelist(codebookItem, valueDomainElement);
        }
        else if(elementHasConcepts(valueSetElement)){
            addValueSetBasedCodelist(codebookItem, valueSetElement);
        }
        codebookItem.resolveToGenerateCodeListItems();
    }

    private void addValueSetBasedCodelist(CodebookItem codebookItem, Element valueSetElement){
        try {
            Element conceptListElement = getChildElementWithName(valueSetElement, "conceptList");

            // elements are in the codebookItem tag, but may also be in the exception tag (NULLFlavors)
            addValueSet(codebookItem, getChildElementsWithName(conceptListElement, "concept"));
            addValueSet(codebookItem, getChildElementsWithName(conceptListElement, "exception"));
            codebookItem.sortCodeList();
        } catch(Exception e){
            // for some reason the codelist is broken
            codebookItem.setBrokenCodeList();
        }
    }

    /**
     * adds valueSet elements from a list to the codebookItem list
     * @param codebookItem  the codebookItem to which to addChild the value set
     * @param elements      list with elements from the valueSet
     */
    private void addValueSet(CodebookItem codebookItem, List<Element> elements) {
        // for all elements, look for the code, codebooksystem name and name
        for (Element entryElement : elements) {
            String valueCode = getAttributeValue(entryElement, "code");
            String textInLanguage = getElementValue(entryElement, "name");
            String codeSystemId = getAttributeValue(entryElement, "codeSystem");
            String diaplayName = getAttributeValue(entryElement, "displayName");

            // As far as I currenlty know, if the codeslists are based on the valueSet, the codesystem
            // can be found in the sourceCodeSystem tag, which is stored in the codeSystemMap
            // attempt to get the codesystem name from the map, otherwise use the id as the name instead
            String valueCodeSystemName = codeSystemMap.getOrDefault(codeSystemId, codeSystemId);

            // if there is no code for an item, keep track of it; we'll generate one later
            if(valueCode.equalsIgnoreCase("")){
                codebookItem.addToGenerateCodeListItem(textInLanguage);
            }
            else {
                codebookItem.addCodelistItem(valueCode, textInLanguage, valueCodeSystemName);
            }
        }
    }


    private void addValueDomainBasedCodelist(CodebookItem codebookItem, Element valuDomainElement){
        try {
            Element conceptListElement = getChildElementWithName(valuDomainElement, "conceptList");
            // elements are in the codebookItem tag, but may also be in the exception tag (NULLFlavors)
            addValueDomain(codebookItem, getChildElementsWithName(conceptListElement, "concept"));
            addValueDomain(codebookItem, getChildElementsWithName(conceptListElement, "exception"));
            codebookItem.sortCodeList();
        } catch(Exception e){
            // for some reason the codelist is broken
            codebookItem.setBrokenCodeList();
        }
    }


    /**
     * adds valueSet elements from a list to the codebookItem list
     * @param codebookItem  the codebookItem to which to addChild the value set
     * @param elements      list with elements from the valueSet
     */
    private void addValueDomain(CodebookItem codebookItem, List<Element> elements) {
        // for all elements, look for the code, codebooksystem name and name
        for (Element entryElement : elements) {
            String valueCode="";
            String valueCodeSystemName="";
            String diaplayName="";
            String id = getAttributeValue(entryElement, "id");
            String textInLanguage = getElementValue(entryElement, "name");

            // as far as I'm currently aware (RIVM) when we're dealing with valueDomain
            // based codelists, the codesystem etc should be in the terminologyAssociation tags, which
            // is available in the terminologyMap
            if(terminologyAssociationMap.containsKey(id)) {
                TerminologyElement terminologyElement = terminologyAssociationMap.get(id);
                valueCode = terminologyElement.getCode();
                valueCodeSystemName = terminologyElement.getCodeSystemName();

                diaplayName = terminologyElement.getDisplayName();

                // if the codesystem name is empty, we can try whether it is in the codesystem map
                if (valueCodeSystemName.equalsIgnoreCase("")) {
                    String codeSystemId = terminologyElement.getCodeSystem();
                    // attempt to retrieve the codesystem from the codesystem map. If it doesn't exist, set it to
                    // the identifier instead.
                    valueCodeSystemName = codeSystemMap.getOrDefault(codeSystemId, codeSystemId);
                }
            }

            // if there is no code for an item, keep track of it; we'll generate one later
            if(valueCode.equalsIgnoreCase("")){
                codebookItem.addToGenerateCodeListItem(textInLanguage);
            }
            else {
                codebookItem.addCodelistItem(valueCode, textInLanguage, valueCodeSystemName);
            }
        }
    }

    /**
     * given the huge differences between XML files, we'll try to create a map which can link
     * the codesystem identifier to the codesystem name
     * @param element
     */
    private void addCodeSystem(Element element){
        handleCodeSystems(element, "//sourceCodeSystem", "id", "identifierName");
//        handleCodeSystems(element, "//terminologyAssociation", "codeSystem", "codeSystemName");
    }


    private void handleCodeSystems(Element element, String expression, String idAttribute, String nameAttribute){
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList elementSet = (NodeList) xPath.compile(expression).evaluate(element, XPathConstants.NODESET);
            for(int i=0; i<elementSet.getLength(); i++){
                Element curElement = (Element) elementSet.item(i);
                codeSystemMap.put(getAttributeValue(curElement, idAttribute), getAttributeValue(curElement, nameAttribute));
            }
        } catch (Exception e){
            logger.log(Level.ERROR, "Unexpected issue while searching for codesystems in the XML file: "+e.getMessage());
        }
    }

    /**
     * returns a single codebook item from this codebook
     * @param itemName the name of the item for which to return the codebook item
     * @return the codebook item
     */
    CodebookItem getCodebookItem(String itemName){
        return codebookItemMap.get(itemName);
    }

    /**
     * return the tree structure
     * @return the root node of the codebook's tree structure
     */
    CodebookStructureNode getCodebookTree(){
        return root;
    }


    /**
     * Terminology objects, based on the terminologyAssociation Tag
     */
    class TerminologyElement{
        private String code="";
        private String codeSystem="";
        private String displayName="";
        private String codeSystemName="";

        TerminologyElement(Element element){
            generateTerminologyElement(element);
        }

        private void generateTerminologyElement(Element element){
            code = getAttributeValue(element, "code");
            codeSystem = getAttributeValue(element, "codeSystem");
            displayName = getAttributeValue(element, "displayName");
            codeSystemName = getAttributeValue(element, "codeSystemName");
        }

        public String getCode() {
            return code;
        }

        public String getCodeSystem() {
            return codeSystem;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getCodeSystemName() {
            return codeSystemName;
        }
    }
}

