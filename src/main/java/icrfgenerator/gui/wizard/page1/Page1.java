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

package icrfgenerator.gui.wizard.page1;

import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.resourcemanagement.ResourceManager;
import icrfgenerator.settings.GlobalSettings;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.utils.GUIUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class Page1 extends WizardPane {
    // boolean property which we use to check whether we can proceed to the next page
    private static BooleanProperty mayNotProceed = new SimpleBooleanProperty();

    private RunSettings runSettings = RunSettings.getInstance();

    public Page1(int wizardWidth, int wizardHeight){
        this.setId("Page1");
        this.getStylesheets().clear();
        this.setPrefWidth(wizardWidth);
        this.setPrefHeight(wizardHeight);
        this.setHeaderText(I18N.getLanguageText("page1Title")+" "+runSettings.getEDC().getEDCName()+" CRF...");
        this.getStylesheets().add(ResourceManager.getResourceStyleSheet("style2.css"));
        createContent();
    }

    /**
     * reset things
     */
    public static void reset(){
        // nothing to do for page 1, but I'll keep it for consistency with the other wizard pages
    }

    /**
     * create the content of this page
     */
    private void createContent(){
        GridPane gridPane = GUIUtils.createGridPane();
        createCodebookCheckboxes(gridPane);
        // set the content
        this.setContent(new ScrollPane(gridPane));
        checkMayProceed();
    }

    /**
     * create checkboxes for all known codebooks
     * @param gridPane the pane to which the checkboxes are added
     */
    private void createCodebookCheckboxes(GridPane gridPane){
        int nrCols = 2;
        // retrieve all known codebooks and the codebooks that were selected
        List<String> codebookList = GlobalSettings.getCodebookNames();
        List<String> previouslySelectedCodebooksList = runSettings.getSelectedCodebooks();
        // add a checkbox for each known codebook
        for(int i = 0; i<codebookList.size(); i++){
            String codebook = codebookList.get(i);
            CheckBox checkBox = new CheckBox(codebook);

            // select it if previously selected
            if(previouslySelectedCodebooksList.contains(codebook)){
                checkBox.setSelected(true);
            }

            // add an event to it
            addCheckBoxEvent(checkBox);

            Hyperlink hyperlink = new Hyperlink("i");
            hyperlink.setStyle("-fx-padding: -4 0 -4 0");
            hyperlink.setOnAction(event -> {
                try {
                    Desktop.getDesktop().browse(new URI("https://decor.nictiz.nl/art-decor/decor-project--"+GlobalSettings.getCodebookPrefix(codebook)));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            });

            HBox hBox = new HBox(checkBox, hyperlink);
            hBox.setAlignment(Pos.BASELINE_LEFT);
            hBox.setSpacing(3);
            // add the checkbox to the gridpane
            gridPane.add(hBox,i%nrCols, i/nrCols);
        }
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
        if(runSettings.getSelectedCodebooks().size()>0){
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
