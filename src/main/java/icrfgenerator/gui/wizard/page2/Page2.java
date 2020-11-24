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

package icrfgenerator.gui.wizard.page2;

import icrfgenerator.codebook.CodebookManager;
import icrfgenerator.gui.MainWindow;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.resourcemanagement.ResourceManager;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.utils.GUIUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Page2 extends WizardPane {
    private static final Logger logger = LogManager.getLogger(MainWindow.class.getName());
    private static RunSettings runSettings;
    private static Map<String, Tab> tabMap = new HashMap<>();

    private CodebookManager codebookManager = CodebookManager.getInstance();

    // boolean property which we use to check whether we can proceed to the next page
    private static BooleanProperty mayNotProceed = new SimpleBooleanProperty();

    /**
     * reset page statics
     */
    public static void reset(){
        tabMap.clear();
        runSettings = RunSettings.getInstance();
    }

    public Page2(int wizardWidth, int wizardHeight){
        this.setId("Page2");
        this.getStylesheets().clear();
        this.setPrefWidth(wizardWidth);
        this.setPrefHeight(wizardHeight);
        this.getStylesheets().add(ResourceManager.getResourceStyleSheet("style2.css"));
    }


    /**
     * retrieves an existing tab or creates a new one if one doesn't exist
     * @param codebook name of the codebook
     * @return a tab for the codebook
     */
    private Tab getTab (String codebook){
        if(!tabMap.containsKey(codebook)){
            tabMap.put(codebook, createNewTab(codebook));
        }
        return tabMap.get(codebook);
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
        this.setContent(GUIUtils.createErrorPane(I18N.getLanguageText("page2ErrorPane")));
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
            Tab tab = getTab(codebook);
            newTabPane.getTabs().add(tab);
            // something weird happening with the redraw when reentering the page. Apparently I can prevent this
            // by selecting the tab
            newTabPane.getSelectionModel().select(tab);
        }
        // set the content
        this.setContent(newTabPane);

        // select the first tab
        newTabPane.getSelectionModel().selectFirst();

        showButtons();
        this.setHeaderText(I18N.getLanguageText("page2Title")+" "+runSettings.getEDC().getEDCName()+" CRF...");

        checkMayProceed();
    }


    /**
     * create some loading awareness for the user
     */
    private void setLoadingContent(){
        this.setHeaderText(I18N.getLanguageText("page2LoadingPageTitle"));
        this.setContent(GUIUtils.createLoadingPane(I18N.getLanguageText("page2LoadingTitle")));
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
        List<String> datasetIdentifiers = codebookManager.getDatasetIdentifiers(codebook);

        // for all datasets in the codebook
        for(String datasetIdentifier:datasetIdentifiers){
            // retrieve the version, name and date
            String datasetVersion = codebookManager.getDatasetVersion(codebook, datasetIdentifier);
            String datasetName = codebookManager.getDatasetName(codebook, datasetIdentifier);
            String datasetDate = codebookManager.getDatasetEffectiveDate(codebook, datasetIdentifier);

            // create a checkbox
            CheckBox checkBox = new CheckBox();
            if(datasetName.length()>60) {
                checkBox.setText(datasetName.substring(0,59)+"..."+" - "+datasetVersion+" ("+datasetDate+")");
                GUIUtils.addTooltip(checkBox, datasetName);
            }
            else{
                checkBox.setText(datasetName+" - "+datasetVersion+" ("+datasetDate+")");
            }

            checkBox.setId(datasetIdentifier);

            // and a language selected combobox
            CheckComboBox checkComboBox = createLanguageCheckComboBox(codebook, datasetIdentifier);

            // add the listeners
            addCheckComboBoxListener(codebook, datasetIdentifier, checkComboBox);
            addCheckboxListener(checkBox, checkComboBox, codebook, datasetIdentifier);

            // add them to the pane
            gridPane.add(checkBox, 1, rownum);
            gridPane.add(checkComboBox, 2, rownum);
            rownum++;
        }

        return gridPane;
    }

    /**
     * Add listener to the codebook datasetId selection checkbox
     * @param checkBox      codebook datasetId selection checkbox
     * @param checkComboBox language selection checkcombobox
     * @param codebook      name of the codebook
     * @param datasetId       datasetId of the codebook
     */
    private void addCheckboxListener(CheckBox checkBox, CheckComboBox checkComboBox, String codebook, String datasetId){
        checkBox.setOnAction(event -> {
            // if the codebook datasetId is selected, enable the language selection box and update the runsettings accordingly
            if(checkBox.isSelected()){
                checkComboBox.setDisable(false);
                runSettings.setDatasetSelectedLanguages(codebook, datasetId, checkComboBox.getCheckModel().getCheckedItems());
            }
            // if the codebook datasetId is selected, enable the language selection box and update the runsettings accordingly
            else{
                checkComboBox.setDisable(true);
                runSettings.clearDatasetSelectedLanguages(codebook, datasetId);
            }
            checkMayProceed();
        });
    }

    /**
     * Creates a single CheckComboBox with available languages for a codebook+version.
     * @param codebook the codebook
     * @param datasetIdentifier identifier of a dataset
     * @return a CheckComboBox with the languages available for this codebook
     */
    private CheckComboBox createLanguageCheckComboBox(String codebook, String datasetIdentifier){
        // retrieve languages and create the CheckComboBox
        List<String> languages = codebookManager.getLanguagesForCodebookDataset(codebook, datasetIdentifier);
        return GUIUtils.createCheckComboBox(languages);
    }

    /**
     * add a listener to a checkcombobox
     * @param codebook name of the codebook
     * @param datasetIdentifier dataset identifier
     * @param checkComboBox the checkcombobox to which to add the listener
     */
    private void addCheckComboBoxListener(String codebook, String datasetIdentifier, CheckComboBox<String> checkComboBox){
        // add a listener to the items in the CheckComboBox
        checkComboBox.getCheckModel().getCheckedItems().addListener(
            (ListChangeListener<String>) c -> {
                runSettings.setDatasetSelectedLanguages(codebook, datasetIdentifier, (List<String>) c.getList());
                checkMayProceed();
            }
        );
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
        if(runSettings.oneOrMoreSelectedDatasetsAndLanguages()){
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
