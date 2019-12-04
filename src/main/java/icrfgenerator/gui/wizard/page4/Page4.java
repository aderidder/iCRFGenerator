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

package icrfgenerator.gui.wizard.page4;

import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.settings.runsettings.RunSettings;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

public class Page4 extends WizardPane {

    public Page4(int wizardWidth, int wizardHeight){
        this.setId("Page4");
        this.getStylesheets().clear();
        this.setPrefWidth(wizardWidth);
        this.setPrefHeight(wizardHeight);
        this.setHeaderText(I18N.getLanguageText("page4Title"));
    }

    /**
     * create the content of this page
     */
    private void buildPage(){
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(new Text(RunSettings.getInstance().getSummary()));
        this.setContent(borderPane);
    }

    /**
     * things to do when we enter the page
     * @param wizard the wizard
     */
    @Override
    public void onEnteringPage(Wizard wizard) {
        buildPage();
        getButtonTypes().forEach(b->((Button)lookupButton(b)).setText(I18N.getLanguageText(b.getButtonData().name())));
    }
}
