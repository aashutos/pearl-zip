/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.stages.jfx;

import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.AbstractJFXStartupStage;
import javafx.application.Application;

import java.nio.file.Path;

import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

/**
 *  Sets up themes for PearlZip including default themes and custom theme detection.
 *
 *  @author Aashutos Kakshepati
 */
public class JFXThemesStartupStage extends AbstractJFXStartupStage {

    public JFXThemesStartupStage() {
        super(InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Application>getAdditionalConfig(CK_APP).get());
    }

    public JFXThemesStartupStage(Application application) {
        super(application);
    }

    @Override
    public void executeProcess() throws Exception {
        // Themes
        Path themesPath = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                              .<Path>getAdditionalConfig(CK_STORE_ROOT)
                                              .get()
                                              .resolve("themes");

        // Copy over and overwrite core themes...
        for (String theme : CORE_THEMES) {
            Path defThemePath = themesPath.resolve(theme);
            String moduleName = "com.ntak.pearlzip.ui";
            com.ntak.pearlzip.ui.util.internal.JFXUtil.extractResources(defThemePath, moduleName, theme);
        }

        // Initialise theme...
        String themeName = System.getProperty(CNS_THEME_NAME, "modena");
        com.ntak.pearlzip.ui.util.internal.JFXUtil.initialiseTheme(themesPath, themeName);
    }
}
