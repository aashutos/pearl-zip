/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveReadService;
import com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveWriteService;
import com.ntak.pearlzip.archive.szjb.pub.SevenZipArchiveService;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

@Tag("fx-test")
public abstract class AbstractPearlZipTestFX extends ApplicationTest {

    private Path LOCAL_TEMP;

    @Override
    public void start(Stage stage) throws IOException, TimeoutException {
        System.setProperty(CNS_NTAK_PEARL_ZIP_NO_FILES_HISTORY, "5");
        PearlZipFXUtil.initialise(stage,
                                  List.of(new CommonsCompressArchiveWriteService()),
                                  List.of(new SevenZipArchiveService(), new CommonsCompressArchiveReadService())
        );
        Path STORE_ROOT = InternalContextCache.GLOBAL_CONFIGURATION_CACHE.<Path>getAdditionalConfig(CK_STORE_ROOT).get();
        LOCAL_TEMP = Paths.get(Optional.ofNullable(System.getenv("TMPDIR"))
                                       .orElse(STORE_ROOT.toString()));
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_LOCAL_TEMP, LOCAL_TEMP);
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_STORE_TEMP,
                                                                            Paths.get(System.getProperty("user.home"), ".pz", "temp"));
    }

    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
        List<Stage> stages = JFXUtil.getMainStageInstances();

        for (Stage stage : stages) {
            FXArchiveInfo archive = (FXArchiveInfo)stage.getUserData();
            Path archivePath = Paths.get(archive.getArchivePath());
            Files.deleteIfExists(archivePath);
        }

        Files.list(LOCAL_TEMP).filter(f->f.getFileName().toString().startsWith("test")).forEach(path -> {
            try {
                Files.deleteIfExists(
                        path);
            } catch(IOException e) {
            }
        });
    }
}
