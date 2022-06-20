/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.util;

import com.ntak.pearlzip.archive.constants.ConfigurationConstants;

import java.util.*;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.CUSTOM_BUNDLE;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.constants.internal.LoggingConstants.PLUGIN_BUNDLES;

/**
 *  Useful utility methods used in generating and sourcing log messages.
 *  @author Aashutos Kakshepati
 */
public class LoggingUtil {

    public static Locale genLocale(Properties props) {
        String lang = props.getProperty(ConfigurationConstants.CNS_LOCALE_LANG, "en");
        String country = props.getProperty(ConfigurationConstants.CNS_LOCALE_COUNTRY, "GB");
        String variant = props.getProperty(ConfigurationConstants.CNS_LOCALE_VARIANT, null);

        Locale locale;
        if (variant != null) {
            locale = new Locale.Builder()
                    .setLanguage(lang)
                    .setRegion(country)
                    .setVariant(variant)
                    .build();
        } else {
            locale = new Locale.Builder()
                    .setLanguage(lang)
                    .setRegion(country)
                    .build();
        }
        return locale;
    }

    public static String genDefaultLoggingMessage(String msgKey, Object... parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append(msgKey)
          .append(" | Parameters : [");
        for (Object param : parameters) {
            sb.append(' ')
              .append(param)
              .append(',');
        }

        int pos;
        if ((pos = sb.lastIndexOf(",")) > 0) {
            sb.deleteCharAt(pos);
        }

        sb.append(" ]");
        return sb.toString();
    }

    /**
     *  A lookup of configuration keys in the available bundles to determine localised String values.
     *  A default String would be returned on failure to process String.
     *
     *  @param key The unique identifier of a localised string property
     *  @param parameters The parameters to substitute in the localised String
     *  @return The formatted localised String
     */
    public static String resolveTextKey(String key, Object... parameters) {
        try {
            String message = null;
            for (ResourceBundle pluginBundle : PLUGIN_BUNDLES) {
                if (pluginBundle.containsKey(key)) {
                    message = pluginBundle.getString(key);
                }
            }

            if (Objects.isNull(message)) {
                if (CUSTOM_BUNDLE.containsKey(key)) {
                    message = CUSTOM_BUNDLE.getString(key);
                } else if (LOG_BUNDLE.containsKey(key)) {
                    message = LOG_BUNDLE.getString(key);
                } else {
                    return genDefaultLoggingMessage(key, parameters);
                }
            }
            return String.format(message, parameters);
        } catch(Exception e) {
            return genDefaultLoggingMessage(key, parameters);
        }
    }

    public static String getStackTraceFromException(Throwable e) {
        return String.format("Exception message: %s\nStack Trace:\n%s",
                             e.getMessage(),
                             Arrays.stream(e.getStackTrace())
                                   .map(StackTraceElement::toString)
                                   .collect(Collectors.joining("\n"))
        );
    }
}
