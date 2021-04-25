/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.model;

import com.ntak.pearlzip.archive.pub.FileInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

public class FXMigrationInfoTest {

    private FXMigrationInfo migrationInfo;
    private FileInfo info = new FileInfo(3, 1, "another-inner-file", 0, 0, 0,
                                         LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                         0, "", false, false, Collections.emptyMap());
    private FileInfo anotherInfo = new FileInfo(4, 1, "yet-another-inner-file", 0, 0, 0,
                                         LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                         0, "", false, false, Collections.emptyMap());
    /*
       Test cases:
       + Init Migration MOVE
       + Init Migration COPY
       + Init Migration DELETE
       + Init Migration fail as already in a migration
     */

    @BeforeEach
    public void setUp() {
        migrationInfo = new FXMigrationInfo();
        Assertions.assertEquals(FXMigrationInfo.MigrationType.NONE, migrationInfo.getType(), "MigrationType not " +
                "initialised properly");
        Assertions.assertNull(migrationInfo.getFile(), "File entry was not initilised as expected");
    }

    @Test
    @DisplayName("Test: Initialise MOVE Migration successfully")
    public void testInitMigration_MOVEMigrationType_MatchExpectation() {
        Assertions.assertTrue(migrationInfo.initMigration(FXMigrationInfo.MigrationType.MOVE, info), "Migration was " +
                "not successful");
        Assertions.assertEquals(FXMigrationInfo.MigrationType.MOVE, migrationInfo.getType(), "MigrationType not " +
                "initialised properly");
        Assertions.assertEquals(info, migrationInfo.getFile(), "File entry was not initialised as expected");
    }

    @Test
    @DisplayName("Test: Initialise COPY Migration successfully")
    public void testInitMigration_COPYMigrationType_MatchExpectation() {
            Assertions.assertTrue(migrationInfo.initMigration(FXMigrationInfo.MigrationType.COPY, info), "Migration " +
                    "was not successful");
            Assertions.assertEquals(FXMigrationInfo.MigrationType.COPY, migrationInfo.getType(), "MigrationType not " +
                    "initialised properly");
            Assertions.assertEquals(info, migrationInfo.getFile(), "File entry was not initialised as expected");
    }

    @Test
    @DisplayName("Test: Initialise DELETE Migration successfully")
    public void testInitMigration_DELETEMigrationType_MatchExpectation() {
        Assertions.assertTrue(migrationInfo.initMigration(FXMigrationInfo.MigrationType.DELETE, info), "Migration " +
                "was not successful");
        Assertions.assertEquals(FXMigrationInfo.MigrationType.DELETE, migrationInfo.getType(), "MigrationType not " +
                "initialised properly");
        Assertions.assertEquals(info, migrationInfo.getFile(), "File entry was not initialised as expected");
    }


    @Test
    @DisplayName("Test: Fails to initialise Migration after an active migration has been set")
    public void testInitMigration_FailMigration_MatchExpectation() {
        Assertions.assertTrue(migrationInfo.initMigration(FXMigrationInfo.MigrationType.COPY, anotherInfo), "Migration " +
                "was not successful");
        Assertions.assertEquals(FXMigrationInfo.MigrationType.COPY, migrationInfo.getType(), "MigrationType not " +
                "initialised properly");
        Assertions.assertEquals(anotherInfo, migrationInfo.getFile(), "File entry was not initialised as expected");

        Assertions.assertFalse(migrationInfo.initMigration(FXMigrationInfo.MigrationType.MOVE, info), "Migration " +
                "was not successful");
        Assertions.assertEquals(FXMigrationInfo.MigrationType.COPY, migrationInfo.getType(), "MigrationType not " +
                "initialised properly");
        Assertions.assertEquals(anotherInfo, migrationInfo.getFile(), "File entry was not initialised as expected");
    }
}
