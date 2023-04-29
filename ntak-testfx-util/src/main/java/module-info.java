/*
 *  Copyright (c) 2021 92AK
 */
module ntak.testfx.util {
    exports com.ntak.testfx;
    exports com.ntak.testfx.specifications;

    requires javafx.graphics;
    requires org.testfx;
    requires javafx.controls;
    requires com.ntak.pearlzip.archive;
    requires org.mockito;
    requires java.desktop;
    requires org.junit.jupiter.api;
}