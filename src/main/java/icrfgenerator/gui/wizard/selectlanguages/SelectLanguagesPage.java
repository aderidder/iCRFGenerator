package icrfgenerator.gui.wizard.selectlanguages;

import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.resourcemanagement.ResourceManager;
import icrfgenerator.settings.runsettings.RunSettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

import java.util.*;

/**
 * select languages page
 */
public class SelectLanguagesPage extends WizardPane {
    // boolean property which we use to check whether we can proceed to the next page
    private static final BooleanProperty mayNotProceed = new SimpleBooleanProperty();

    private static RunSettings runSettings = RunSettings.getInstance();

    public SelectLanguagesPage(int wizardWidth, int wizardHeight){
        this.setId("SelectLanguages");
        this.getStylesheets().clear();
        this.setPrefWidth(wizardWidth);
        this.setPrefHeight(wizardHeight);
        this.setHeaderText(I18N.getLanguageText("pageSelectLanguagesTitle"));
        this.getStylesheets().add(ResourceManager.getResourceStyleSheet("style2.css"));

    }

    /**
     * reset things
     */
    public static void reset(){
        runSettings = RunSettings.getInstance();
    }

    private void buildPage(){
        System.out.println("build SelectLanguagePage");
        createContent();
    }

    /**
     * create the content of this page
     */
    private void createContent(){
        HBox hBox = new HBox(createMainLanguageSelector(), createOtherLanguagesSelector());
        hBox.setSpacing(40);
        // set the content
        this.setContent(hBox);
        checkMayProceed();
    }

    /**
     * creates the part that allows a user to select the main language
     * @return a gridpane with radiobuttons
     */
    private Node createMainLanguageSelector(){
        GridPane gridPane = new GridPane();
        gridPane.add(new Label(I18N.getLanguageText("pageSelectLanguagesMainLanguage")),1,1);

        final ToggleGroup group = new ToggleGroup();
        final VBox vBox = new VBox();

        String curMainLanguage = runSettings.getMainSimpleLanguage();

        // the overlapping languages are based on the simple language names
        // however, we want the UI to show the descriptive names
        // fetch a (tree)map which uses descriptive --> simple and iterate the entries
        Map<String, String> overlappingDescriptiveToSimpleLangaugeMap = runSettings.getOverlappingDescriptiveToSimpleLanguageMap();

        for(Map.Entry<String, String> languageEntry:overlappingDescriptiveToSimpleLangaugeMap.entrySet()){
            String descriptiveName = languageEntry.getKey();
            String simpleName = languageEntry.getValue();
            RadioButton radioButton = new RadioButton(descriptiveName);
            radioButton.setId(simpleName);
            radioButton.setToggleGroup(group);
            if(simpleName.equalsIgnoreCase(curMainLanguage)){
                radioButton.setSelected(true);
            }
            radioButton.setOnAction(event->updateMainLanguage(radioButton));
            vBox.getChildren().add(radioButton);
        }

        if(group.getSelectedToggle()==null){
            runSettings.setMainSimpleLanguage("");
        }

        vBox.setSpacing(5);
        gridPane.add(vBox,1,2);
        return gridPane;
    }

    /**
     * when the main language is changed, inform the runsettings and check whether the user
     * may proceed to the next page
     * @param radioButton the radiobutton that fired the event
     */
    private void updateMainLanguage(RadioButton radioButton){
        runSettings.setMainSimpleLanguage(radioButton.getId());
        checkMayProceed();
    }

    /**
     * creates the part that allows a user to select the other languages
     * @return gridpane with checkboxes
     */
    private Node createOtherLanguagesSelector(){
        GridPane gridPane = new GridPane();
        gridPane.add(new Label(I18N.getLanguageText("pageSelectLanguagesOtherLanguages")),1,1);

        final VBox vBox = new VBox();

        List<String> curOtherLanguages = runSettings.getOtherSimpleLanguages();
        List<String> newOtherLanguages = new ArrayList<>();

        // the overlapping languages are based on the simple language names
        // however, we want the UI to show the descriptive names
        // fetch a (tree)map which uses descriptive --> simple and iterate the entries
        Map<String, String> overlappingDescriptiveToSimpleLanguageMap = runSettings.getOverlappingDescriptiveToSimpleLanguageMap();

        for(Map.Entry<String, String> languageEntry:overlappingDescriptiveToSimpleLanguageMap.entrySet()) {
            String descriptiveName = languageEntry.getKey();
            String simpleName = languageEntry.getValue();
            CheckBox checkBox = new CheckBox(descriptiveName);
            checkBox.setId(simpleName);

            if(curOtherLanguages.contains(simpleName)){
                newOtherLanguages.add(simpleName);
                checkBox.setSelected(true);
            }
            checkBox.setOnAction(event->updateOtherLanguage(checkBox));
            vBox.getChildren().add(checkBox);
        }

        runSettings.setOtherSimpleLanguage(newOtherLanguages);

        vBox.setSpacing(5);
        gridPane.add(vBox,1,2);
        return gridPane;
    }

    /**
     * when a checkbox is changed, update the runsettings
     * @param checkBox the checkbox which fired the event
     */
    private void updateOtherLanguage(CheckBox checkBox){
        if(checkBox.isSelected()){
            runSettings.addOtherSimpleLanguage(checkBox.getId());
        }
        else{
            runSettings.removeOtherSimpleLanguage(checkBox.getId());
        }
    }

    /**
     * check whether a user may proceed to the next page
     * this is based on whether there is at least one selected codebook
     */
    private void checkMayProceed(){
        mayNotProceed.setValue(runSettings.getMainSimpleLanguage().equalsIgnoreCase(""));
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
    }


}
