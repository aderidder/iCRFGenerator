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

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;

/**
 * TextAreaAppender, log4j. Uses the log4j2.xml for its parameters
 * name: name of the plugin
 * elementType: Name of the corresponding category of elements this plugin belongs under. In our case
 * the appender value is an Appender plugin, which is located in the Appender tag in the xml
 * printObject: set to true for appender plugin
 */
@Plugin(
    category = "Core",
    name = "TextAreaAppender",
    elementType = "appender",
    printObject = true
)
public class TextAreaAppender extends AbstractAppender {
    private static TextArea textArea;

    private TextAreaAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout);
    }

    /**
     * Factory method to construct the appender
     *
     * name : LogAreaLogger
     * layout : something like %d{yyyy-MM-dd HH:mm:ss} - %m%n
     * filter : allow Log Events to be evaluated to determine if or how they should be published.
     *
     * @param name   Name of appender
     * @param layout Layout of appender
     * @param filter Filter for appender
     * @return The newly created TextAreaAppender
     */
    @PluginFactory
    public static TextAreaAppender createAppender(@PluginAttribute("name") String name,
                                                  @PluginElement("Layout") Layout<? extends Serializable> layout,
                                                  @PluginElement("Filter") final Filter filter) {
        if (name == null) {
            LOGGER.error("No name provided for TextAreaAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new TextAreaAppender(name, filter, layout);
    }

    /**
     * Set TextArea for the appender
     *
     * @param textArea TextArea for the appender
     */
    public static void setTextArea(TextArea textArea) {
        TextAreaAppender.textArea = textArea;
    }

    /**
     * Appender receives a LogEvent and appends it to the textArea
     *
     * @param event Log event
     */
    @Override
    public void append(LogEvent event) {
        // format the message and transform it into a String
        final String message = new String(getLayout().toByteArray(event));

        // append log text to TextArea, run on the JavaFX Application Thread
        Platform.runLater(() -> {
            try {
                if (textArea != null) {
                    textArea.appendText(message);
                }
            } catch (Exception e) {
                System.err.println("Exception occurred while appending to the text area: " + e.getMessage());
            }
        });
    }
}