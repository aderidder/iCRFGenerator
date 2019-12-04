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

package icrfgenerator.edc;

import icrfgenerator.edc.edc.edcrunsettings.castor.CastorRunSettings;
import icrfgenerator.edc.edc.edcrunsettings.openclinica3.OpenClinica3RunSettings;
import icrfgenerator.edc.edc.edcrunsettings.redcap.REDCapRunSettings;

/**
 * create the appropriate runsettings
 */
public class EDCFactory {
    public static void generateEDC(String value){
        switch (value){
            case "OpenClinica 3":
                new OpenClinica3RunSettings();
                break;
            case "REDCap":
                new REDCapRunSettings();
                break;
            case "Castor":
                new CastorRunSettings();
                break;
            default: System.err.println("not found: "+value);
        }
    }
}
