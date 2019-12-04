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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.StageStyle;

/**
 * dialog for the "About"
 */
class AboutWindow extends Dialog{

    private static final double prefWidth = 675;
    private static final double prefHeight = 400;

    private AboutWindow(){
        createDialog();
    }

    static void showAbout(){
        Dialog aboutWindow = new AboutWindow();
        aboutWindow.showAndWait();
    }

    /**
     * create the dialog
     */
    private void createDialog(){
        setTitle(I18N.getLanguageText("aboutPageTitle"));
        getDialogPane().setPrefSize(prefWidth, prefHeight);
        initStyle(StageStyle.UTILITY);

        getDialogPane().getStylesheets().add(ResourceManager.getResourceStyleSheet("style.css"));
        getDialogPane().getStyleClass().add("fillBackground");

        // add an ok button
        getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        // generate the panes
        Node topPane = setupTopPane();
        Node centerPane = setupCenterPane();
        Node bottomPane = setupBottomPane();

        // add them to the borderpane
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topPane);
        borderPane.setCenter(centerPane);
        borderPane.setBottom(bottomPane);

        // set the content of the dialog
        getDialogPane().setContent(borderPane);
    }


    /**
     * create the toppane
     * @return the toppane
     */
    private Node setupTopPane(){
        Label sceneTitle = new Label(I18N.getLanguageText("aboutTitle"));
        sceneTitle.setId("title2");
        sceneTitle.setPadding(new Insets(5,0,5,10));
        return sceneTitle;
    }

    /**
     * create the centerpane
     * @return the centerpane
     */
    private Node setupCenterPane(){
        TextArea textArea = new TextArea(I18N.getLanguageText("aboutText"));
        textArea.setEditable(false);
        textArea.setPrefWidth(prefWidth);

        HBox hBox = new HBox();
        hBox.getStyleClass().add("fillBackground");
        hBox.getChildren().addAll(textArea);
        hBox.setAlignment(Pos.CENTER);

        return hBox;
    }

    /**
     * create a grid with logos
     * @return a node with the bottom pane's content
     */
    private Node setupBottomPane(){
        double f=0.70;
        int height=(int) Math.floor(60*f);
        int width= (int) Math.floor(70*f);

        GridPane grid = new GridPane();
        grid.setHgap(55);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(10,0,0,0));

        // first row
        ImageView vumcImv = getImageView(height,2*width,"vumc_white.png");
        grid.add(vumcImv,0,0,2,1);

        ImageView antonieImv = getImageView(height,2*width,"nki_white.png");
        grid.add(antonieImv,2,0,2,1);

        ImageView lygatureImv = getImageView(height,2*width,"lygature_white.png");
        grid.add(lygatureImv,4,0,2,1);

        ImageView kwfImv = getImageView(height,2*width,"kwf_white.png");
        grid.add(kwfImv,6,0,2,1);

        // second row...
        ImageView nictizImv = getImageView(height,2*width,"nictiz_white.png");
        grid.add(nictizImv,0,1,2,1);

        ImageView aumcImv = getImageView(height,5*width,"aumc_white.png");
        grid.add(aumcImv,2,1,5,1);

        ImageView bbmriImv = getImageView(height,2*width,"bbmri_white.png");
        grid.add(bbmriImv,6,1,2,1);

        // third row
        ImageView healthriImv = getImageView(height,3*width,"healthri_white_small.png");
        grid.add(healthriImv,0,2,3,1);

        // give the grid an id and add styleclass
        grid.setId("icons");
        grid.getStyleClass().add("fillBackgroundWhite");
        return grid;
    }

    /**
     * helper for creating an imageview
     * @param height heigth
     * @param width width
     * @param image string name of the image
     * @return imageview
     */
    private static ImageView getImageView(int height, int width, String image){
        ImageView imageView = new ImageView();
        imageView.setFitHeight(height);
        imageView.setFitWidth(width);
        imageView.setImage(ResourceManager.getResourceImage(image));
        return imageView;
    }
}
