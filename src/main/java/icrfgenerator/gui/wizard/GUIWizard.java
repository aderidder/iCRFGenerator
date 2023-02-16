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

package icrfgenerator.gui.wizard;

import icrfgenerator.edc.edc.edcspecificpane.edccodelistpane.CodelistPane;
import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.gui.wizard.selectcodebooks.SelectCodebooksPage;
import icrfgenerator.gui.wizard.selectdatasets.SelectDatasetsPage;
import icrfgenerator.gui.wizard.selectitems.SelectItemsPage;
import icrfgenerator.gui.wizard.selectlanguages.SelectLanguagesPage;
import icrfgenerator.gui.wizard.summarypage.SummaryPage;
import javafx.scene.control.ButtonType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

/**
 * class that is responsible for creating the wizard
 */
public class GUIWizard {
    private static final Logger logger = LogManager.getLogger(GUIWizard.class.getName());
    private static final int wizardWidth = 700;
    private static final int wizardHeight = 500;

    private boolean createFile;

    public GUIWizard(){

    }

    /**
     * create and show the wizard
     * @return whether the wizard has resulted in a successful ending and a file has to be created
     */
    public boolean showWizard() {
        Wizard wizard = new Wizard();
        // create the pages
        WizardPane selectCodebooksPage = new SelectCodebooksPage(wizardWidth, wizardHeight);
        WizardPane selectDatasetsPage = new SelectDatasetsPage(wizardWidth, wizardHeight);
        WizardPane selectLanguagesPage = new SelectLanguagesPage(wizardWidth, wizardHeight);
        WizardPane selectItemsPage = new SelectItemsPage(wizardWidth+400, wizardHeight+200);
        WizardPane summaryPage = new SummaryPage(wizardWidth, wizardHeight);
        wizard.setFlow(new Wizard.LinearFlow(selectCodebooksPage, selectDatasetsPage, selectLanguagesPage, selectItemsPage, summaryPage));

        // show wizard and wait for response
        wizard.showAndWait().ifPresent(result -> {
            if (result == ButtonType.FINISH) {
                createFile = true;
            }
            else if(result == ButtonType.CANCEL) {
                logger.log(Level.INFO, I18N.getLanguageText("wizardCanceled"));
                createFile = false;
            }
        });
        return createFile;
    }

    /**
     * reset some static variables on the pages
     */
    public static void resetWizard(){
        SelectCodebooksPage.reset();
        SelectDatasetsPage.reset();
        SelectLanguagesPage.reset();
        SelectItemsPage.reset();
        CodelistPane.reset();
    }
}
