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

import icrfgenerator.codebook.CodebookItem;
import icrfgenerator.codebook.CodebookManager;
import icrfgenerator.edc.edc.edcdefinitions.OpenClinica3Definition;
import icrfgenerator.edc.edc.edcspecificpane.EDCSpecificPane;
import icrfgenerator.edc.edc.edcspecificpane.OpenClinica3SpecificPane;
import icrfgenerator.resourcemanagement.ResourceManager;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.edc.edc.edcrunsettings.openclinica3.OpenClinica3RunSettings;
import icrfgenerator.utils.StringUtils;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OpenClinica 3
 */
public class OpenClinica3EDC extends EDCDefault{
    private Workbook workbook;
    private static final String sectionLabel="Section1";
    private static final int nrCellsOnItemsSheet = 27;
    private static final int nrCellsOnSectionsSheet = 6;

    public OpenClinica3EDC(){
        super("OpenClinica 3");
    }

    /**
     * generates a new instance of this EDC's rightside pane and returns it
     * @return a new instance of this EDC's rightside pane
     */
    @Override
    public EDCSpecificPane generateRightSidePane(){
        return new OpenClinica3SpecificPane();
    }

    /**
     * EDC specific setup
     */
    @Override
    public void setupEDC(){
        openTemplate();
    }

    /**
     * add data to the template
     */
    @Override
    public void generateCRF() {
        OpenClinica3RunSettings runSettings = (OpenClinica3RunSettings) RunSettings.getInstance();
        CodebookManager codebookManager = CodebookManager.getInstance();

        addSection();

        Sheet sheet = workbook.getSheet("Items");

        // get all the keys (codebook + datasetId + language)
        List<String> keys = runSettings.getKeys();
        for (String key : keys) {
            // retrieve the selected items for the key and add each item to the template
            List<String> selectedItemIds = runSettings.getSelectedItemIdentifiers(key);
            for (String itemId : selectedItemIds) {
                String dataType = runSettings.getSelectedItemDataType(key, itemId);
                String fieldType = runSettings.getSelectedItemFieldType(key, itemId);

                // get the codebookItem to retrieve some information which is not dynamic
                // such as the item's name and its ontology values
                CodebookItem codebookItem = codebookManager.getCodebookItem(key, itemId);
                String itemName = codebookItem.getItemName();
                String ontologyCode = codebookItem.getCode();

                String ocDescriptionString=codebookItem.getDescription()+" | ";
                if(!ontologyCode.equalsIgnoreCase("")) {
                    String ontologyCodeSystem = codebookItem.getCodeSystem();
                    String ontologyDescription = codebookItem.getCodeDescription();
                    ocDescriptionString += ontologyDescription + " ("+ontologyCodeSystem + ": " + ontologyCode+") | ";
                }

                // check whether the selected item can have an associated codelist
                if (codebookManager.codebookItemHasCodeList(key, itemId)) {
                    List<String> selectedCodes = runSettings.getSelectedItemSelectedTerminologyCodes(key, itemId);
//                    createTerminologyRow(sheet, key, itemId, itemName, dataType, fieldType, String.join(", ", selectedCodes), selectedCodes.stream().map(t -> StringUtils.escapeCommas(codebookManager.getValueForCode(key, itemId, t))).collect(Collectors.joining(", ")));
                    createTerminologyRow(sheet, ocDescriptionString, itemId, itemName, dataType, fieldType, String.join(", ", selectedCodes), selectedCodes.stream().map(t -> StringUtils.escapeCommas(codebookManager.getValueForCode(key, itemId, t))).collect(Collectors.joining(", ")));
                } else {
//                    createNonTerminologyRow(sheet, key, itemId, itemName, dataType, fieldType);
                    createNonTerminologyRow(sheet, ocDescriptionString, itemId, itemName, dataType, fieldType);
                }
            }
        }
    }

    /**
     * get an Excel filechooser filter
     * @return a filechooser filter for Excel files
     */
    @Override
    public FileChooser.ExtensionFilter getExtensionFilter() {
        return new FileChooser.ExtensionFilter("Excel files: (*.xls)", "*.xls");
    }

    /**
     * write the filled template to a file
     * @param file name of the file to write to
     */
    @Override
    public void writeFile(File file) {
        try (FileOutputStream fileOut = new FileOutputStream(file)){
            workbook.write(fileOut);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addSection(){
        Sheet sheet = workbook.getSheet("Sections");
        Row row = createEmptyRow(sheet, nrCellsOnSectionsSheet);
        row.getCell(SectionsColumns.SECTION_LABEL.index).setCellValue(sectionLabel);
        row.getCell(SectionsColumns.SECTION_TITLE.index).setCellValue(sectionLabel);
    }

    /**
     * opens an OC3 template, which we will fill with what the user has selected
     */
    private void openTemplate(){
        try {
            workbook = WorkbookFactory.create(ResourceManager.getResourceTemplateInputStream("OC3Template.xls"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * creata a row for an item which has terminology
     * @param sheet sheet to which to add the row
     * @param key key of the item
     * @param itemName name of the item
     * @param dataType datatype of the item
     * @param fieldType fieldtype of the item
     * @param codes codes of the codelist
     * @param values values of the codelist
     */
    private void createTerminologyRow(Sheet sheet, String key, String itemId, String itemName, String dataType, String fieldType, String codes, String values){
        Row row = createEmptyRow(sheet, nrCellsOnItemsSheet);
        addGeneralValues(row, key, itemId, itemName, dataType, fieldType);
        addTerminologyValues(row, itemName, fieldType, codes, values);
    }

    /**
     * creata a row for an item which does not have terminology
     * @param sheet sheet to which to add the row
     * @param key key of the item
     * @param itemName name of the item
     * @param dataType datatype of the item
     * @param fieldType fieldtype of the item
     */
    private void createNonTerminologyRow(Sheet sheet, String key, String itemId, String itemName, String dataType, String fieldType){
        Row row = createEmptyRow(sheet, nrCellsOnItemsSheet);
        addGeneralValues(row, key, itemId, itemName, dataType, fieldType);
    }

    /**
     * creates a blank row
     * @param sheet sheet in which the row is created
     * @return a blank row
     */
    private Row createEmptyRow(Sheet sheet, int nrCells){
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        for(int i=0; i<nrCells; i++) {
            row.createCell(i);
        }
        return row;
    }

    /**
     * sets general cell values
     * @param row row
     * @param key key
     * @param itemName item name
     * @param dataType data type
     * @param fieldType field type
     */
    private void addGeneralValues(Row row, String key, String itemId, String itemName, String dataType, String fieldType){
        row.getCell(ItemColumns.ITEM_NAME.index).setCellValue(StringUtils.removeSpacesFromString(makeUnique(itemName)));
        row.getCell(ItemColumns.DESCRIPTION_LABEL.index).setCellValue(key+"itemId: "+itemId);
        row.getCell(ItemColumns.SECTION_LABEL.index).setCellValue(sectionLabel);
        row.getCell(ItemColumns.LEFT_ITEM_TEXT.index).setCellValue(itemName);
        row.getCell(ItemColumns.RESPONSE_TYPE.index).setCellValue(fieldType);
        row.getCell(ItemColumns.DATA_TYPE.index).setCellValue(dataType);
    }

    /**
     * sets terminology cell values
     * @param row row
     * @param itemName itemname
     * @param fieldType fieldtype
     * @param codes codes
     * @param values values
     */
    private void addTerminologyValues(Row row, String itemName, String fieldType, String codes, String values){
        row.getCell(ItemColumns.RESPONSE_LABEL.index).setCellValue(fieldType+"_"+ StringUtils.removeSpacesFromString(itemName));
        row.getCell(ItemColumns.RESPONSE_OPTIONS_TEXT.index).setCellValue(values);
        row.getCell(ItemColumns.RESPONSE_VALUES_OR_CALCULATIONS.index).setCellValue(codes);
        if(OpenClinica3Definition.isFieldTypeWithDefault(fieldType)) {
            row.getCell(ItemColumns.DEFAULT_VALUE.index).setCellValue("(select)");
        }
    }

    /**
     * enum for column names in OC3 and their indexes
     */
    private enum ItemColumns{
        ITEM_NAME (0),
        DESCRIPTION_LABEL (1),
        LEFT_ITEM_TEXT (2),
        UNITS (3),
        RIGHT_ITEM_TEXT (4),
        SECTION_LABEL (5),
        GROUP_LABEL (6),
        HEADER (7),
        SUBHEADER (8),
        PARENT_ITEM (9),
        COLUMN_NUMBER (10),
        PAGE_NUMBER (11),
        QUESTION_NUMBER (12),
        RESPONSE_TYPE (13),
        RESPONSE_LABEL (14),
        RESPONSE_OPTIONS_TEXT (15),
        RESPONSE_VALUES_OR_CALCULATIONS (16),
        RESPONSE_LAYOUT (17),
        DEFAULT_VALUE (18),
        DATA_TYPE (19),
        WIDTH_DECIMAL (20),
        VALIDATION (21),
        VALIDATION_ERROR_MESSAGE (22),
        PHI (23),
        REQUIRED (24),
        ITEM_DISPLAY_STATUS (25),
        SIMPLE_CONDITIONAL_DISPLAY (26);

        private final int index;

        ItemColumns(int index){
            this.index = index;
        }
    }

    private enum SectionsColumns{
        SECTION_LABEL (0),
        SECTION_TITLE (1);

        private final int index;

        SectionsColumns(int index){
            this.index = index;
        }
    }

}
