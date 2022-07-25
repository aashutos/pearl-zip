/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util.internal;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BiFunction;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.CNS_NTAK_PEARL_ZIP_DB_CACHE_THRESHOLD_HOURS;
import static com.ntak.pearlzip.ui.constants.ZipConstants.LOG_QUERY_RESULT_INVALID_EXTRACTION;

/**
 *  Cached result data from query execution is stored in normalised form here.
 *  Used in conjunction with simple queries in which results can be parsed from a String value.
 *  i.e. It most likely not be compatible with queries that return blob and inherent binary values.
 *
 *  @author Aashutos Kakshepati
 */
public final class QueryResult implements Serializable {
    @Serial
    private static final long serialVersionUID = -3044874206183846580L;

    private final LocalDateTime expiryTimestamp;
    private final List<Map<String,String>> results;

    public QueryResult(List<Map<String,String>> results) {
        this.expiryTimestamp = LocalDateTime.now().plus(Duration.of(Long.parseLong(System.getProperty(CNS_NTAK_PEARL_ZIP_DB_CACHE_THRESHOLD_HOURS, "4")), ChronoUnit.HOURS));
        this.results = results;
    }

    // Object extraction...
    public Optional<String> getString(int row, String column) {
        try {
            return Optional.of(results.get(row)
                                      .get(column));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<LocalDateTime> getTimestamp(int row, String column, String format) {
        try {
            return Optional.of(LocalDateTime.parse(results.get(row)
                                      .get(column), DateTimeFormatter.ofPattern(format)));
        } catch (IllegalArgumentException | NullPointerException | DateTimeParseException e) {
            return Optional.empty();
        }
    }

    // Primitives...
    public byte getByte(int row, String column) {
        try {
            return Byte.parseByte(results.get(row)
                                         .get(column));
        } catch (NullPointerException|NumberFormatException e) {
            // LOG: Data of type = %s not found at (row: %s, column: %s).
            throw new RuntimeException(resolveTextKey(LOG_QUERY_RESULT_INVALID_EXTRACTION, "byte", row, column));
        }
    }

    public short getShort(int row, String column) {
        try {
            return Short.parseShort(results.get(row).get(column));
        } catch (NullPointerException|NumberFormatException e) {
            // LOG: Data of type = %s not found at (row: %s, column: %s).
            throw new RuntimeException(resolveTextKey(LOG_QUERY_RESULT_INVALID_EXTRACTION, "short", row, column));
        }
    }

    public int getInt(int row, String column) {
        try {
            return Integer.parseInt(results.get(row).get(column));
        } catch (NullPointerException|NumberFormatException e) {
            // LOG: Data of type = %s not found at (row: %s, column: %s).
            throw new RuntimeException(resolveTextKey(LOG_QUERY_RESULT_INVALID_EXTRACTION, "int", row, column));
        }
    }

    public long getLong(int row, String column) {
        try {
            return Long.parseLong(results.get(row).get(column));
        } catch (NullPointerException|NumberFormatException e) {
            // LOG: Data of type = %s not found at (row: %s, column: %s).
            throw new RuntimeException(resolveTextKey(LOG_QUERY_RESULT_INVALID_EXTRACTION, "long", row, column));
        }
    }

    public char getChar(int row, String column) {
        try {
            if (results.get(row).get(column).length() == 1) {
                return results.get(row)
                              .get(column)
                              .charAt(0);
            } else {
                // LOG: Data of type = %s not found at (row: %s, column: %s).
                throw new RuntimeException(resolveTextKey(LOG_QUERY_RESULT_INVALID_EXTRACTION, "char", row, column));
            }
        } catch (NullPointerException|NumberFormatException e) {
            // LOG: Data of type = %s not found at (row: %s, column: %s).
            throw new RuntimeException(resolveTextKey(LOG_QUERY_RESULT_INVALID_EXTRACTION, "char", row, column));
        }
    }

    public boolean getBoolean(int row, String column) {
        try {
            return Boolean.parseBoolean(results.get(row).get(column));
        } catch (NullPointerException|NumberFormatException e) {
            // LOG: Data of type = %s not found at (row: %s, column: %s).
            throw new RuntimeException(resolveTextKey(LOG_QUERY_RESULT_INVALID_EXTRACTION, "boolean", row, column));
        }
    }

    public float getFloat(int row, String column) {
        try {
            return Float.parseFloat(results.get(row).get(column));
        } catch (NullPointerException|NumberFormatException e) {
            // LOG: Data of type = %s not found at (row: %s, column: %s).
            throw new RuntimeException(resolveTextKey(LOG_QUERY_RESULT_INVALID_EXTRACTION, "float", row, column));
        }
    }

    public double getDouble(int row, String column) {
        try {
            return Double.parseDouble(results.get(row).get(column));
        } catch (NullPointerException|NumberFormatException e) {
            // LOG: Data of type = %s not found at (row: %s, column: %s).
            throw new RuntimeException(resolveTextKey(LOG_QUERY_RESULT_INVALID_EXTRACTION, "double", row, column));
        }
    }

    public <T> List<T> mapResult(BiFunction<QueryResult,Integer,T> mapper) {
        if (Objects.isNull(mapper)) {
            return Collections.emptyList();
        }

        List<T> output = new LinkedList<>();
        for (int i = 0; i < results.size(); i++) {
            output.add(mapper.apply(this, i));
        }

        return output;
    }

    // Expiry timestamp for cached data stating to what point valid until.
    public LocalDateTime getExpiryTimestamp() {
        return expiryTimestamp;
    }

    // Size of Result Set.
    public int getSize() {
        return results.size();
    }
}
