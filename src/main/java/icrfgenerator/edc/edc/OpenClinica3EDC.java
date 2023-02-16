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
import icrfgenerator.types.OperatorType;
import icrfgenerator.utils.StringUtils;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final int nrCellsOnItemsSheet = ItemColumns.values().length;
    private static final int nrCellsOnSectionsSheet = SectionsColumns.values().length;

    private String curLanguage;

    private static final Logger logger = LogManager.getLogger(OpenClinica3EDC.class.getName());

    public OpenClinica3EDC(){
        super("LibreClinica / OpenClinica 3");
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
    public void generateCRF(List<String> keys, String language) {
        this.curLanguage = language;
        OpenClinica3RunSettings runSettings = (OpenClinica3RunSettings) RunSettings.getInstance();
        CodebookManager codebookManager = CodebookManager.getInstance();

        addSection();

        Sheet sheet = workbook.getSheet("Items");

        for (String key : keys) {
            // retrieve the selected items for the key and add each item to the template
            List<String> selectedItemIds = runSettings.getSelectedItemIdentifiers(key);
            for (String itemId : selectedItemIds) {
                // create rows
                if (codebookManager.codebookItemHasCodeList(key, itemId)) {
                    createTerminologyRow(sheet, key, itemId);
                } else {
                    createNonTerminologyRow(sheet, key, itemId);
                }
            }
        }
    }

    /**
     * create description field
     * @param key    key
     * @param itemId itemId
     * @return description field
     */
    private String generateOCDescription(String key, String itemId){
        CodebookItem codebookItem = CodebookManager.getInstance().getCodebookItem(key, itemId);
        String ontologyCode = codebookItem.getCodeForItem();

        String ocDescriptionString=codebookItem.getItemDescription()+" | ";
        if(!ontologyCode.equalsIgnoreCase("")) {
            String ontologyCodeSystem = codebookItem.getCodeSystemForItem();
            String ontologyDescription = codebookItem.getItemCodeDescription();
            ocDescriptionString += ontologyDescription + " ("+ontologyCodeSystem + ": " + ontologyCode+") | ";
        }
        return ocDescriptionString;
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

    /**
     * Fill the section sheet
     */
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
     */
    private void createTerminologyRow(Sheet sheet, String key, String itemId){
        Row row = createEmptyRow(sheet, nrCellsOnItemsSheet);
        addGeneralValues(row, key, itemId);
        addTerminologyValues(row, key, itemId);
    }

    /**
     * creata a row for an item which does not have terminology
     * @param sheet sheet to which to add the row
     * @param key key of the item
     */
    private void createNonTerminologyRow(Sheet sheet, String key, String itemId){
        Row row = createEmptyRow(sheet, nrCellsOnItemsSheet);
        addGeneralValues(row, key, itemId);
        addValidations(row, key, itemId);
    }

    private void addValidations(Row row, String key, String itemId){
        RunSettings runSettings = RunSettings.getInstance();
        String maxValue = runSettings.getSelectedItemMaxValue(key, itemId);
        OperatorType operatorTypeMaxValue = runSettings.getSelectedItemMaxCheckOperator(key, itemId);
        String minValue = runSettings.getSelectedItemMinValue(key, itemId);
        OperatorType operatorTypeMinValue = runSettings.getSelectedItemMinCheckOperator(key, itemId);
        boolean addMaxCheck = !maxValue.equalsIgnoreCase("") && !operatorTypeMaxValue.equals(OperatorType.NONE);
        boolean addMinCheck = !minValue.equalsIgnoreCase("") && !operatorTypeMinValue.equals(OperatorType.NONE);

        if(addMaxCheck && addMinCheck){
            row.getCell(ItemColumns.VALIDATION.index).setCellValue("func:range("+minValue+","+maxValue+")");
            String errorMessage = operatorTypeMinValue.getErrorMessage(curLanguage, minValue)+"; "+operatorTypeMaxValue.getErrorMessage(curLanguage, maxValue);
            row.getCell(ItemColumns.VALIDATION_ERROR_MESSAGE.index).setCellValue(errorMessage);
        }
        else if(addMaxCheck){
            row.getCell(ItemColumns.VALIDATION.index).setCellValue("func:"+ operatorTypeMaxValue.getTextLabel().toLowerCase()+"("+maxValue+")");
            row.getCell(ItemColumns.VALIDATION_ERROR_MESSAGE.index).setCellValue(operatorTypeMaxValue.getErrorMessage(curLanguage, maxValue));
        }
        else if(addMinCheck){
            row.getCell(ItemColumns.VALIDATION.index).setCellValue("func:"+ operatorTypeMinValue.getTextLabel().toLowerCase()+"("+minValue+")");
            row.getCell(ItemColumns.VALIDATION_ERROR_MESSAGE.index).setCellValue(operatorTypeMinValue.getErrorMessage(curLanguage, minValue));
        }
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
     */
    private void addGeneralValues(Row row, String key, String itemId){
        OpenClinica3RunSettings runSettings = (OpenClinica3RunSettings) RunSettings.getInstance();
        CodebookItem codebookItem = CodebookManager.getInstance().getCodebookItem(key, itemId);
        String itemName = codebookItem.getItemName();

        row.getCell(ItemColumns.ITEM_NAME.index).setCellValue(StringUtils.removeSpacesFromString(makeUnique(itemName)));
        row.getCell(ItemColumns.DESCRIPTION_LABEL.index).setCellValue(generateOCDescription(key, itemId));
        row.getCell(ItemColumns.SECTION_LABEL.index).setCellValue(sectionLabel);
        row.getCell(ItemColumns.LEFT_ITEM_TEXT.index).setCellValue(itemName);
        row.getCell(ItemColumns.RESPONSE_TYPE.index).setCellValue(runSettings.getSelectedItemFieldType(key, itemId));
        row.getCell(ItemColumns.DATA_TYPE.index).setCellValue(runSettings.getSelectedItemDataType(key, itemId));
        row.getCell(ItemColumns.UNITS.index).setCellValue(runSettings.getSelectedItemUnitsValue(key, itemId));
        row.getCell(ItemColumns.REQUIRED.index).setCellValue(runSettings.getSelectedItemRequiredValue(key, itemId)?"1":"");
    }

    /**
     * sets terminology cell values
     * @param row row
     */
    private void addTerminologyValues(Row row, String key, String itemId){
        OpenClinica3RunSettings runSettings = (OpenClinica3RunSettings) RunSettings.getInstance();
        CodebookManager codebookManager = CodebookManager.getInstance();
        CodebookItem codebookItem = codebookManager.getCodebookItem(key, itemId);

        String itemName = codebookItem.getItemName();
        String fieldType = runSettings.getSelectedItemFieldType(key, itemId);
        List<String> selectedCodes = runSettings.getSelectedItemSelectedTerminologyCodes(key, itemId);
//        String values = String.join(", ", selectedCodes);
//        String codes = selectedCodes.stream().map(t -> StringUtils.escapeCommas(codebookManager.getValueForOptionCode(key, itemId, t))).collect(Collectors.joining(", "));
        String codes = String.join(", ", selectedCodes);
        String values = selectedCodes.stream().map(t -> StringUtils.escapeCommas(codebookManager.getValueForOptionCode(key, itemId, t))).collect(Collectors.joining(", "));

        row.getCell(ItemColumns.RESPONSE_LABEL.index).setCellValue(fieldType+"_"+ StringUtils.removeSpacesFromString(itemName));
        row.getCell(ItemColumns.RESPONSE_OPTIONS_TEXT.index).setCellValue(checkLength(itemName, values));
        row.getCell(ItemColumns.RESPONSE_VALUES_OR_CALCULATIONS.index).setCellValue(checkLength(itemName, codes));
        if(OpenClinica3Definition.isFieldTypeWithDefault(fieldType)) {
            row.getCell(ItemColumns.DEFAULT_VALUE.index).setCellValue("(select)");
        }
    }

    /**
     * check whether the length of the string is within the allowed length in Excel
     * @param itemName item name
     * @param value    value to check
     * @return the string or a message that the value is too long
     */
    private static String checkLength(String itemName, String value){
        if(value.length()>32766){
            logger.log(Level.ERROR, itemName+" has codes/values that exceed Excel's maximum field length. Please select fewer options...");
            return "Too long, sorry...";
        }
        return value;
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
        SECTION_TITLE (1),
        SUBTITLE (2),
        INSTRUCTIONS (3),
        PAGE_NUMBER (4),
        PARENT_SECTION (5);

        private final int index;

        SectionsColumns(int index){
            this.index = index;
        }
    }

}
