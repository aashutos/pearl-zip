/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;


import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.internal.QueryDefinition;
import com.ntak.pearlzip.ui.util.internal.QueryResult;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static org.mockito.Mockito.*;

public class QueryExecutorTest {

    /*
     *  Test cases:
     *  + Expired cache entries do not get used and are overwritten
     *  + Cache used when available and force not set
     *  + Cache not used even when available when force set
     *  + Cache identifiers are unique with respect to each other despite identical Query Definitions
     */

    private QueryExecutor executor;
    private QueryDefinition queryDefinition;
    private static final Map<String,QueryResult> dataCache = new ConcurrentHashMap<>();

    private Connection mockConnection;
    private Statement mockStatement;
    private ResultSet mockResultSet;
    private ResultSetMetaData mockResultSetMetaData;

    @BeforeAll
    public static void setUpOnce() {
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_QUERY_RESULT_CACHE, dataCache);
    }

    @BeforeEach
    public void setUp() throws SQLException, IOException {
        final Path STORE_ROOT = Paths.get(System.getProperty(CNS_STORE_ROOT,
                                                             String.format("%s/.pz",
                                                                           System.getProperty("user.home"))));
        Files.createDirectories(Paths.get(STORE_ROOT.toString(), "db-cache", "data"));
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_STORE_ROOT, STORE_ROOT);

        final var queryDefinitionCache = new ConcurrentHashMap<String,QueryDefinition>();
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_QUERY_DEFINITION_CACHE, queryDefinitionCache);

        // Add query definition to cache...
        queryDefinition = new QueryDefinition("test-query", "select 1", Collections.emptyList(), Collections.emptyList());
        queryDefinitionCache.put("test-query", queryDefinition);

        // Stubbing...
        mockConnection = Mockito.mock(Connection.class);
        mockStatement = Mockito.mock(Statement.class);
        mockResultSet = Mockito.mock(ResultSet.class);
        mockResultSetMetaData = Mockito.mock(ResultSetMetaData.class);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockResultSetMetaData);
        when(mockResultSetMetaData.getColumnCount()).thenReturn(1);
        when(mockResultSetMetaData.getColumnName(eq(1))).thenReturn("column");
    }

    @AfterEach
    public void tearDown() {
        dataCache.clear();
        Mockito.reset(mockConnection);
    }

    @Test
    @DisplayName("Test: Execute query with expired cache entry will cause invalidation and refresh from upstream")
    public void testExecute_ExpiredCache_InvalidateRefreshCache() throws SQLException {
        // Add query result to cache...
        System.setProperty(CNS_NTAK_PEARL_ZIP_DB_CACHE_THRESHOLD_HOURS, "0");
        QueryResult result = new QueryResult(List.of(Map.of("column","1")));
        dataCache.put("test-query-cache-id", result);

        // Prepare executor
        executor = new QueryExecutor("cache-id", queryDefinition, false, Collections.emptyMap(), Collections.emptyMap());
        executor.setConnection(mockConnection);

        // Method under test
        executor.execute();

        // Validate outcomes...
        verify(mockConnection, times(1)).createStatement();
        Assertions.assertNotEquals(result, dataCache.get("test-query-cache-id"), "The cache entry was not refreshed.");
    }

    @Test
    @DisplayName("Test: Execute query with valid cache entry and no force refresh will result in cached results to be used")
    public void testExecute_CacheAvailableForceNotSet_UseCache() throws SQLException {
        // Add query result to cache...
        System.setProperty(CNS_NTAK_PEARL_ZIP_DB_CACHE_THRESHOLD_HOURS, "4");
        QueryResult result = new QueryResult(List.of(Map.of("column","1")));
        dataCache.put("test-query-cache-id", result);

        // Prepare executor
        executor = new QueryExecutor("cache-id", queryDefinition, false, Collections.emptyMap(), Collections.emptyMap());
        executor.setConnection(mockConnection);

        // Method under test
        executor.execute();

        // Validate outcomes...
        verify(mockConnection, never()).createStatement();
        Assertions.assertEquals(result, dataCache.get("test-query-cache-id"), "The cache entry was refreshed unexpectedly.");
    }

    @Test
    @DisplayName("Test: Execute query with valid cache entry and force refresh enabled will cause invalidation and refresh from upstream")
    public void testExecute_CacheAvailableForceSet_InvalidateRefreshCache() throws SQLException {
        // Add query result to cache...
        System.setProperty(CNS_NTAK_PEARL_ZIP_DB_CACHE_THRESHOLD_HOURS, "0");
        QueryResult result = new QueryResult(List.of(Map.of("column","1")));
        dataCache.put("test-query-cache-id", result);

        // Prepare executor
        executor = new QueryExecutor("cache-id", queryDefinition, true, Collections.emptyMap(), Collections.emptyMap());
        executor.setConnection(mockConnection);

        // Method under test
        executor.execute();

        // Validate outcomes...
        verify(mockConnection, times(1)).createStatement();
        Assertions.assertNotEquals(result, dataCache.get("test-query-cache-id"), "The cache entry was not refreshed.");
    }

    @Test
    @DisplayName("Test: Execute query with different cache name (but same Query Definition) to stored in cache will yield a new cache entry (Independence by cache id)")
    public void testExecute_CacheAvailableDifferentId_NewCacheEntry() throws SQLException {
        // Add query result to cache...
        System.setProperty(CNS_NTAK_PEARL_ZIP_DB_CACHE_THRESHOLD_HOURS, "0");
        QueryResult result = new QueryResult(List.of(Map.of("column","1")));
        dataCache.put("test-query-cache-id", result);

        // Prepare executor
        executor = new QueryExecutor("cache-id-2", queryDefinition, true, Collections.emptyMap(), Collections.emptyMap());
        executor.setConnection(mockConnection);

        // Method under test
        executor.execute();

        // Validate outcomes...
        verify(mockConnection, times(1)).createStatement();
        Assertions.assertEquals(2, dataCache.size(), "A new cache entry was not created for executed query");
        Assertions.assertEquals(result, dataCache.get("test-query-cache-id"), "The existing cache entry does not match.");
        Assertions.assertNotNull(dataCache.get("test-query-cache-id-2"), "The new cache entry was not present.");
        Assertions.assertNotEquals(result, dataCache.get("test-query-cache-id-2"), "The new cache entry was not refreshed.");
    }
}
