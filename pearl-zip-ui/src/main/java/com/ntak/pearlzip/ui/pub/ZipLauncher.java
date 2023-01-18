/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.AbstractSeedStartupStage;
import com.ntak.pearlzip.ui.util.JFXUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

/**
 *  Loads the main UI screen for the Zip Application.
 *  @author Aashutos Kakshepati
*/
public class ZipLauncher {

    public static final CopyOnWriteArrayList<String> OS_FILES = new CopyOnWriteArrayList<>();

    // Reference: https://github.com/eschmar/javafx-custom-file-ext-boilerplate
    static {
        if (Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_FILE)) {
            Desktop.getDesktop().setOpenFileHandler((e)-> e.getFiles().stream().map(File::getAbsolutePath).forEach(l -> {
                try {
                    OS_FILES.add(l);
                } catch(Exception exc) {
                }
            }));
        }
    }

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, InterruptedException, IOException {

        ////////////////////////////////////////////
        ///// SEED STARTUP STAGES /////////////////
        //////////////////////////////////////////

        for (AbstractSeedStartupStage stage : SEED_STARTUP_STAGE) {
            stage.execute();
        }

        ////////////////////////////////////////////
        ///// LAUNCH //////////////////////////////
        //////////////////////////////////////////

        // Dynamically load launcher...
        Class<?> klass = Class.forName(System.getProperty(CNS_LAUNCHER_CANONICAL_NAME));
        Method mainMethod = klass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args);

        // Wait for latch unless countdown was not triggered due to race. A break check is initiated in this case.
       while (!InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                                   .<CountDownLatch>getAdditionalConfig(CK_APP_LATCH)
                                   .get()
                                   .await(300, TimeUnit.MILLISECONDS)) {
           if (JFXUtil.getMainStageInstances().size() == 0)  {
               break;
           }
       }

       Runtime.getRuntime().exit(0);
    }
}
