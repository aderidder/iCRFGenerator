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
import icrfgenerator.edc.edc.edcrunsettings.emx.EMXRunSettings;
import icrfgenerator.edc.edc.edcspecificpane.EDCSpecificPane;
import icrfgenerator.edc.edc.edcspecificpane.EMXSpecificPane;
import icrfgenerator.resourcemanagement.ResourceManager;
import icrfgenerator.settings.runsettings.RunSettings;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * EMX
 */
public class EMXEDC extends EDCDefault{
    private Workbook workbook;

    public EMXEDC(){
        super("EMX");
    }

    /**
     * generates a new instance of this EDC's rightside pane and returns it
     * @return a new instance of this EDC's rightside pane
     */
    @Override
    public EDCSpecificPane generateRightSidePane(){
        return new EMXSpecificPane();
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
        EMXRunSettings runSettings = (EMXRunSettings) RunSettings.getInstance();
        CodebookManager codebookManager = CodebookManager.getInstance();

        Sheet attributesSheet = workbook.getSheet("attributes");
        Sheet dataSheet = workbook.getSheet("data");

        // get all the keys (codebook + datasetId + language)
        List<String> keys = runSettings.getKeys();
        for (String key : keys) {
            // retrieve the selected items for the key and add each item to the template
            List<String> selectedItemIds = runSettings.getSelectedItemIdentifiers(key);
            for (String itemId : selectedItemIds) {
                String dataType = runSettings.getSelectedItemDataType(key, itemId);

                // get the codebookItem to retrieve some information which is not dynamic
                // such as the item's name and its ontology values
                CodebookItem codebookItem = codebookManager.getCodebookItem(key, itemId);
                String itemName = codebookItem.getItemName();
                String itemDescription = codebookItem.getDescription();
                String ontologyCode = codebookItem.getCode();
                String nillable = Boolean.toString(runSettings.getSelectedItemNillableValue(key, itemId)).toUpperCase();
                String aggregateable = Boolean.toString(runSettings.getSelectedItemAggregateableValue(key, itemId)).toUpperCase();
                String minVal = runSettings.getSelectedItemMinValue(key,itemId);
                String maxVal = runSettings.getSelectedItemMaxValue(key,itemId);
                String refEntity = "";

                // should we do something with this?
                if(!ontologyCode.equalsIgnoreCase("")) {
                    String ontologyCodeSystem = codebookItem.getCodeSystem();
                    String ontologyDescription = codebookItem.getCodeDescription();
                }

                // add the codelist to the file if the item has one
                if (codebookManager.codebookItemHasCodeList(key, itemId)) {
                    List<String> selectedCodes = runSettings.getSelectedItemSelectedTerminologyCodes(key, itemId);
                    List<String> selectedValues = selectedCodes.stream().map(t->codebookManager.getValueForCode(key, itemId, t)).collect(Collectors.toList());
                    refEntity = addList(itemName, selectedValues);
                }

                // add the attributes
                addAttributeValues(attributesSheet, itemName, itemDescription, dataType, nillable, refEntity, aggregateable, minVal, maxVal);

                // add the itemName to the data sheet als column name
                addDataColumn(dataSheet, itemName);
            }
        }
    }

    /**
     * get an Excel filechooser filter
     * @return a filechooser filter for Excel files
     */
    @Override
    public FileChooser.ExtensionFilter getExtensionFilter() {
        return new FileChooser.ExtensionFilter("Excel files: (*.xlsx)", "*.xlsx");
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
     * add the
     * @param itemName
     * @return
     */
//    private String addList(String key, String itemId, String itemName, List<String> selectedCodes, CodebookManager codebookManager){
    private String addList(String itemName, List<String> selectedValues){
        String sheetName = "cat_"+itemName;
        // add the entry to the entities
        addEntityValues(sheetName);

        // create a worksheet for the list (category)
        Sheet sheet = createListSheet(sheetName);

        // add values for codes to this worksheet
        for(int i =1; i<=selectedValues.size();i++){
            Row row = sheet.createRow(i);
            row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(selectedValues.get(i-1));
        }

        return sheetName;
    }

    /**
     * create an empty sheet for storing list/catalogue information
     * @param sheetName name of the new sheet
     * @return the newly created sheet
     */
    private Sheet createListSheet(String sheetName){
        Sheet sheet = workbook.createSheet(sheetName);
        Row row = sheet.createRow(0);
        Cell cell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue("name");
        return sheet;
    }

    /**
     * add an entity value to the entity sheet
     * @param entityValue value to add to the sheet
     */
    private void addEntityValues(String entityValue){
        Sheet entitySheet = workbook.getSheet("entities");
        Row newRow = entitySheet.createRow(entitySheet.getLastRowNum()+1);
        setRowCellValue(newRow, EntitiesColumns.NAME.index, entityValue);
        setRowCellValue(newRow, EntitiesColumns.EXTENDS.index, "cat_template");
        setRowCellValue(newRow, EntitiesColumns.DESCRIPTION.index, entityValue);
    }

    /**
     * add the itemName as a data sheet column name
     * @param dataSheet the data sheet
     * @param itemName  column name to add
     */
    private void addDataColumn(Sheet dataSheet, String itemName){
        Row row = dataSheet.getRow(dataSheet.getLastRowNum());
        row.getCell(row.getLastCellNum(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(itemName);
    }

    /**
     * add values to the attributes sheet
     * we may want to change this if we know what to do with e.g. the ontology values
     * @param sheet         attributes sheet
     * @param itemName      name of the item
     * @param description   description of the item
     * @param dataType      data type of the item
     * @param nillable      whether the item is nillable
     * @param refEnitity    reference
     * @param aggregateable whether the item is aggregateable
     */
    private void addAttributeValues(Sheet sheet, String itemName, String description, String dataType, String nillable, String refEnitity, String aggregateable, String minVal, String maxVal){
        Row row = sheet.createRow(sheet.getLastRowNum()+1);
        setRowCellValue(row, AttributesColumns.NAME.index, itemName);
        setRowCellValue(row, AttributesColumns.ENTITY.index, "data");
        setRowCellValue(row, AttributesColumns.DATATYPE.index, dataType);
        setRowCellValue(row, AttributesColumns.IDATTRIBUTE.index, "FALSE");
        setRowCellValue(row, AttributesColumns.NILLABLE.index, nillable);
        setRowCellValue(row, AttributesColumns.DESCRIPTION.index, description);
        setRowCellValue(row, AttributesColumns.REFENTITY.index, refEnitity);
        setRowCellValue(row, AttributesColumns.AGGREGATEABLE.index, aggregateable);
        setRowCellValue(row, AttributesColumns.LABEL.index, itemName); //again?
        setRowCellValue(row, AttributesColumns.MINVAL.index, minVal);
        setRowCellValue(row, AttributesColumns.MAXVAL.index, maxVal);

    }

    /**
     * helper. Maybe move to some general excel manipulation class
     * add a value to the row at the specified index
     * @param row   row
     * @param index index
     * @param value value
     */
    private static void setRowCellValue(Row row, int index, String value){
        if(!value.equalsIgnoreCase("")) row.getCell(index, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(value);
    }

    /**
     * opens the template, which we will fill with what the user has selected
     */
    private void openTemplate(){
        try {
            workbook = WorkbookFactory.create(ResourceManager.getResourceTemplateInputStream("EMXTemplate.xlsx"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * enum for column names in the attributes sheet
     */
    private enum AttributesColumns{
        NAME (0),
        ENTITY (1),
        DATATYPE (2),
        IDATTRIBUTE (3),
        NILLABLE (4),
        DESCRIPTION (5),
        REFENTITY (6),
        AGGREGATEABLE (7),
        LABEL (8),
        MINVAL (9),
        MAXVAL (10);

        private final int index;

        AttributesColumns(int index){
            this.index = index;
        }
    }

    /**
     * enum for column names in the attributes sheet
     */
    private enum EntitiesColumns{
        NAME (0),
        EXTENDS (1),
        ABSRTACT (2),
        DESCRIPTION (3);

        private final int index;

        EntitiesColumns(int index){
            this.index = index;
        }
    }

}
