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

package icrfgenerator.utils;

import icrfgenerator.resourcemanagement.ResourceManager;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.controlsfx.control.CheckComboBox;

import java.util.List;

public class GUIUtils {

    /**
     * create a standard gridpane
     * @return gridpane
     */
    public static GridPane createGridPane(){
        GridPane gridPane = new GridPane();
        gridPane.setVgap(5);
        gridPane.setHgap(15);
        return gridPane;
    }

    /**
     * create a standard combobox
     * @param id id of the combobox
     * @param itemList content of the combobox
     * @return a new combobox
     */
    public static ComboBox<String> createComboBox(String id, List<String> itemList) {
        ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableList(itemList));
        comboBox.setId(id);
        return comboBox;
    }

    /**
     * create a standard checkcombobox
     * @param stringList list of items
     * @return new checkcombobox
     */
    public static CheckComboBox<String> createCheckComboBox(List<String> stringList){
        CheckComboBox<String> checkComboBox = new CheckComboBox<>(FXCollections.observableList(stringList));
        checkComboBox.setDisable(true);
        return checkComboBox;
    }

    public static Label createWrappedLabel(String labelText, int prefWidth){
        Label label = new Label(labelText);
        label.setWrapText(true);
        label.setPrefWidth(prefWidth);
        return label;
    }

    public static Text createWrappedText(String labelText, int prefWidth){
        Text label = new Text(labelText);
        label.setWrappingWidth(prefWidth);
        return label;
    }

    /**
     * add tooltip to an item
     * @param control item to which to add the tooltip
     * @param helpText the text to show in the tooltip
     */
    public static void addTooltip(Control control, String helpText){
        Tooltip tooltip = new Tooltip(helpText);
        control.setTooltip(tooltip);
    }

    /**
     * create a pane with a loading icon and a message
     * @param message the message to show
     * @return the pane
     */
    public static BorderPane createLoadingPane(String message){
        BorderPane borderPane = new BorderPane();
        ImageView imageView = new ImageView();
        imageView.setFitHeight(80);
        imageView.setFitWidth(80);
        imageView.setImage(ResourceManager.getResourceImage("loading.gif"));
        borderPane.setCenter(imageView);
        borderPane.setTop(new Label(message));
        return borderPane;
    }

    /**
     * create a pane with a loading icon and a message
     * @param message the message to show
     * @return the pane
     */
    public static BorderPane createErrorPane(String message){
        BorderPane borderPane = new BorderPane();
//        ImageView imageView = new ImageView();
//        imageView.setFitHeight(80);
//        imageView.setFitWidth(80);
//        imageView.setImage(ResourceManager.getResourceImage("loading.gif"));
//        borderPane.setCenter(imageView);
        borderPane.setTop(new Label(message));
        return borderPane;
    }
}
