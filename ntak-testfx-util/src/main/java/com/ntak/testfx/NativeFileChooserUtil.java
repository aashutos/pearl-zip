/*
 * Copyright © 2021 92AK
 */

package com.ntak.testfx;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.testfx.api.FxRobot;

import java.nio.file.Path;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class NativeFileChooserUtil {

    public static void chooseFile(TestFXConstants.Platform platform, FxRobot robot, Path filePath) {
        switch (platform) {
            case MAC:   chooseFileMac(robot, filePath);
                        break;
            case WIN:
            case LINUX:
            default:    throw new RuntimeException(String.format("Platform %s not supported", platform));
        }
    }

    private static void chooseFileMac(FxRobot robot, Path filePath) {
        chooseFileMac(robot, filePath, s->s);
    }

    private static void chooseFileMac(FxRobot robot, Path filePath, Function<String,String> filenamePostTransform) {
        Path fileNamePath = filePath.getFileName();
        Path dirPath = filePath.getParent();
        String filename = filenamePostTransform.apply(fileNamePath.toString());
        String dir = dirPath.toString();

        // Type filename
        //TypeUtil.typeString(robot, filename);
        //robot.sleep(50, MILLISECONDS);

        // Navigate to directory
        robot.push(new KeyCodeCombination(KeyCode.G, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN));
        TypeUtil.typeString(robot, filePath.toString());
        robot.push(KeyCode.ENTER);
        robot.sleep(50, MILLISECONDS);

        // Execute dialog
        robot.push(KeyCode.ENTER);
        robot.sleep(50, MILLISECONDS);
    }

    public static void chooseFolder(TestFXConstants.Platform platform, FxRobot robot, Path dirPath) {
        switch (platform) {
            case MAC:   chooseFolderMac(robot, dirPath);
                        break;
            case WIN:
            case LINUX:
            default:    throw new RuntimeException(String.format("Platform %s not supported", platform));
        }
    }

    private static void chooseFolderMac(FxRobot robot, Path dirPath) {
        // Navigate to directory
        robot.push(new KeyCodeCombination(KeyCode.G, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN));
        TypeUtil.typeString(robot, dirPath.toAbsolutePath().toString());
        robot.push(KeyCode.ENTER);
        robot.sleep(50, MILLISECONDS);

        // Execute dialog
        robot.push(KeyCode.ENTER);
        robot.sleep(50, MILLISECONDS);
    }
}
