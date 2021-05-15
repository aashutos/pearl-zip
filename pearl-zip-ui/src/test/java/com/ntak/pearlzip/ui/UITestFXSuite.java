/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.runner.RunWith;

/***
 *   @author akakshepati
 */
@RunWith(JUnitPlatform.class)
@SelectPackages("com.ntak.pearlzip.ui.testfx")
@SuiteDisplayName("PearlZip UI module Integration TestFX tests")
@IncludeClassNamePatterns("^.*TestFX$")
@IncludeTags("fx-test")
public class UITestFXSuite {

}
