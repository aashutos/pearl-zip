/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.mac;

import com.ntak.pearlzip.archive.constants.LoggingConstants;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.pub.PearlZipApplication;
import com.ntak.pearlzip.ui.pub.SysMenuController;
import com.ntak.pearlzip.ui.pub.ZipLauncher;
import de.jangassen.MenuToolkit;
import de.jangassen.model.AppearanceMode;
import de.jangassen.platform.NativeAdapter;
import de.jangassen.platform.mac.MacNativeAdapter;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.COM_BUS_EXECUTOR_SERVICE;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.ui.mac.MacZipConstants.*;

/**
 * Loads the main macOS UI screen for the Zip Application.
 *
 * @author Aashutos Kakshepati
 */
public class MacPearlZipApplication extends PearlZipApplication {

    public void createSystemMenu(Stage aboutStage, List<javafx.scene.control.Menu> customMenus) throws IOException {
        ////////////////////////////////////////////
        ///// Create System Menu //////////////////
        //////////////////////////////////////////

        if (!InternalContextCache.INTERNAL_CONFIGURATION_CACHE.getAdditionalConfig(CK_MENU_TOOLKIT).isPresent()) {
            InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_MENU_TOOLKIT, MenuToolkit.toolkit(Locale.getDefault()));
        }

        // Create a new System Menu
        String appName = System.getProperty(CNS_NTAK_PEARL_ZIP_APP_NAME, "PearlZip");
        MenuBar sysMenu;
        final MenuToolkit menuToolkit = InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<MenuToolkit>getAdditionalConfig(CK_MENU_TOOLKIT)
                                                    .get();

        if (!ZipConstants.getAdditionalConfig(SYS_MENU).isPresent()) {

            sysMenu = new MenuBar();
            sysMenu.setUseSystemMenuBar(true);
            sysMenu.getMenus()
                   .add(menuToolkit.createDefaultApplicationMenu(appName,
                                                       aboutStage));

            // Add some more Menus...
            FXMLLoader menuLoader = new FXMLLoader();
            menuLoader.setLocation(MacPearlZipApplication.class.getClassLoader()
                                                               .getResource("sysmenu.fxml"));
            menuLoader.setResources(LOG_BUNDLE);
            MenuBar additionalMenu = menuLoader.load();
            SysMenuController menuController = menuLoader.getController();
            menuController.initData();
            sysMenu.getMenus()
                   .addAll(additionalMenu.getMenus());
            ZipConstants.setAdditionalConfig(CORE_MENU_SIZE, sysMenu.getMenus().size());
        } else {
            sysMenu = ZipConstants.<MenuBar>getAdditionalConfig(SYS_MENU)
                                  .get();
        }
        ZipConstants.setAdditionalConfig(SYS_MENU, sysMenu);
        int coreMenuSize = ZipConstants.<Integer>getAdditionalConfig(CORE_MENU_SIZE)
                                       .get();
        if (sysMenu.getMenus().size() > coreMenuSize) {
            sysMenu.getMenus()
                   .remove(3,
                           sysMenu.getMenus()
                                  .size() - 2);
        }

        for (javafx.scene.control.Menu menu : customMenus) {
            // Add before Window and Help menus
            sysMenu.getMenus().add(sysMenu.getMenus().size()-2, menu);
        }
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_WINDOW_MENU,
                sysMenu.getMenus()
                       .stream()
                       .filter(m -> m.getText()
                                     .equals(LoggingUtil.resolveTextKey(CNS_SYSMENU_WINDOW_TEXT)))
                       .findFirst()
                       .get()
        );

        // Use the menu sysMenu for all stages including new ones
        if (menuToolkit.systemUsesDarkMode()) {
            menuToolkit.setAppearanceMode(AppearanceMode.DARK);
        } else {
            menuToolkit.setAppearanceMode(AppearanceMode.LIGHT);
        }
        try {
            final NativeAdapter nativeAdapter = MacNativeAdapter.getInstance();
            nativeAdapter.setMenuBar(sysMenu.getMenus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        COM_BUS_EXECUTOR_SERVICE.shutdown();
        PRIMARY_EXECUTOR_SERVICE.shutdown();
    }

    public static void main(String[] args) {
        try {
            ZipLauncher.initialize();
            LoggingConstants.ROOT_LOGGER.debug(Arrays.toString(args));
            launch(args);
        } catch(Exception e) {
            LoggingConstants.ROOT_LOGGER.error(e.getMessage());
            LoggingConstants.ROOT_LOGGER.error(LoggingUtil.getStackTraceFromException(e));
        }
    }

}
