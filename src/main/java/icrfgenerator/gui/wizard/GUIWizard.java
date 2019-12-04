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

import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.gui.wizard.page1.Page1;
import icrfgenerator.gui.wizard.page2.Page2;
import icrfgenerator.gui.wizard.page3.Page3;
import icrfgenerator.gui.wizard.page4.Page4;
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
        WizardPane page1 = new Page1(wizardWidth, wizardHeight);
        WizardPane page2 = new Page2(wizardWidth, wizardHeight);
        WizardPane page3 = new Page3(wizardWidth+400, wizardHeight+200);
        WizardPane page4 = new Page4(wizardWidth, wizardHeight);
        wizard.setFlow(new Wizard.LinearFlow(page1, page2, page3, page4));

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
        Page1.reset();
        Page2.reset();
        Page3.reset();
    }
}
