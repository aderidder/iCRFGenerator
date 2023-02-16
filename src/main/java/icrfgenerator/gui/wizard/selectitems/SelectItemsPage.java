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

package icrfgenerator.gui.wizard.selectitems;

import icrfgenerator.codebook.CodebookManager;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.resourcemanagement.ResourceManager;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.utils.GUIUtils;
import icrfgenerator.utils.KeyUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

import java.util.*;

public class SelectItemsPage extends WizardPane {
    private static final CodebookManager codebookManager = CodebookManager.getInstance();
    private static RunSettings runSettings;

    private static Map<String, Tab> tabMap;

    // boolean property which we use to check whether we can proceed to the next page
    private static BooleanProperty mayNotProceed = new SimpleBooleanProperty();

    /**
     * reset some statics
     */
    public static void reset(){
        tabMap = new HashMap<>();
        runSettings = RunSettings.getInstance();
    }

    public SelectItemsPage(int wizardWidth, int wizardHeight){
        this.setId("SelectItemsPage");
        this.getStylesheets().clear();
        this.setPrefWidth(wizardWidth);
        this.setPrefHeight(wizardHeight);
        this.getStylesheets().add(ResourceManager.getResourceStyleSheet("style2.css"));
    }

    /**
     * check whether or not the user may proceed to the next page
     */
    void checkMayProceed(){
        if(runSettings.oneOrMoreSelectedItems()){
            mayNotProceed.setValue(false);
        }
        else{
            mayNotProceed.setValue(true);
        }
    }

    /**
     * the top level tab is the codebook+datasetId tab
     * this tab contains one or more tab, one per selected language
     * each of these language tabs contains items
     * @param codebook codebook name
     * @param datasetId codebook datasetId
     * @return a tab for a codebook+datasetId
     */
    private Tab getTab(String codebook, String datasetId){
        Tab tab;
        String mainSimpleLanguage = runSettings.getMainSimpleLanguage();

        String key = KeyUtils.getSimpleLanguageKey(codebook, datasetId, mainSimpleLanguage);
            if(!tabMap.containsKey(key)){
                tabMap.put(key, new LanguageTab(codebook, datasetId, mainSimpleLanguage, this, generateTabTitle(codebook, datasetId)));
            }
            tab = tabMap.get(key);

        return tab;
    }

    /**
     * generates the title for the tab based on some variables
     * @param codebook  codebook
     * @param datasetId dataset id
     * @return the title
     */
    private String generateTabTitle (String codebook, String datasetId){
        String version = codebookManager.getDatasetVersion(codebook, datasetId);
        String datasetName = codebookManager.getDatasetName(codebook, datasetId);
        String title=datasetName;
        if(datasetName.length()>40) {
            title = datasetName.substring(0,37)+"...";
        }
        return title+" "+version+" ("+runSettings.getMainSimpleLanguage()+")";
    }

    /**
     * starts codebook loading in the background to prevent an unresponsive UI
     */
    private void startLoadingTask(){
        // the background task
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                runSettings.determineLanguageBasedIdentifiers();
                codebookManager.updateCodebookItems();
                return null;
            }
        };
        task.setOnSucceeded(event -> setSelectItemsContent());
        task.setOnFailed(event -> setFailedLoadingContent());
        new Thread(task).start();
    }

    /**
     * sets the content for when loading failed
     */
    private void setFailedLoadingContent(){
        this.setContent(GUIUtils.createErrorPane(I18N.getLanguageText("pageSelectItemsErrorPane")));
        showButtons();
        checkMayProceed();
    }

    /**
     * create the Select Items page once the codebooks have become available
     */
    private void setSelectItemsContent(){
        // retrieve the selected codebooks
        List<String> codebooks = runSettings.getSelectedCodebooks();
        TabPane newTabPane = new TabPane();

        // for each selected codebook
        for(String codebook:codebooks){
            // get the selected dataset identifiers
            List<String> datasetIds = runSettings.getCodebookSelectedDatasetIds(codebook);
            // and get/create a new tab for each one
            for(String datasetId:datasetIds) {
                Tab tab = getTab(codebook, datasetId);
                newTabPane.getTabs().add(tab);
            }
        }

        this.setContent(newTabPane);
        selectFirstTab(newTabPane);

        newTabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observableValue, Tab oldTab, Tab newTab) {
                ((LanguageTab)newTab).updatePropertiesDialogWindow();
            }
        });

        showButtons();
        this.setHeaderText(I18N.getLanguageText("pageSelectItemsTitle")+" "+runSettings.getEDC().getEDCName()+" CRF...");
        checkMayProceed();
    }

    /**
     * if we select e.g. the second tab, navigate to the summary and then
     * navigate back, the item selection right side will have visual issues
     * so we basically reselect the first tab and first language tab
     * @param newTabPane tab pane
     */
    private void selectFirstTab(TabPane newTabPane){
        newTabPane.getSelectionModel().selectLast();
        newTabPane.getSelectionModel().selectFirst();
    }

    /**
     * create some loading awareness for the user
     */
    private void setLoadingContent(){
        this.setHeaderText(I18N.getLanguageText("pageSelectItemsLoadingPageTitle"));
        this.setContent(GUIUtils.createLoadingPane(I18N.getLanguageText("pageSelectItemsLoadingTitle")));
    }

    /**
     * build the page
     */
    private void buildPage(){
        System.out.println("build page 3");
        hideButtons();
        setLoadingContent();
        startLoadingTask();
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
     * things to do when we enter the page
     * @param wizard the wizard
     */
    @Override
    public void onEnteringPage(Wizard wizard) {
        // bind the mayNotProceed boolean to the invalidProperty. When the wizard is invalid, it disables the next button
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
//        runSettings.printSelectedItems();
        PropertiesDialog.hideWindow();
    }

}
