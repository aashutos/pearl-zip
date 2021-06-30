/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.*;

public class FrmZip4jNewOptionsController {

    private static final String[] COMPRESSION_METHOD = {"STORE", "DEFLATE"};
    private static final Integer[] COMPRESSION_LEVEL = {1,2,3,4,5,6,7,8,9};
    private static final String[] ENCRYPTION_ALGORITHM = {"AES"};
    private static final String[] ENCRYPTION_STRENGTH = {"128-bit","256-bit"};

    @FXML
    private AnchorPane paneZip4jOptions;

    @FXML
    private ComboBox<String> comboCompressionMethod;
    @FXML
    private ComboBox<Integer> comboCompressionLevel;

    @FXML
    private CheckBox checkEnableEncryption;
    @FXML
    private PasswordField textEncryptionPassword;
    @FXML
    private ComboBox<String> comboEncryptionAlgorithm;
    @FXML
    private ComboBox<String> comboEncryptionStrength;

    @FXML
    public void initialize() {
        comboCompressionMethod.setItems(FXCollections.observableArrayList(COMPRESSION_METHOD));
        comboCompressionLevel.setItems(FXCollections.observableArrayList(COMPRESSION_LEVEL));

        comboEncryptionAlgorithm.setItems(FXCollections.observableArrayList(ENCRYPTION_ALGORITHM));
        comboEncryptionStrength.setItems(FXCollections.observableArrayList(ENCRYPTION_STRENGTH));

        comboCompressionLevel.setOnAction((e)-> {
            if (paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                archiveInfo.setCompressionLevel(comboCompressionLevel.getSelectionModel().getSelectedItem());
            }
        });

        comboCompressionMethod.setOnAction((e) -> {
            if (paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                archiveInfo.addProperty(KEY_COMPRESSION_METHOD,
                                        CompressionMethod.valueOf(comboCompressionMethod.getValue()));
            }
        });

        checkEnableEncryption.setOnAction((e)->{
            final boolean isEncrypted = this.checkEnableEncryption.isSelected();
            comboEncryptionAlgorithm.setDisable(!isEncrypted);
            comboEncryptionStrength.setDisable(!isEncrypted);
            textEncryptionPassword.setDisable(!isEncrypted);
            if (paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                if (isEncrypted) {
                    archiveInfo.addProperty(KEY_ENCRYPTION_ENABLE, true);
                } else {
                    archiveInfo.addProperty(KEY_ENCRYPTION_ENABLE, false);
                }
            }
        });

        comboEncryptionAlgorithm.setOnAction((e)->{
            if (!comboEncryptionAlgorithm.isDisabled() && paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                EncryptionMethod method = switch (comboEncryptionAlgorithm.getValue()) {
                    default -> EncryptionMethod.AES;
                };

                archiveInfo.addProperty(KEY_ENCRYPTION_METHOD, method);
            }
            if (comboEncryptionAlgorithm.isDisabled() && paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                archiveInfo.addProperty(KEY_ENCRYPTION_METHOD, null);
            }
        });

        comboEncryptionStrength.setOnAction((e)->{
            if (!comboEncryptionStrength.isDisabled() && paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                String encryptionStrength = comboEncryptionStrength.getValue();
                AesKeyStrength keyStrength = switch (encryptionStrength) {
                    case "128-bit" -> AesKeyStrength.KEY_STRENGTH_128;
                    case "192-bit" -> AesKeyStrength.KEY_STRENGTH_192;
                    default -> AesKeyStrength.KEY_STRENGTH_256;
                };
                archiveInfo.addProperty(KEY_ENCRYPTION_STRENGTH, keyStrength);
            }
            if (comboEncryptionStrength.isDisabled() && paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                archiveInfo.addProperty(KEY_ENCRYPTION_STRENGTH, null);
            }
        });

        textEncryptionPassword.setOnKeyReleased((e)->{
            if (!textEncryptionPassword.isDisabled() && paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                archiveInfo.addProperty(KEY_ENCRYPTION_PW, textEncryptionPassword.getText().toCharArray());
            }
            if (textEncryptionPassword.isDisabled() && paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                archiveInfo.addProperty(KEY_ENCRYPTION_PW, null);
            }
        });

        comboCompressionMethod.getSelectionModel().select(COMPRESSION_METHOD.length-1);
        comboCompressionLevel.getSelectionModel().select(COMPRESSION_LEVEL.length-1);
        comboEncryptionAlgorithm.getSelectionModel().select(ENCRYPTION_ALGORITHM.length-1);
        comboEncryptionStrength.getSelectionModel().select(ENCRYPTION_STRENGTH.length-1);
    }
}
