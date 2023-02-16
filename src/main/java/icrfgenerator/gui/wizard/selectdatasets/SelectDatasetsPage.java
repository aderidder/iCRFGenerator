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

package icrfgenerator.gui.wizard.selectdatasets;

import icrfgenerator.codebook.CodebookManager;
import icrfgenerator.gui.MainWindow;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.resourcemanagement.ResourceManager;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.utils.GUIUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectDatasetsPage extends WizardPane {
    private static final Logger logger = LogManager.getLogger(MainWindow.class.getName());
    private static final Map<String, Tab> tabMap = new HashMap<>();
    private static RunSettings runSettings;

    private final CodebookManager codebookManager = CodebookManager.getInstance();
    private final Label overlappingLanguagesLabel = new Label("Overlapping languages: ");

    // boolean property which we use to check whether we can proceed to the next page
    private static final BooleanProperty mayNotProceed = new SimpleBooleanProperty();

    /**
     * reset page statics
     */
    public static void reset(){
        tabMap.clear();
        runSettings = RunSettings.getInstance();
    }

    public SelectDatasetsPage(int wizardWidth, int wizardHeight){
        this.setId("SelectDatasets");
        this.getStylesheets().clear();
        this.setPrefWidth(wizardWidth);
        this.setPrefHeight(wizardHeight);
        this.getStylesheets().add(ResourceManager.getResourceStyleSheet("style2.css"));
    }

    /**
     * create a new tab for a codebook
     * @param codebook codebook name
     * @return new tab for codebook
     */
    private Tab createNewTab(String codebook){
        Tab tab = new Tab(codebook);
        tab.setId(codebook);
        tab.setClosable(false);

        GridPane gridPane = createVersionLanguagesGridPane(codebook);
        gridPane.setPrefWidth(580);
        tab.setContent(new ScrollPane(gridPane));
        return tab;
    }

    /**
     * starts codebook loading in the background to prevent an unresponsive UI
     */
    private void startLoadingTask(){
        // the background task
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                codebookManager.updateCodebooksMetadata();
                return null;
            }
        };
        task.setOnSucceeded(event -> setSelectCodebookVersionsContent());
        task.setOnFailed(event -> setFailedLoadingContent());
        new Thread(task).start();
    }

    /**
     * show a pane with an error message when loading fails due to e.g. a timeout
     */
    private void setFailedLoadingContent(){
        this.setContent(GUIUtils.createErrorPane(I18N.getLanguageText("pageSelectDatasetsErrorPane")));
        showButtons();
        checkMayProceed();
    }

    /**
     * create the Select Items page once the codebooks have become available
     */
    private void setSelectCodebookVersionsContent(){
        List<String> selectedCodebooks = runSettings.getSelectedCodebooks();
        // create a new tab pane. This pane will contain one tab per codebook
        // each tab will contain the codebook versions and a box to select languages per codebook version
        TabPane newTabPane = new TabPane();

        // retrieve existing tab / create a new tab for each selected codebook
        for(String codebook:selectedCodebooks){
            Tab tab = createNewTab(codebook);
            newTabPane.getTabs().add(tab);
            // something weird happening with the redraw when reentering the page. Apparently I can prevent this
            // by selecting the tab
            newTabPane.getSelectionModel().select(tab);
        }
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(newTabPane);

        updateOverlappingLanguagesLabel();
        borderPane.setBottom(overlappingLanguagesLabel);
        // set the content
        this.setContent(borderPane);

        // select the first tab
        newTabPane.getSelectionModel().selectFirst();

        showButtons();
        this.setHeaderText(I18N.getLanguageText("pageSelectDatasetsTitle"));

        checkMayProceed();
    }

    /**
     * create some loading awareness for the user
     */
    private void setLoadingContent(){
        this.setHeaderText(I18N.getLanguageText("pageSelectDatasetsLoadingPageTitle"));
        this.setContent(GUIUtils.createLoadingPane(I18N.getLanguageText("pageSelectDatasetsLoadingTitle")));
    }

    /**
     * build the page content
     */
    private void buildPage(){
        System.out.println("build page 2");
        hideButtons();
        setLoadingContent();
        startLoadingTask();
    }

    /**
     * Creates a grid pane for a single codebook, with the versions available
     * Pre-selects the versions for the codebook that were selected previously
     * @param codebook Name of the codebook
     * @return gridpane
     */
    private GridPane createVersionLanguagesGridPane(String codebook){
        int rownum = 1;
        GridPane gridPane = GUIUtils.createGridPane();
        gridPane.setPadding(new Insets(10,0,0,0));
        List<String> datasetIdentifiers = codebookManager.getDatasetIdentifiers(codebook);

        List<String> codebookSelectedDatasetIds = runSettings.getCodebookSelectedDatasetIds(codebook);

        // for all datasets in the codebook
        for(String datasetIdentifier:datasetIdentifiers){
            // retrieve the version, name and date
            String datasetVersion = codebookManager.getDatasetVersion(codebook, datasetIdentifier);
            String datasetName = codebookManager.getDatasetName(codebook, datasetIdentifier);
            String datasetDate = codebookManager.getDatasetEffectiveDate(codebook, datasetIdentifier);
            String dateString = datasetDate.equalsIgnoreCase("01-Jan-1900")?"":" ("+datasetDate+")";

            // create a checkbox
            CheckBox checkBox = new CheckBox();
            if(datasetName.length()>60) {
                checkBox.setText(datasetName.substring(0,59)+"..."+" - "+datasetVersion+dateString);
                GUIUtils.addTooltip(checkBox, datasetName);
            }
            else{
                checkBox.setText(datasetName+" - "+datasetVersion+dateString);
            }

            checkBox.setId(datasetIdentifier);

            addCheckboxListener(checkBox, codebook, datasetIdentifier);

            if(codebookSelectedDatasetIds.contains(datasetIdentifier)){
                checkBox.setSelected(true);
            }

            // add them to the pane
            gridPane.add(checkBox, 1, rownum, 3, 1);
            rownum++;
            List<String> simpleLanguagesList = codebookManager.getDatasetSimpleLanguages(codebook, datasetIdentifier);
            Collections.sort(simpleLanguagesList);
            gridPane.add(new Label(I18N.getLanguageText("pageSelectDatasetsLanguages")+" "+simpleLanguagesList), 3, rownum);
            rownum++;
        }

        return gridPane;
    }

    /**
     * add listener to the checkbox
     * @param checkBox  checkbox
     * @param codebook  codebook
     * @param datasetId id of the dataset
     */
    private void addCheckboxListener(CheckBox checkBox, String codebook, String datasetId){
        checkBox.setOnAction(event -> {
            // if the codebook datasetId is selected, enable the language selection box and update the runsettings accordingly
            if(checkBox.isSelected()){
                runSettings.addSelectedDataset(codebook, datasetId);
            }
            // if the codebook datasetId is selected, enable the language selection box and update the runsettings accordingly
            else{
                runSettings.removeSelectedDataset(codebook, datasetId);
            }
            updateOverlappingLanguagesLabel();
            checkMayProceed();
        });
    }

    /**
     * retrieve the overlapping languages for the selected codebooks and update the label to provide the information
     */
    private void updateOverlappingLanguagesLabel(){
        List<String> overlappingLanguages = runSettings.getOverlappingSimpleLanguages();
        Collections.sort(overlappingLanguages);

        if(!runSettings.anyDatasetsSelected()){
            overlappingLanguagesLabel.setText(I18N.getLanguageText("pageSelectDatasetsOverlappingLanguagesNone1"));
        }
        else if(overlappingLanguages.size()==0){
            overlappingLanguagesLabel.setText(I18N.getLanguageText("pageSelectDatasetsOverlappingLanguagesNone2"));
        }
        else {
            overlappingLanguagesLabel.setText(I18N.getLanguageText("pageSelectDatasetsOverlappingLanguages")+" "+ String.join(", ", overlappingLanguages));
        }
    }

    /**
     * hide all buttons in the wizard
     */
    private void hideButtons(){
        getButtonTypes().forEach(b->lookupButton(b).setVisible(false));
    }

    /**
     * show all buttons in the wizard
     */
    private void showButtons(){
        getButtonTypes().forEach(b->lookupButton(b).setVisible(true));
    }

    /**
     * Check whether the user may proceed to the next page
     */
    private void checkMayProceed(){
        // the user is allowed to proceed if at least one dataset + language is selected
        if(runSettings.anyDatasetsSelected() && runSettings.getOverlappingSimpleLanguages().size()>0){
            mayNotProceed.setValue(false);
        }
        else{
            mayNotProceed.setValue(true);
        }
    }

    /**
     * things to do when we enter the page
     * @param wizard the wizard
     */
    @Override
    public void onEnteringPage(Wizard wizard) {
        wizard.invalidProperty().bind(mayNotProceed);
        buildPage();
        getButtonTypes().forEach(b->((Button)lookupButton(b)).setText(I18N.getLanguageText(b.getButtonData().name())));
    }

    /**
     * things to do when we leave the page
     * @param wizard the wizard
     */
    @Override
    public void onExitingPage(Wizard wizard) {
        runSettings.printDatasetIdSelectedLanguages();
    }

}
