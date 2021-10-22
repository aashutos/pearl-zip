/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.constants;

import javafx.geometry.Insets;
import javafx.scene.control.Menu;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static com.ntak.pearlzip.ui.constants.ZipConstants.CNS_NTAK_PEARL_ZIP_NO_FILES_HISTORY;

/**
 *  Useful constant objects that help with the display of JavaFX UI structures.
 *  @author Aashutos Kakshepati
*/
public class ResourceConstants {
    public static final Image folderIcon = new Image("folder.png");
    public static final Image fileIcon = new Image("file.png");

    public static final BackgroundFill DEFAULT_HIGHLIGHT = new BackgroundFill(Color.GOLDENROD, CornerRadii.EMPTY,
                                                                              Insets.EMPTY);

    public static final DateTimeFormatter DTF_YYYY = DateTimeFormatter.ofPattern("yyyy");
    public static final Pattern DSV = Pattern.compile(Pattern.quote("."));
    public static final Pattern CSV = Pattern.compile(Pattern.quote(","));
    public static final Pattern SSV = Pattern.compile(Pattern.quote("/"));

    public static final String PATTERN_FXID_NEW_OPTIONS = "%s.new-options";
    public static final String PATTERN_FXID_OPTIONS = "%s.options";

    public static final int NO_FILES_HISTORY = Integer.parseInt(System.getProperty(CNS_NTAK_PEARL_ZIP_NO_FILES_HISTORY,
                                                                                   "5"));
    public static Menu WINDOW_MENU;
}
