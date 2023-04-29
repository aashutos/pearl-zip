/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.stages.jfx;

import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.AbstractJFXStartupStage;
import javafx.application.Application;

import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

/**
 *  Sets up constants based on the running JavaFX application.
 *
 *  @author Aashutos Kakshepati
 */
public class JFXInMemoryCacheStartupStage extends AbstractJFXStartupStage {

    public JFXInMemoryCacheStartupStage() {
        super(InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Application>getAdditionalConfig(CK_APP).get());
    }

    public JFXInMemoryCacheStartupStage(Application application) {
        super(application);
    }

    @Override
    public void executeProcess() throws Exception {
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_HOST_SERVICES, application.getHostServices());
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_PARAMETERS, application.getParameters());
    }
}
