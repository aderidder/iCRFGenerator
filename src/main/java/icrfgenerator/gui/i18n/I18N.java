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

package icrfgenerator.gui.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Class for internationlisation
 */
public class I18N {
    private static ResourceBundle resourceBundle;

    /**
     * initialise with English
     */
    static {
        setLocale("en");
        initBundle();
    }

    /**
     * set the appropriate resource bundle
     */
    private static void initBundle(){
        resourceBundle = ResourceBundle.getBundle("i18n.localisation");
    }

    /**
     * retrieve the textual representation of the identifier from the resource bundle
     * @param id the id for which to retrieve the text
     * @return text for the id
     */
    public static String getLanguageText(String id){
        return resourceBundle.getString(id);
    }

    /**
     * returns the currently used language
     * @return the currently used language
     */
    public static String getCurrentLocale(){
        return Locale.getDefault().getLanguage();
    }

    /**
     * change the language to a new one
     * @param countryCode code for the country
     */
    public static void setLocale(String countryCode){
        Locale.setDefault(new Locale(countryCode));
        initBundle();
    }
}
