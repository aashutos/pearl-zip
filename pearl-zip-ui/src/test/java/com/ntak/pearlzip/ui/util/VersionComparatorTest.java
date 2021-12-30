/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import javafx.application.Platform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class VersionComparatorTest {

    private final VersionComparator comparator = VersionComparator.getInstance();
    private static CountDownLatch latch = new CountDownLatch(1);

    /*
     *  Test cases:
     *  + Major version difference
     *  + Minor version difference
     *  + Patch version difference
     *  + Incremental version difference
     */

    @BeforeAll
    public static void setUpOnce() throws IOException, InterruptedException {
        try {
            Platform.startup(() -> latch.countDown());
        } catch (Exception e) {
            latch.countDown();
        } finally {
            latch.await();
        }
    }

    @Test
    @DisplayName("Test: Major version difference yields the expected comparison coefficient")
    public void testCompare_MajorDifference_MatchExpectations() {
        Assertions.assertEquals(2, comparator.compare("4.0.0.0", "2.0.0.0"), "Unexpected coefficient. 4 > 2");
        Assertions.assertEquals(8, comparator.compare("10.0.0.0","2.0.0.0"), "Unexpected coefficient. 10 > 2");
        Assertions.assertEquals(-1, comparator.compare("1.0.0.0","2.0.0.0"), "Unexpected coefficient. 1 < 2");
    }

    @Test
    @DisplayName("Test: Minor version difference yields the expected comparison coefficient")
    public void testCompare_MinorDifference_MatchExpectations() {
        Assertions.assertEquals(4, comparator.compare("2.4.0.0", "2.0.0.0"), "Unexpected coefficient. 4 > 0");
        Assertions.assertEquals(9, comparator.compare("12.10.0.0","12.1.0.0"), "Unexpected coefficient. 10 > 1");
        Assertions.assertEquals(-999, comparator.compare("1.0.0.0","1.999.0.0"), "Unexpected coefficient. 0 < 999");
    }

    @Test
    @DisplayName("Test: Patch version difference yields the expected comparison coefficient")
    public void testCompare_PatchDifference_MatchExpectations() {
        Assertions.assertEquals(10, comparator.compare("2.4.10.0", "2.4.0.0"), "Unexpected coefficient. 10 > 0");
        Assertions.assertEquals(76, comparator.compare("12.10.99.0","12.10.23.0"), "Unexpected coefficient. 99 > 23");
        Assertions.assertEquals(-4, comparator.compare("1.999.0.0","1.999.4.0"), "Unexpected coefficient. 0 < 4");
    }

    @Test
    @DisplayName("Test: Incremental version difference yields the expected comparison coefficient")
    public void testCompare_IncrementalDifference_MatchExpectations() {
        Assertions.assertEquals(6, comparator.compare("2.4.10.15", "2.4.10.9"), "Unexpected coefficient. 15 > 9");
        Assertions.assertEquals(7, comparator.compare("12.10.99.9","12.10.99.2"), "Unexpected coefficient. 9 > 2");
        Assertions.assertEquals(-23, comparator.compare("1.999.0.0","1.999.0.23"), "Unexpected coefficient. 0 < 23");
    }
}
