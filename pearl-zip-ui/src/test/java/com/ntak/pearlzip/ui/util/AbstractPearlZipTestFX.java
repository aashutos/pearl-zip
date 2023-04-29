/*
 * Copyright © 2023 92AK
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_STORE_REPO;
import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_STORE_ROOT;

@Tag("fx-test")
public abstract class AbstractPearlZipTestFX extends ApplicationTest {

    public static final Path LOCAL_TEMP = Paths.get(System.getenv("TMPDIR"));
    public static final Path localWorkspace = Path.of(System.getProperty("user.home"), ".pz");
    public static final Path applicationProps = localWorkspace.resolve("application.properties");
    public static final Path backupLocalWorkspace = Path.of(System.getProperty("user.home"), ".pz-backup");

    @Override
    public void start(Stage stage) throws IOException, TimeoutException {
        Path STORE_TEMP = localWorkspace.resolve("temp");

        // Setup workspace prior to launch
        if (Files.exists(backupLocalWorkspace)) {
            ArchiveUtil.deleteDirectory(backupLocalWorkspace, (p)->false);
        }
        if (Files.exists(localWorkspace)) {
            Files.move(localWorkspace, backupLocalWorkspace);
        }

        // Initialise PearlZip Application
        PearlZipFXUtil.initialise(stage, List.of(new CommonsCompressArchiveWriteService()), List.of(new SevenZipArchiveService(), new CommonsCompressArchiveReadService()), Paths.get(STORE_TEMP.toAbsolutePath().toString(), String.format("a%d.zip", System.currentTimeMillis())));

        // Save Properties...
        if (!Files.exists(applicationProps)) {
            Files.createFile(applicationProps);
        }
        System.getProperties().store(Files.newBufferedWriter(applicationProps), "PearlZip Automated Test");

        if (Arrays.stream(this.getClass().getAnnotations())
                  .anyMatch(a -> (Tag.class.isInstance(a)) && ((Tag)a).value().equals("UATExtensionStore"))) {
            Path repoPath = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                    .<Path>getAdditionalConfig(CK_STORE_ROOT)
                    .get()
                    .resolve("repository").resolve("default");
            Path testRepoPath = Paths.get("src", "test", "resources", "default").toAbsolutePath();
            Files.copy(repoPath, repoPath.getParent().resolve("default_backup"),
                       StandardCopyOption.REPLACE_EXISTING);
            com.ntak.pearlzip.ui.util.internal.JFXUtil.loadStoreRepoDetails(testRepoPath);
            com.ntak.pearlzip.ui.util.internal.JFXUtil.persistStoreRepoDetails(InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Map<String,StoreRepoDetails>>getAdditionalConfig(CK_STORE_REPO).get().get("default"), repoPath);
        }
    }

    @AfterAll
    public static void tearDownLast() throws IOException {
        // Restore previous workspace
        if (Files.exists(localWorkspace)) {
            ArchiveUtil.deleteDirectory(localWorkspace, (p)->false);
        }

        if (Files.exists(backupLocalWorkspace)) {
            Files.move(backupLocalWorkspace, localWorkspace);
        }
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

        if (Arrays.stream(this.getClass().getAnnotations())
                  .anyMatch(a -> (Tag.class.isInstance(a)) && ((Tag)a).value().equals("UATExtensionStore"))) {
            Path repoPath = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                    .<Path>getAdditionalConfig(CK_STORE_ROOT)
                    .get()
                    .resolve("repository")
                    .resolve("default");
            Files.copy(repoPath.getParent()
                               .resolve("default_backup"), repoPath, StandardCopyOption.REPLACE_EXISTING);
            com.ntak.pearlzip.ui.util.internal.JFXUtil.loadStoreRepoDetails(repoPath);
            com.ntak.pearlzip.ui.util.internal.JFXUtil.persistStoreRepoDetails(InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Map<String,StoreRepoDetails>>getAdditionalConfig(CK_STORE_REPO).get().get("default"), repoPath);
        }
    }
}
