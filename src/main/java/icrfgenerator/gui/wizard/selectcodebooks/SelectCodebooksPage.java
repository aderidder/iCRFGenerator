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

package icrfgenerator.gui.wizard.selectcodebooks;

import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.resourcemanagement.ResourceManager;
import icrfgenerator.settings.GlobalSettings;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.utils.GUIUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class SelectCodebooksPage extends WizardPane {
    private static final Logger logger = LogManager.getLogger(SelectCodebooksPage.class.getName());
    // boolean property which we use to check whether we can proceed to the next page
    private static final BooleanProperty mayNotProceed = new SimpleBooleanProperty();
    private static RunSettings runSettings;

    private List<CheckBox> checkboxList;

    public SelectCodebooksPage(int wizardWidth, int wizardHeight){
        this.setId("SelectCodebooks");
        this.getStylesheets().clear();
        this.setPrefWidth(wizardWidth);
        this.setPrefHeight(wizardHeight);
        this.setHeaderText(I18N.getLanguageText("pageSelectCodebooksTitle")+" "+runSettings.getEDC().getEDCName()+" CRF...");
        this.getStylesheets().add(ResourceManager.getResourceStyleSheet("style2.css"));
        createContent();
    }

    /**
     * reset things
     */
    public static void reset(){
        runSettings = RunSettings.getInstance();
    }

    /**
     * create the content of this page
     */
    private void createContent(){
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(createCodebookCheckboxes());
        borderPane.setBottom(createBottomButtons());
        // set the content
        this.setContent(borderPane);
        checkMayProceed();
    }

    /**
     * creates an hbox with search and clear buttons
     * @return hbox
     */
    private Node createBottomButtons(){
        Button clearButton = new Button(I18N.getLanguageText("pageSelectCodebooksClearSelection"));
        clearButton.setOnAction(e->clearSelection());
        Button searchButton = new Button(I18N.getLanguageText("pageSelectCodebooksSearchCodebooks"));
        searchButton.setOnAction(e->searchCodebooks());

        HBox hBox = new HBox(clearButton, searchButton);
        hBox.setSpacing(5);
        return hBox;
    }

    /**
     * clear all selected codebooks
     */
    private void clearSelection(){
        checkboxList.stream().filter(CheckBox::isSelected).forEach(e->{
            e.setSelected(false);
            runSettings.removeSelectedCodebook(e.getText());
        });
        checkMayProceed();
    }

    /**
     * open the codebook search page
     */
    private void searchCodebooks(){
        // show the searchpage and update the selection if necessary
        CodebooksSearchPage codebooksSearchPage = new CodebooksSearchPage();
        codebooksSearchPage.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> updateSelection(codebooksSearchPage.getSelectedCodebookNames()));

    }

    /**
     * update the codebook selection
     * @param codebookNames the new list of selected codebooks
     */
    private void updateSelection(List<String> codebookNames){
        clearSelection();
        checkboxList.stream().filter(e->codebookNames.contains(e.getText())).forEach(e->{
            e.setSelected(true);
            runSettings.addSelectedCodebook(e.getText());
        });
        checkMayProceed();
    }

    /**
     * create checkboxes for all known codebooks
     */
    private Node createCodebookCheckboxes(){
        checkboxList = new ArrayList<>();
        GridPane gridPane = GUIUtils.createGridPane();

        int nrCols = 1;
        // retrieve all known codebooks and the codebooks that were selected
        List<String> codebookList = GlobalSettings.getCodebookNames();
        List<String> previouslySelectedCodebooksList = runSettings.getSelectedCodebooks();
        // add a checkbox for each
        for(int i = 0; i<codebookList.size(); i++){
            String codebook = codebookList.get(i);
            CheckBox checkBox = new CheckBox(codebook);
            checkboxList.add(checkBox);

            // select it if previously selected
            if(previouslySelectedCodebooksList.contains(codebook)){
                checkBox.setSelected(true);
            }

            // add an event to it
            addCheckBoxEvent(checkBox);

            // create an HBox with the checkbox and a hyperlink which links to the source website
            HBox hBox = new HBox(checkBox, createHyperlink(codebook));
            hBox.setAlignment(Pos.BASELINE_LEFT);
            hBox.setSpacing(3);
            // add the checkbox to the gridpane
            gridPane.add(hBox, i%nrCols, i/nrCols);
        }
        // set the content
        return new ScrollPane(gridPane);
    }

    /**
     * create a clickable "i" which leads to the codebook online
     * @param codebook name of the codebook
     * @return the link
     */
    private Hyperlink createHyperlink(String codebook){
        Hyperlink hyperlink = new Hyperlink("i");
        hyperlink.setStyle("-fx-padding: -4 0 -4 0");
        hyperlink.setOnAction(event -> {
            try {
                Desktop.getDesktop().browse(new URI(GlobalSettings.getOnlineURLString(codebook)));
            } catch (IOException | URISyntaxException e) {
                logger.error("There was an issue browsing to the following URI: "+GlobalSettings.getOnlineURLString(codebook));
            }
        });
        return hyperlink;
    }

    /**
     * add an event to a checkbox when it is selected / deselected
     * @param checkBox the checkbox to which the event is added
     */
    private void addCheckBoxEvent(CheckBox checkBox){
        checkBox.setOnAction(event -> {
            if(checkBox.isSelected()){
                runSettings.addSelectedCodebook(checkBox.getText());
            }
            else{
                runSettings.removeSelectedCodebook(checkBox.getText());
            }
            checkMayProceed();
        });
    }

    /**
     * check whether a user may proceed to the next page
     * this is based on whether there is at least one selected codebook
     */
    private void checkMayProceed(){
        mayNotProceed.setValue(runSettings.getSelectedCodebooks().size() == 0);
    }

    /**
     * things to do when we enter the page
     * @param wizard the wizard
     */
    @Override
    public void onEnteringPage(Wizard wizard) {
        wizard.invalidProperty().bind(mayNotProceed);
        getButtonTypes().forEach(b->((Button)lookupButton(b)).setText(I18N.getLanguageText(b.getButtonData().name())));
    }

    /**
     * things to do when we leave the page
     * @param wizard the wizard
     */
    @Override
    public void onExitingPage(Wizard wizard) {
    }


}
