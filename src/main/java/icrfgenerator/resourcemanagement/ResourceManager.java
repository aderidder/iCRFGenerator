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

package icrfgenerator.resourcemanagement;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.net.URL;

/**
 * class for frontend resource management
 */
public class ResourceManager {
    private static final String templatesLocation = "/templates/";
    private static final String cssLocation = "/css/";
    private static final String imagesLocation = "/images/";

    /**
     * returns the requested resource as an inputstream
     * @param resource    the requested resource
     * @return the resource as an inputstream
     */
    private static InputStream getResourceAsStream(String resource) {
        return ResourceManager.class.getResourceAsStream(resource);
    }

    /**
     * returns the requested resource as a URL
     * @param resource    the requested resource
     * @return the resource as a URL
     */
    private static URL getResourceURL(String resource){
        return ResourceManager.class.getResource(resource);
    }

    /**
     * returns the requested image as an Image. Looks in resources/images/ for the requested image
     * @param imageName    the requested image's name
     * @return the image as an Image
     */
    public static Image getResourceImage(String imageName){
        return new Image(getResourceAsStream(imagesLocation+imageName));
    }

    /**
     * returns the requested stylesheet. Looks in resources/css/ for the requested stylesheet
     * @param styleSheetName    the requested stylesheet's name
     * @return a string representation of the stylesheet's location
     */
    public static String getResourceStyleSheet(String styleSheetName){
        return getResourceURL(cssLocation+styleSheetName).toExternalForm();
    }

    /**
     * returns a template located in the resource template directory as an inputstream
     * @param templateName name of the template to look for
     * @return template inputstream
     */
    public static InputStream getResourceTemplateInputStream(String templateName){
        return getResourceAsStream(templatesLocation+templateName);
    }

}
