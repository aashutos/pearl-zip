/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.util;

import com.ntak.pearlzip.archive.constants.ConfigurationConstants;
import com.ntak.pearlzip.archive.constants.LoggingConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import static com.ntak.pearlzip.archive.util.LoggingUtil.genLocale;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;

public class LoggingUtilTest {

    /*
        Test cases:
        + Default Locale generation
        + Configure custom Locale:
            - Language
            - Language, Country
            - Language, Country, Variant

        + Default Logging Message is in expected format (with multiple parameters)

        + Custom Bundle text key search
        + Log Bundle text key search
        + No match default logging message
        + String format issue -> default logging message
     */

    @BeforeAll
    public static void setUpOnce() {
        Locale defaultLocale = genLocale(new Properties());
        LoggingConstants.LOG_BUNDLE = ResourceBundle.getBundle("pearlzip",
                                                               defaultLocale);
        LoggingConstants.CUSTOM_BUNDLE = ResourceBundle.getBundle("custom",
                                                                  defaultLocale);
    }

    @Test
    @DisplayName("Test: Generate Locale with Custom language: Klingon GB")
    public void testGenerateLocale_CustomLanguage_MatchExpectations() {
        Properties props = new Properties();
        props.setProperty(ConfigurationConstants.CNS_LOCALE_LANG, "tlh");
        Locale locale = LoggingUtil.genLocale(props);
        Assertions.assertNotNull(locale, "Locale was not generated");
        Assertions.assertEquals("tlh", locale.getLanguage(), "Language was not 'tlh'");
        Assertions.assertEquals("GB", locale.getCountry(), "Country was not 'GB'");
        Assertions.assertEquals("", locale.getVariant(), "Variant was not empty");
    }

    @Test
    @DisplayName("Test: Generate Locale with Custom language: Klingon US")
    public void testGenerateLocale_CustomLanguageCustomLanguage_MatchExpectations() {
        Properties props = new Properties();
        props.setProperty(ConfigurationConstants.CNS_LOCALE_LANG, "tlh");
        props.setProperty(ConfigurationConstants.CNS_LOCALE_COUNTRY, "US");
        Locale locale = LoggingUtil.genLocale(props);
        Assertions.assertNotNull(locale, "Locale was not generated");
        Assertions.assertEquals("tlh", locale.getLanguage(), "Language was not 'tlh'");
        Assertions.assertEquals("US", locale.getCountry(), "Country was not 'US'");
        Assertions.assertEquals("", locale.getVariant(), "Variant was not empty");
    }

    @Test
    @DisplayName("Test: Generate Locale with Custom language: Klingon US of the House of T'Kuvma dialect")
    public void testGenerateLocale_CustomLanguageCustomLanguageCustomVariant_MatchExpectations() {
        Properties props = new Properties();
        props.setProperty(ConfigurationConstants.CNS_LOCALE_LANG, "tlh");
        props.setProperty(ConfigurationConstants.CNS_LOCALE_COUNTRY, "US");
        props.setProperty(ConfigurationConstants.CNS_LOCALE_VARIANT, "HoTKuvma");
        Locale locale = LoggingUtil.genLocale(props);
        Assertions.assertNotNull(locale, "Locale was not generated");
        Assertions.assertEquals("tlh", locale.getLanguage(), "Language was not 'tlh'");
        Assertions.assertEquals("US", locale.getCountry(), "Country was not 'US'");
        Assertions.assertEquals("HoTKuvma", locale.getVariant(), "Variant was not: House of T-Kuvma (HoTKuvma)");
    }

    @Test
    @DisplayName("Test: Generate Locale without parameters to form default Locale: en-GB")
    public void testGenerateLocale_DefaultSetup_MatchExpectations() {
        Locale locale = LoggingUtil.genLocale(new Properties());
        Assertions.assertNotNull(locale, "Locale was not generated");
        Assertions.assertEquals("en", locale.getLanguage(), "Language was not 'en'");
        Assertions.assertEquals("GB", locale.getCountry(), "Country was not 'GB'");
        Assertions.assertEquals("", locale.getVariant(), "Variant was not empty");
    }

    @Test
    @DisplayName("Test: Generate default message with no parameters successfully")
    public void testGenerateDefaultMessage_NoParameters_Success() {
        Assertions.assertEquals("some.key.here | Parameters : [ ]", LoggingUtil.genDefaultLoggingMessage("some.key.here"));
    }

    @Test
    @DisplayName("Test: Generate default message with parameters successfully")
    public void testGenerateDefaultMessage_Parameters_Success() {
        Assertions.assertEquals("some.parameter.key.here | Parameters : [ pie, 3, 0.14, true ]",
                                LoggingUtil.genDefaultLoggingMessage("some.parameter.key.here", "pie"
                , 3, 0.14f, true));
    }

    @Test
    @DisplayName("Test: Resolve text key with a valid key logs as expected")
    public void testResolveTextKey_ValidKey_Success() {
        Assertions.assertEquals("\"When a man is denied the right to live the life he believes in, he has no choice but to become an outlaw.\" -" +
                                        " Nelson Mandela",
                                resolveTextKey("demo.logging.MSG004", "Nelson Mandela"),
                                "Generated log was not as expected"
        );

        Assertions.assertEquals("\"Do not judge me by my successes, judge me by how many times I fell down and got back up again.\" -" +
                                        " Nelson Mandela",
                                resolveTextKey("demo.logging.MSG005", "Nelson Mandela"),
                                "Generated log was not as expected"
        );

        Assertions.assertEquals("\"All generalizations are dangerous, even this one.\" -" + " Alexandre Dumas",
                                resolveTextKey("demo.logging.MSG007", "Alexandre Dumas"),
                                "Generated log was not as expected"
        );

        Assertions.assertEquals("\"Without freedom of thought, there can be no such thing as wisdom - and no such thing as public liberty without" +
                                        " freedom of speech.\" -" + " Benjamin Franklin",
                                resolveTextKey("demo.logging.MSG008", "Benjamin Franklin"),
                                "Generated log was not as expected"
        );

        Assertions.assertEquals("\"Time and tide wait for no man\" -" + " Geoffrey Chaucer",
                                resolveTextKey("demo.logging.MSG009", "Geoffrey Chaucer"),
                                "Generated log was not as expected"
        );
    }

    @Test
    @DisplayName("Test: Resolve text key with an invalid key logs with a default message")
    public void testResolveTextKey_InvalidKey_Success() {
        Assertions.assertEquals("demo.logging.non-existent | Parameters : [ ]",
                                resolveTextKey("demo.logging.non-existent"),
                                "Generated log was not as expected"
        );
    }

    @Test
    @DisplayName("Test: Resolve text key with a valid key, but invalid parameter logs with a default message")
    public void testResolveTextKey_ValidKeyInvalidArguments_Success() {
        Assertions.assertEquals(
                "demo.logging.MSG004 | Parameters : [ ]",
                resolveTextKey("demo.logging.MSG004"),
                "Generated log was not as expected"
        );
    }
}
