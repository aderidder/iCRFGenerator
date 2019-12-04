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

package icrfgenerator.edc.edc;

import icrfgenerator.edc.edc.edcspecificpane.EDCSpecificPane;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Interface
 * Stuff each EDC should be able to do to be compatible
 */
public interface EDC {
    /**
     * add data to a crf
     */
    void generateCRF();

    /**
     * write the crf to disk
     * @param file name of the output file
     */
    void writeFile(File file);

    /**
     * do setup things for the edc, such as opening a template
     */
    void setup();

    /**
     * returns the EDC's right side pane for the GUI
     * @return the EDC's right side pane for the GUI
     */
    EDCSpecificPane generateRightSidePane();

    /**
     * returns the name of the EDC
     * @return the name of the EDC
     */
    String getEDCName();

    /**
     * returns the ExtensionFilter used for the FileChooser to save a file
     * @return the ExtensionFilter used for the FileChooser to save a file
     */
    FileChooser.ExtensionFilter getExtensionFilter();
}
