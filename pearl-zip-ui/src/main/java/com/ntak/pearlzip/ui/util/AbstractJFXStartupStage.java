/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.util;

import javafx.application.Application;

/**
 *  A special instantiation of a stage for use after JavaFX application has been launched and so includes a context of the
 *  JavaFX Application this stage is being executed from. The stage runs within the JavaFX UI thread.
 *
 *  @author Aashutos Kakshepati
 */
public abstract class AbstractJFXStartupStage extends AbstractStartupStage {
    protected final Application application;

    public AbstractJFXStartupStage(Application application) {
        this.application = application;
        this.NAME = this.getClass().getName();
    }

    public AbstractJFXStartupStage(Application application, String name) {
        this.application = application;
        this.NAME = name;
    }

    @Override
    public void execute() {
            JFXUtil.runLater(() ->
                {
                   try {
                        logStartStage();
                        executeProcess();
                        logEndStage();
                   } catch (Exception e) {
                        logException(e);
                   }
                }
            );
    }
}
