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

package icrfgenerator.gui;

import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.resourcemanagement.ResourceManager;
import icrfgenerator.settings.GlobalSettings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;

import java.util.List;
import java.util.stream.Collectors;

/**
 * dialog for the Settings
 */
class SettingsWindow extends Dialog{
    private static final double prefWidth = 475;
    private static final double prefHeight = 200;

    private Label setLanguageLabel, readTimeoutLabel;
    private ComboBox <IdLanguage> languageComboBox;
    private TextField readTimeoutTextField;

    private SettingsWindow (){
        createDialog();
    }

    static void showSettings(){
        SettingsWindow settingsWindow = new SettingsWindow();
        settingsWindow.showAndWait();
    }

    /**
     * create the dialog
     */
    private void createDialog(){
        getDialogPane().setPrefSize(prefWidth, prefHeight);
        initStyle(StageStyle.UTILITY);

        getDialogPane().getStylesheets().add(ResourceManager.getResourceStyleSheet("style.css"));
        getDialogPane().getStyleClass().add("fillBackground");

        // add an ok button
        getDialogPane().getButtonTypes().add(ButtonType.OK);

        // set the content of the dialog
        getDialogPane().setContent(createPane());

        // set all the labels in the appropriate language
        updateI18N();
    }

    /**
     * update this page to a different language
     */
    private void updateI18N(){
        // update the page title to the new language title
        setTitle(I18N.getLanguageText("settingsPageTitle"));
        // update the labels in settings
        setLanguageLabel.setText(I18N.getLanguageText(setLanguageLabel.getId()));
        // update the textual representation of the languages in the combobox
        languageComboBox.getItems().forEach(t->t.updateLanguage());
        readTimeoutLabel.setText(I18N.getLanguageText(readTimeoutLabel.getId()));
    }

    /**
     * create a combobox to allow users to select a language
     */
    private void setupLanguageComboBox() {
        List<String> supportedLanguages = GlobalSettings.getUILanguages();
        List<IdLanguage> languageList = supportedLanguages.stream().map(t -> new IdLanguage(t)).collect(Collectors.toList());

        // create a ComboBox and add all the languages to it
        languageComboBox = new ComboBox<>();
        languageComboBox.getItems().addAll(languageList);
        // set a custom cell factory that will work with the IdLanguage objects
        // this will ensure the dropdown shows the language in the appropriate language and that we can
        // still access the id (which is the locale, e.g. "nl" or "en").
        languageComboBox.setCellFactory(t -> new LanguageListCell());
        // ensure the non-dropdown part of the ComboBox also correctly displays the language
        languageComboBox.setButtonCell(new LanguageListCell());

        // select the currently selected locale by checking at which index in the supportedLanguages the
        // current locale exists. This is identical to the index in the combobox
        languageComboBox.getSelectionModel().select(supportedLanguages.indexOf(I18N.getCurrentLocale()));
    }
    /**
     * create the centerpane
     * @return the centerpane
     */
    private Node createPane(){
        VBox vBox = new VBox();

        // language fields
        setLanguageLabel = new Label();
        setLanguageLabel.setPrefWidth(200);
        setLanguageLabel.setId("settingsSetLanguage");
        setupLanguageComboBox();

        // add a listener to register a change to the selected language
        addLanguageChangeListener();

        // read timeout fields
        readTimeoutLabel = new Label("timeout in seconds");
        readTimeoutLabel.setPrefWidth(200);
        readTimeoutLabel.setId("readTimeOut");

        readTimeoutTextField = new IntField(GlobalSettings.getCodebookReadTimeout()/1000);
        readTimeoutTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.equalsIgnoreCase("")) newValue = "0";
            GlobalSettings.setCodebookReadTimeout(Integer.parseInt(newValue));
        });


        vBox.getChildren().addAll(getHBox(setLanguageLabel, languageComboBox), getHBox(readTimeoutLabel, readTimeoutTextField));
        return vBox;
    }

    private HBox getHBox(Node ... nodes){
        HBox hBox = new HBox();
        hBox.setSpacing(5);
        hBox.getStyleClass().add("fillBackground");
        hBox.getChildren().addAll(nodes);
        hBox.setAlignment(Pos.TOP_LEFT);
        return hBox;
    }



    /**
     * Add listener to the language combobox
     */
    private void addLanguageChangeListener(){
        languageComboBox.setOnAction(event -> {
            I18N.setLocale(languageComboBox.getSelectionModel().getSelectedItem().getId());
            updateI18N();
        });
    }


    /**
     * class which we use to keep track of a language id and its textual representation
     * this is used by the language combobox.
     */
    private static class IdLanguage{
        private String id;
        private String language;

        IdLanguage(String id){
            this.id = id;
            updateLanguage();
        }

        // update the textual representation of the id, based on the currently used language
        void updateLanguage(){
            language = I18N.getLanguageText(id);
        }

        String getLanguage(){
            return language;
        }

        String getId(){
            return id;
        }
    }

    /**
     * customised listcell to ensure the language combobox shows the textual representation of the items
     */
    private static class LanguageListCell extends ListCell<IdLanguage> {
        @Override
        protected void updateItem(IdLanguage item, boolean empty) {
            // ensure the normal functionality takes place
            super.updateItem(item, empty);

            // update the text if necessary
            if (empty || item == null) {
                setText(null);
            }
            else {
                setText(item.getLanguage());
            }
        }
    }

    class IntField extends TextField {
        IntField(int initialValue) {
            setText(Integer.toString(initialValue));

            this.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {
                if (!"0123456789".contains(keyEvent.getCharacter())) {
                    keyEvent.consume();
                }
            });
        }
    }

}
