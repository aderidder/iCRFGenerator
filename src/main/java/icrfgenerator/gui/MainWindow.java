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

import icrfgenerator.edc.EDCFactory;
import icrfgenerator.edc.edc.EDC;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.resourcemanagement.ResourceManager;
import icrfgenerator.gui.wizard.GUIWizard;
import icrfgenerator.settings.GlobalSettings;
import icrfgenerator.settings.runsettings.RunSettings;
import icrfgenerator.utils.TextAreaAppender;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * class for the Main iCRFGenerator Window
 */
public class MainWindow {
    private static final int sceneWidth = 800;
    private static final int sceneHeight = 500;

    private static final Logger logger = LogManager.getLogger(MainWindow.class.getName());
    private TextArea logArea;
    private Hyperlink aboutHyperlink;
    private Hyperlink helpHyperlink;
    private Hyperlink settingsHyperlink;
    private Button buttonClear;
    private Button buttonExit;
    private Button buttonRun;
    private Label subTitle;
    private Label sceneTitle;
    private ComboBox <String> edcComboBox;

    private Stage primaryStage;

    /**
     * setup the main window
     * @param primaryStage primary stage
     */
    public void createMainWindow(Stage primaryStage) {
        // create the components
        Node topPane = setupTopPane();
        Node centerPane = setupCenterPane();
        Node bottomPane = setupBottomPane();

        // add them to a borderpane
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topPane);
        borderPane.setCenter(centerPane);
        borderPane.setBottom(bottomPane);

        // create the scene, set the scene
        Scene scene = new Scene(borderPane, sceneWidth, sceneHeight);
        primaryStage.setScene(scene);

        // create an icon
        primaryStage.getIcons().add(ResourceManager.getResourceImage("icon.png"));
        // add the stylesheet
        scene.getStylesheets().add(ResourceManager.getResourceStyleSheet("style.css"));
        primaryStage.setResizable(false);
        primaryStage.show();
        this.primaryStage = primaryStage;

        updateI18N();
    }

    /**
     * generate the top pane of the main window with all of its components
     * @return the top pane
     */
    private Node setupTopPane(){
        BorderPane borderPane = new BorderPane();

        // create first image for the left side
        ImageView healthriImv = new ImageView();
        healthriImv.setFitHeight(80);
        healthriImv.setFitWidth(200);
        healthriImv.setImage(ResourceManager.getResourceImage("healthri_transp.png"));

        HBox leftHBox = new HBox();
        leftHBox.setAlignment(Pos.CENTER_LEFT);

        // create the title
        sceneTitle = new Label();
        sceneTitle.setId("title");

        // create a subtitle
        subTitle = new Label();
        subTitle.setPadding(new Insets(-5,0,5,0));

        // setup the edc combobox
        createEDCComboBox();

        // add title, subtitle and edcComboBox to a vertical box and add the box to the Grid Pane
        VBox vBox = new VBox();
        vBox.getChildren().addAll(sceneTitle, subTitle, edcComboBox);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(0,0,15,0));

        // create an imageview for RIB image
        ImageView ribImv = new ImageView();
        ribImv.setFitHeight(90);
        ribImv.setFitWidth(90);
        ribImv.setImage(ResourceManager.getResourceImage("rib_new.png"));

        HBox rightHBox = new HBox();
        rightHBox.setPadding(new Insets(0,10,0,0));
        rightHBox.setAlignment(Pos.CENTER_RIGHT);

        // add items to the boxes and give them an equal preferred width for centering
        leftHBox.getChildren().addAll(healthriImv);
        rightHBox.getChildren().addAll(ribImv);
        leftHBox.setPrefWidth(215);
        rightHBox.setPrefWidth(215);

        // give the grid an id and add styleclass
        borderPane.setId("topPane");
        borderPane.getStyleClass().add("fillBackground");

        borderPane.setLeft(leftHBox);
        borderPane.setCenter(vBox);
        borderPane.setRight(rightHBox);
        return borderPane;
    }

    /**
     * create the EDC selection combobox
     */
    private void createEDCComboBox(){
        // create a combobox for edc selection
        edcComboBox = new ComboBox<>(FXCollections.observableList(GlobalSettings.getEdcList()));

        // a combobox basically operates as a button with content.
        // if there is no item selected, we'll set the button's text to our language specific text string
        // and otherwise it will display the text of the select item (one of the EDCs)
        edcComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                // ensure the normal functionality takes place
                super.updateItem(item, empty);
                // update the text if necessary
                if (empty || item == null) {
                    setText(I18N.getLanguageText("mainEDCBox"));

                } else {
                    setText(item);
                }
            }
        });
        edcComboBox.setPrefWidth(250);
        edcComboBox.setPromptText(I18N.getLanguageText("mainEDCBox"));

        edcComboBox.setOnAction(event -> {
            // since the default value is a value that is not an EDC and this default can also change due
            // to I18N, we'll add a check to prevent execution when the edcComboBox doesn't have a proper value
            if(edcComboBox.getSelectionModel().getSelectedIndex()>=0) {
                EDCFactory.generateEDC(edcComboBox.getSelectionModel().getSelectedItem());
                buttonRun.setDisable(false);
            }
            else{
                buttonRun.setDisable(true);
            }
            // when manipulating the combobox, reset the GUIWizard
            GUIWizard.resetWizard();
        });
    }

    /**
     * Create center pane which contains the log area
     *
     * @return the Node which will be added to the borderpane
     */
    private Node setupCenterPane(){
        // center pane currently contains only logarea, which we add to an hbox and return
        HBox hBox = new HBox();
        hBox.getStyleClass().add("fillBackground");
        createLogArea();
        hBox.getChildren().addAll(logArea);
        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }

    /**
     * create the log area
     */
    private void createLogArea(){
        logArea = new TextArea();
        logArea.setPadding(new Insets(5, 5, 5, 5));
        logArea.setPrefWidth(sceneWidth);
        logArea.setEditable(false);
        logArea.setId("logArea");
        logArea.setWrapText(true);
        TextAreaAppender.setTextArea(logArea);
    }

    /**
     * Create the bottom pane, which contains buttons to e.g. run and exit the program
     *
     * @return the Node which will be added to the borderpane
     */
    private Node setupBottomPane(){
        BorderPane borderPane = new BorderPane();

        HBox buttonBox = createButtonBox();
        HBox hyperlinkBox = createHyperlinkBox();

        // add a region with the same size as the hyperlink box to allow for easy centering
        // of the buttons
        Region padderRegion = new Region();
        padderRegion.prefWidthProperty().bind(hyperlinkBox.widthProperty());
        padderRegion.getStyleClass().add("fillBackground");

        // add everything to the borderpane
        borderPane.setCenter(buttonBox);
        borderPane.setRight(hyperlinkBox);
        borderPane.setLeft(padderRegion);

        return borderPane;
    }


    /**
     * creates an HBox and all its hyperlink elements
     * @return the HBox
     */
    private HBox createHyperlinkBox(){
        // Give Help and About their own hbox, which we align to the right
        HBox hyperlinkBox = new HBox();
        hyperlinkBox.getStyleClass().add("fillBackground");
        hyperlinkBox.setAlignment(Pos.CENTER_RIGHT);
        hyperlinkBox.setPadding(new Insets(0,15,0,0));

        aboutHyperlink = new Hyperlink();
        aboutHyperlink.getStyleClass().add("hyperlink");
        aboutHyperlink.setOnAction(event -> AboutWindow.showAbout());

        helpHyperlink = new Hyperlink();
        helpHyperlink.getStyleClass().add("hyperlink");
        helpHyperlink.setOnAction(event -> logArea.setText(I18N.getLanguageText("mainHelp")));

        settingsHyperlink = new Hyperlink();
        settingsHyperlink.getStyleClass().add("hyperlink");
        settingsHyperlink.setOnAction(event -> {
            SettingsWindow.showSettings();
            updateI18N();
        });

        // add to box and return it
        hyperlinkBox.getChildren().addAll(settingsHyperlink, helpHyperlink, aboutHyperlink);
        return hyperlinkBox;
    }

    /**
     * create a box for the bottom pane's buttons
     * @return the box with the buttons
     */
    private HBox createButtonBox(){
        HBox buttonBox = new HBox();
        buttonBox.getStyleClass().add("fillBackground");

        buttonBox.setPadding(new Insets(12, 0, 12, 0));
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER);

        buttonClear = new Button();
        buttonClear.setPrefSize(100, 20);
        buttonClear.setOnAction(event -> updateI18N());

        // add some buttons and tell what to do when the button is clicked
        buttonRun = new Button();
        buttonRun.setPrefSize(100, 20);
        buttonRun.setOnAction(event -> runProgram());
        buttonRun.setDisable(true);

        buttonExit = new Button();
        buttonExit.setPrefSize(100, 20);
        buttonExit.setOnAction(event -> System.exit(0));

        // add all the buttons to the button box
        buttonBox.getChildren().addAll(buttonClear, buttonRun, buttonExit);
        return buttonBox;
    }

    /**
     * update texts to the appropriate language
     */
    private void updateI18N(){
        aboutHyperlink.setText(I18N.getLanguageText("mainAboutLink"));
        helpHyperlink.setText(I18N.getLanguageText("mainHelpLink"));
        settingsHyperlink.setText(I18N.getLanguageText("mainSettingsLink"));
        buttonClear.setText(I18N.getLanguageText("mainClearButton"));
        buttonRun.setText(I18N.getLanguageText("mainRunButton"));
        buttonExit.setText(I18N.getLanguageText("mainExitButton"));
        logArea.setText(I18N.getLanguageText("mainWelcome"));
        sceneTitle.setText(I18N.getLanguageText("mainTitle"));
        subTitle.setText(I18N.getLanguageText("mainSubtitle"));
        primaryStage.setTitle(I18N.getLanguageText("mainPageTitle"));

        // check whether an item is selected in the edcComboBox.
        // if so, we can set the prompt text
        // if not, clear the selection, which will trigger the action event
        if(edcComboBox.getSelectionModel().isEmpty()){
            edcComboBox.setPromptText(I18N.getLanguageText("mainEDCBox"));
        }
        else {
            edcComboBox.getSelectionModel().clearSelection();
        }

        buttonRun.setDisable(true);
    }


    /**
     * start the wizard
     */
    private void runProgram(){
        logArea.clear();
        try {
            GUIWizard guiWizard = new GUIWizard();
            // if the wizard was canceled, createFile is false, otherwise it was
            // properly finished and it is true
            boolean createFile = guiWizard.showWizard();

            // if true, start a thread for the file saving.
            if (createFile){
                new Thread(new RunTask()).start();
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, "Something went terribly wrong... "+e.getMessage());
        }
    }


    private class RunTask extends Task {
        RunTask(){

        }

        /**
         * process for saving the CRF(s)
         * @return null
         */
        @Override
        public Void call() {
            try {
                // fetch which EDC was used, inialise the necessary stuff for the EDC and generate the CRF
                EDC edc = RunSettings.getInstance().getEDC();

                // start a filechosser to allow a user to save the file
                Platform.runLater(() -> {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.getExtensionFilters().add(edc.getExtensionFilter());
                    // show save file dialog
                    File file = fileChooser.showSaveDialog(primaryStage);

                    if (file != null) {
                        edc.generateCRFs(file);
                        logger.log(Level.INFO, I18N.getLanguageText("mainDone"));
                    }
                    else{
                        logger.log(Level.INFO, I18N.getLanguageText("mainCancelSave"));
                    }

                });

            } catch (Exception e){
                logger.log(Level.INFO, "A fatal error occurred:\n"+e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }
}
