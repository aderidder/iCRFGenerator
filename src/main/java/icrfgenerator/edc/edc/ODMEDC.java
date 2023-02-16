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

import icrfgenerator.codebook.CodebookManager;
import icrfgenerator.types.OperatorType;
import icrfgenerator.edc.edc.edcrunsettings.odm.ODMRunSettings;
import icrfgenerator.edc.edc.edcspecificpane.EDCSpecificPane;
import icrfgenerator.edc.edc.edcspecificpane.ODMSpecificPane;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.utils.KeyUtils;
import icrfgenerator.utils.XMLUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javafx.stage.FileChooser;

import static icrfgenerator.utils.StringUtils.prepareValueForXML;

/**
 * ODM-XML
 * Online validator: <a href="https://www.biyeescitech.com/B1/ODMValidator.aspx">...</a>
 */
public class ODMEDC extends EDCDefault {
    private StringBuilder fullXML;
    private String dateCreated;
    private final Map<String, Integer> idMap = setupIdMap();
    private final Map<String, String> measurementUnitMap = new HashMap<>();

    private ODMRunSettings runSettings;
    private CodebookManager codebookManager;

    private static Map<String, Integer> setupIdMap(){
        Map <String, Integer> idMap = new HashMap<>();
        idMap.put("CodeListID", 0);
        idMap.put("MeasurementUnitID", 0);
        idMap.put("ItemGroupID", 0);
        idMap.put("ItemID", 0);
        idMap.put("MedaDataVersionID", 0);
        idMap.put("StudyEventID", 0);
        idMap.put("FormID", 0);
        idMap.put("UserID", 0);
        return idMap;
    }

    static String generateUUID() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    public ODMEDC() {
        super("ODM-XML");
    }

    /**
     * generates the CRF and writes it to a file
     * @param file base filename
     */
    @Override
    public void generateCRFs(File file){
        runSettings = (ODMRunSettings) RunSettings.getInstance();
        codebookManager = CodebookManager.getInstance();
        buildXML();
        writeFile(file);
    }

    /**
     * generates a new instance of this EDC's rightside pane and returns it
     * @return a new instance of this EDC's rightside pane
     */
    @Override
    public EDCSpecificPane generateRightSidePane() {
        return new ODMSpecificPane();
    }

    /**
     * EDC specific setup
     */
    @Override
    public void setupEDC() {

    }

    /**
     * generate the CRF based on what the user selected
     */
    @Override
    public void generateCRF(List<String> keys, String language) {

    }

    /**
     * get an xml filechooser filter
     * @return a filechooser filter for xml files
     */
    @Override
    public FileChooser.ExtensionFilter getExtensionFilter() {
        return new FileChooser.ExtensionFilter("XML files: (*.xml)", "*.xml");
    }

    /**
     * write the filled template to a file
     * @param file file to write to
     */
    @Override
    public void writeFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(XMLUtils.formatXMLString(fullXML.toString(), 2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Store date + time
     */
    private void setDateCreated() {
        // yyyy-MM-ddTHH:mm:ss
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        dateCreated = dtf.format(now).replace(" ", "T"); //e.g. 2016-11-16 12:08:43
    }

    /**
     * generate the next id for a type to ensure their uniqueness
     * @param type type of id, e.g. ItemID, ItemGroupID, etc.
     * @return the unique id
     */
    private String getNextId(String type){
        int val =  idMap.get(type)+1;
        idMap.put(type, val);
        return type+val;
    }

    /**
     * build the ODM-XML
     */
    private void buildXML() {
        setDateCreated();
        String studyOID = "iCRFGenerator_"+generateUUID();
        String fileOID = studyOID+"_"+dateCreated;
        fullXML = new StringBuilder();

        // An element or element group can be followed by
        // a ? (meaning optional)
        // a * (meaning zero or more occurrences)
        // a + (meaning one or more occurrences)

        appendValues(fullXML,
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>",
                "<ODM xmlns=\"http://www.cdisc.org/ns/odm/v1.3\" ",
                "xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" ",
                "FileOID=\"", fileOID, "\" ",
                "CreationDateTime=\"", dateCreated, "\" ",
                "ODMVersion=\"1.3.1\">",
                "<Study OID=\"", studyOID, "\"></Study>"
        );
        // get the globalsection, which contains the study's name etc.
        fullXML.append(getGlobalSectionXML());

        // get the metadatasection, which sets some values necessary for the basic definitions
        String metadataSection = getMetaDataSectionXML();

        // get the BasicDefinition section and append it
        fullXML.append(getBasicDefinitionsSectionXML());

        // append the metadataSection
        fullXML.append(metadataSection);
        fullXML.append("</ODM>");

    }

    /**
     * Contains the study's name, description and protocol name.
     * Currently hardcoded, since we don't have a page which provides this
     * @return a String with the aforementioned values
     */
    private String getGlobalSectionXML(){
        // We don't have a page for these types of descriptions yet for any of the EDCs.
        // StudyName: This is a short external name for the study
        // StudyDescription: This is a free-text description of the study
        // ProtocolName: This is the sponsor's internal name for the protocol.
        String studyName = prepareValueForXML("Your Study Name");
        String studyDescription = prepareValueForXML("Your Study Description. File generated by the iCRF Generator.");
        return "<StudyName>" + studyName +"</StudyName>" +
               "<StudyDescription>" + studyDescription+"</StudyDescription>" +
               "<ProtocolName>" + studyName +"</ProtocolName>";
    }

    /**
     * returns the BasicDefinitions section, which contains the measurement units
     * @return the BasicDefinitions section
     */
    private String getBasicDefinitionsSectionXML(){
        StringBuilder basicDefinitionsBuilder = new StringBuilder();
        if(measurementUnitMap.size()>0){
            String mainLanguage = runSettings.getMainSimpleLanguage();
            basicDefinitionsBuilder.append("<BasicDefinitions>");
            for(Map.Entry<String, String> unitsEntry:measurementUnitMap.entrySet()){
                appendValues(basicDefinitionsBuilder,
                        "<MeasurementUnit OID=\"", unitsEntry.getValue(), "\" Name=\"", unitsEntry.getKey()+"\">",
                            "<Symbol>",
                                "<TranslatedText xml:lang=\"",mainLanguage, "\">", unitsEntry.getKey(), "</TranslatedText>",
                            "</Symbol>",
                        "</MeasurementUnit>");
            }
            basicDefinitionsBuilder.append("</BasicDefinitions>");
        }
        return basicDefinitionsBuilder.toString();
    }

    /**
     * Helper function to use the StringBuilder's append function and still keep the rest of the code readable
     * @param stringBuilder the stringBuilder
     * @param values        values to append
     */
    private void appendValues(StringBuilder stringBuilder, String ... values){
        Arrays.stream(values).forEach(stringBuilder::append);
    }

    /**
     * get / generate the identifier for a measurement unit
     * @param measurementUnit the measurement unit
     * @return identifier
     */
    private String getMeasurementUnitId(String measurementUnit){
        if(!measurementUnitMap.containsKey(measurementUnit)){
            measurementUnitMap.put(measurementUnit, getNextId("MeasurementUnitID"));
        }
        return measurementUnitMap.get(measurementUnit);
    }

    /**
     * The main part of the XML generation
     * @return XML-string
     */
    private String getMetaDataSectionXML(){
        String formName = "iCRFGenerator_Form";
        String studyEventOID = getNextId("StudyEventID");
        String formOID = getNextId("FormID");
        String itemGroupOID = getNextId("ItemGroupID");
        String metaDataVersionID = getNextId("MedaDataVersionID");

        return "<MetaDataVersion OID=\"" + metaDataVersionID + "\" Name=\"Metadataversion\">" +
                "<Protocol>" +
                "<StudyEventRef OrderNumber=\"1\" Mandatory=\"Yes\" StudyEventOID=\"" + studyEventOID + "\"/>" +
                "</Protocol>" +
                "<StudyEventDef OID=\"" + studyEventOID + "\" Name=\"ODM\" Repeating=\"No\" Type=\"Unscheduled\">" +
                "<FormRef OrderNumber=\"1\" Mandatory=\"No\" FormOID=\"" + formOID + "\"/>" +
                "</StudyEventDef>" +
                "<FormDef OID=\"" + formOID + "\" Name=\"" + formName + "\" Repeating=\"No\">" +
                "<ItemGroupRef ItemGroupOID=\"" + itemGroupOID + "\" OrderNumber=\"1\" Mandatory=\"Yes\"/>" +
                "</FormDef>" +
                getItemXML(itemGroupOID) +
                "</MetaDataVersion>";
    }

    /**
     * generate the item-part of the XML
     * @param itemGroupOID id of the itemgroup
     * @return item-part of the XML
     */
    private String getItemXML(String itemGroupOID){
        StringBuilder itemXML = new StringBuilder();

        StringBuilder tmpItemGroupDefBuilder = new StringBuilder();
        StringBuilder tmpItemDefBuilder = new StringBuilder();
        StringBuilder tmpCodeListBuilder = new StringBuilder();

        List<String> uniqueLanguages = runSettings.getAllLanguages();

        // Future plans:
        // We currently don't support actual item groups. This means we can't give the group a name, nor can we actually group items in a group
        // If we want to support this, we will need to add a new page to the Wizard, which will be complicated
        // For now we'll just add "IG" as a name for every language
        String itemGroupName = "IG";
        appendValues(tmpItemGroupDefBuilder,
                "<ItemGroupDef OID=\"", itemGroupOID, "\" Name=\"", itemGroupName,"\" Repeating=\"No\">",
                    "<Description>"
        );
        for (String language:uniqueLanguages) {
            appendValues(tmpItemGroupDefBuilder,
                    "<TranslatedText xml:lang=\"", language, "\">", itemGroupName, "</TranslatedText>"
            );
        }
        tmpItemGroupDefBuilder.append("</Description>");

        // items
        List<String> selectedCodebooks = runSettings.getSelectedCodebooks();
        for(String codebook:selectedCodebooks){
            List<String> datasetIds = runSettings.getCodebookSelectedDatasetIds(codebook);
            for(String datasetId:datasetIds){
                String mainLanguageId = KeyUtils.getKey(codebook, datasetId, runSettings.getMainSimpleLanguage());
                String codebookDatasetKey = KeyUtils.getKey(codebook, datasetId);

                List<String> selectedItemIds = runSettings.getSelectedItemIdentifiers(mainLanguageId);
                // add items
                for (String mainLanguageItemId : selectedItemIds) {
                    addItemXML(mainLanguageId, mainLanguageItemId, codebookDatasetKey, tmpItemGroupDefBuilder, tmpItemDefBuilder, tmpCodeListBuilder);
                }
            }
        }
        tmpItemGroupDefBuilder.append("</ItemGroupDef>");

        itemXML.append(tmpItemGroupDefBuilder).append(tmpItemDefBuilder).append(tmpCodeListBuilder);

        return itemXML.toString();
    }

    /**
     * add range checks and units ref
     * @param mainLanguageId     id of the main language
     * @param mainLanguageItemId item's id
     * @param tmpItemDefBuilder  item StringBuilder
     */
    private void addRangeAndUnits(String mainLanguageId, String mainLanguageItemId, StringBuilder tmpItemDefBuilder){
        String units = runSettings.getSelectedItemUnitsValue(mainLanguageId, mainLanguageItemId);
        String min = runSettings.getSelectedItemMinValue(mainLanguageId, mainLanguageItemId);
        OperatorType minOperator = runSettings.getSelectedItemMinCheckOperator(mainLanguageId, mainLanguageItemId);
        String max = runSettings.getSelectedItemMaxValue(mainLanguageId, mainLanguageItemId);
        OperatorType maxOperator = runSettings.getSelectedItemMaxCheckOperator(mainLanguageId, mainLanguageItemId);

        // range checks require both an operator and a value
        if(!min.equalsIgnoreCase("") && !minOperator.equals(OperatorType.NONE)){
            addRangeCheck(min, minOperator, units, tmpItemDefBuilder);
        }
        if(!max.equalsIgnoreCase("")&& !maxOperator.equals(OperatorType.NONE)){
            addRangeCheck(max, maxOperator, units, tmpItemDefBuilder);
        }
        if(min.equalsIgnoreCase("") && max.equalsIgnoreCase("") && !units.equalsIgnoreCase("")){
            addMeasurementUnitRef(tmpItemDefBuilder, units);
        }
    }

    /**
     * add measurement unit reference tag
     * @param tmpItemDefBuilder item StringBuilder
     * @param units             units
     */
    private void addMeasurementUnitRef(StringBuilder tmpItemDefBuilder, String units){
        appendValues(tmpItemDefBuilder,
                "<MeasurementUnitRef MeasurementUnitOID=\"", getMeasurementUnitId(units), "\"/>"
        );
    }

    /**
     * add range check
     * @param checkValue        value to check
     * @param operatorType      operator to use for the check
     * @param units             units
     * @param tmpItemDefBuilder item StringBuilder
     */
    private void addRangeCheck(String checkValue, OperatorType operatorType, String units, StringBuilder tmpItemDefBuilder){
        // http://xml4pharmaserver.com/XML4PharmaWiki/doku.php?id=using_rangechecks_in_odm_1.3

        appendValues(tmpItemDefBuilder,
                "<RangeCheck Comparator=\"", operatorType.getTextLabel(), "\" SoftHard=\"Soft\">",
                    "<CheckValue>", checkValue, "</CheckValue>"
                );
        if(!units.equalsIgnoreCase("")){
            addMeasurementUnitRef(tmpItemDefBuilder, units);
        }
        List<String> allLanguages = runSettings.getAllLanguages();
        for(String language:allLanguages) {

            appendValues(tmpItemDefBuilder,
                    "<ErrorMessage>",
                    "<TranslatedText xml:lang=\""+language+"\">", operatorType.getErrorMessage(language, checkValue), "</TranslatedText>",
                    "</ErrorMessage>"
            );
        }
        appendValues(tmpItemDefBuilder,
                "</RangeCheck>"
        );
    }

    /**
     * add question
     * @param tmpItemDefBuilder  item StringBuilder
     * @param codebookDatasetKey codebook - dataset key
     * @param mainLanguageItemId item's id
     */
    private void addQuestion(StringBuilder tmpItemDefBuilder, String codebookDatasetKey, String mainLanguageItemId){
        tmpItemDefBuilder.append("<Question>");
        addItemLanguageTranslations(tmpItemDefBuilder, codebookDatasetKey, mainLanguageItemId);
        tmpItemDefBuilder.append("</Question>");
    }

    /**
     * adding the item...
     * @param mainLanguageId         id of the main language
     * @param mainLanguageItemId     item's id
     * @param codebookDatasetKey     codebook - dataset key
     * @param tmpItemGroupDefBuilder itemGroup StringBuilder
     * @param tmpItemDefBuilder      item StringBuilder
     * @param tmpCodeListBuilder     codelist StringBuilder
     */
    private void addItemXML(String mainLanguageId, String mainLanguageItemId, String codebookDatasetKey, StringBuilder tmpItemGroupDefBuilder, StringBuilder tmpItemDefBuilder, StringBuilder tmpCodeListBuilder){
        String itemOID = getNextId("ItemID");

        // Important: the DataType attributes of the referenced CodeList and the containing ItemDef must be the same
        String dataType = runSettings.getSelectedItemDataType(mainLanguageId, mainLanguageItemId);
        String mandatory = runSettings.getSelectedItemRequiredValue(mainLanguageId, mainLanguageItemId)?"Yes":"No";
        String itemName = prepareValueForXML(codebookManager.getItemName(mainLanguageId, mainLanguageItemId));

        // add item information to the ItemGroupDef part
        appendValues(tmpItemGroupDefBuilder,
                "<ItemRef OrderNumber=\"1\" Mandatory=\"", mandatory, "\" ItemOID=\"", itemOID, "\"/>"
        );

        // add item information to the ItemDef part
        // ItemDef (Description?, Question?, ExternalQuestion?, MeasurementUnitRef*, RangeCheck*, CodeListRef?, Role* Deprecated, Alias*)
        appendValues(tmpItemDefBuilder,
                "<ItemDef OID=\"", itemOID, "\" Name=\"", itemName, "\" DataType=\"", dataType, "\">"
        );
        addQuestion(tmpItemDefBuilder, codebookDatasetKey, mainLanguageItemId);
        addRangeAndUnits(mainLanguageId, mainLanguageItemId, tmpItemDefBuilder);
        addCodeList(tmpItemDefBuilder, tmpCodeListBuilder, mainLanguageId, mainLanguageItemId, codebookDatasetKey, itemName, dataType);
        addItemAlias(tmpItemDefBuilder, mainLanguageId, mainLanguageItemId);
        tmpItemDefBuilder.append("</ItemDef>");
    }

    /**
     * add item alias tag for item
     * @param tmpItemDefBuilder  string builder
     * @param mainLanguageId     id of the main language
     * @param mainLanguageItemId item's id
     */
    private void addItemAlias(StringBuilder tmpItemDefBuilder, String mainLanguageId, String mainLanguageItemId){
        String itemCode = codebookManager.getCodeForItem(mainLanguageId, mainLanguageItemId);
        if(!itemCode.equalsIgnoreCase("")) {
            String itemCodeSystem = codebookManager.getCodeSystemForItem(mainLanguageId, mainLanguageItemId);
            appendValues(tmpItemDefBuilder,
                    "<Alias Context=\"", itemCodeSystem, "\" Name=\"", itemCode, "\"/>"
            );
        }
    }

    /**
     * add codelist for an item
     * @param tmpItemDefBuilder  item StringBuilder
     * @param tmpCodeListBuilder codelist StringBuilder
     * @param mainLanguageId     id of the main language
     * @param mainLanguageItemId item's id
     * @param codebookDatasetKey codebook - dataset key
     * @param itemName           name of the item
     * @param dataType           datatype of the item
     */
    private void addCodeList(StringBuilder tmpItemDefBuilder, StringBuilder tmpCodeListBuilder, String mainLanguageId, String mainLanguageItemId, String codebookDatasetKey, String itemName, String dataType){
        if (codebookManager.codebookItemHasCodeList(mainLanguageId, mainLanguageItemId)) {
            String codeListOID = getNextId("CodeListID");

            // add information to the ItemDef section
            appendValues(tmpItemDefBuilder,
                    "<CodeListRef CodeListOID=\"", codeListOID, "\"/>"
            );

            // add information to the CodeList section
            appendValues(tmpCodeListBuilder,
                    "<CodeList OID=\"", codeListOID, "\" Name=\"", itemName, "\" DataType=\"", dataType, "\">"
            );

            List<String> selectedCodes = runSettings.getSelectedItemSelectedTerminologyCodes(mainLanguageId, mainLanguageItemId);
            for (String code : selectedCodes) {
                String codesystem = codebookManager.getCodesystemForOptionCode(mainLanguageId, mainLanguageItemId, code);

                // An Alias provides an additional name for an element. The Context attribute specifies the application
                // domain in which this additional name is relevant.
                appendValues(tmpCodeListBuilder,
                        "<CodeListItem CodedValue=\"", code, "\">",
                            "<Decode>"
                );

                addOptionsLanguageTranslations(tmpCodeListBuilder, codebookDatasetKey, mainLanguageItemId, code);

                appendValues(tmpCodeListBuilder,
                            "</Decode>",
                            "<Alias Context=\"", codesystem, "\" Name=\"", code, "\"/>",
                            "</CodeListItem>"
                );
            }
            tmpCodeListBuilder.append("</CodeList>");
        }
    }

    /**
     * add item in other languages
     * @param tmpItemDefBuilder  item StringBuilder
     * @param codebookDatasetKey codebook - dataset key
     * @param mainLanguageItemId item's id
     */
    private void addItemLanguageTranslations(StringBuilder tmpItemDefBuilder, String codebookDatasetKey, String mainLanguageItemId){
        List<String> allLanguage = runSettings.getAllLanguages();
        for(String language:allLanguage){
            // get the full key for a different language
            // retrieve the item name using this key and the item's id
            String key = KeyUtils.getKey(codebookDatasetKey, language);
            tmpItemDefBuilder
                    .append("<TranslatedText xml:lang=\"").append(language).append("\">")
                    .append(prepareValueForXML(codebookManager.getItemName(key, mainLanguageItemId)))
                    .append("</TranslatedText>");
        }
    }

    /**
     * add options in other languages
     * @param tmpCodeListBuilder codelist StringBuilder
     * @param codebookDatasetKey codebook - dataset key
     * @param mainLanguageItemId item's id
     * @param code               option's code
     */
    private void addOptionsLanguageTranslations(StringBuilder tmpCodeListBuilder, String codebookDatasetKey, String mainLanguageItemId, String code){
        List<String> allLanguage = runSettings.getAllLanguages();
        for(String language:allLanguage){
            // get the full key for a different language
            // retrieve the appropriate option using this key, the item's id and the option's code
            String key = KeyUtils.getKey(codebookDatasetKey, language);
            appendValues(tmpCodeListBuilder,
                    "<TranslatedText xml:lang=\"", language, "\">",
                    prepareValueForXML(codebookManager.getValueForOptionCode(key, mainLanguageItemId, code)),
                    "</TranslatedText>"
            );
        }
    }
}
