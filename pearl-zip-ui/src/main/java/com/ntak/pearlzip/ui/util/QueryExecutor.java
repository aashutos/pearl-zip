/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.internal.QueryDefinition;
import com.ntak.pearlzip.ui.util.internal.QueryResult;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.*;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

public class QueryExecutor {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(QueryExecutor.class);
    private static final Map<String,QueryResult> QUERY_RESULT_CACHE = InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Map<String,QueryResult>>getAdditionalConfig(CK_QUERY_RESULT_CACHE).get();

    private final QueryDefinition definition;
    private final String cacheIdentifier;
    private final boolean isRefreshForced;
    private final Map<String,Object> parameters;
    private final Map<String, Function<Object,String>> extractors;
    private QueryResult queryResult;

    private Connection connection;

    public QueryExecutor(String cacheIdentifier, QueryDefinition definition, boolean isRefreshForced, Map<String,Object> parameters, Map<String,Function<Object,String>> extractors) {
        if (Objects.nonNull(cacheIdentifier)) {
            this.cacheIdentifier = String.format("%s-%s", definition.getId(), cacheIdentifier);
        } else {
            this.cacheIdentifier = definition.getId();
        }
        this.definition = definition;
        this.isRefreshForced = isRefreshForced;
        this.parameters = parameters;
        this.extractors = extractors;
    }

    public Optional<QueryResult> getQueryResult() {
        return Optional.ofNullable(queryResult);
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void execute() {
        synchronized(QUERY_RESULT_CACHE) {
            // Retrieve from source DB if:
            // 1) Forced to do so
            // 2) No cached data is available
            // 3) Cached data is out of date
            if (isRefreshForced
                || !QUERY_RESULT_CACHE.containsKey(cacheIdentifier)
                || (QUERY_RESULT_CACHE.containsKey(cacheIdentifier) && QUERY_RESULT_CACHE.get(cacheIdentifier).getExpiryTimestamp().isBefore(LocalDateTime.now()))
            ) {
                // PostGres SQL Driver does not support CallableStatement and so have to manually consume and form parameters...
                try (Connection conn = initConnection();
                     Statement s = conn.createStatement()) {
                     String query = definition.getQuery();

                     // Set parameters
                     for (Map.Entry<String,Object> parameter : parameters.entrySet()) {
                        query = query.replace(parameter.getKey(), String.format("'%s'", extractors.getOrDefault(parameter.getKey(), Object::toString)
                                                                                             .apply(parameter.getValue()))
                        );
                     }

                     // LOG: Query executed: %s
                     LOGGER.debug(resolveTextKey(LOG_QUERY_EXECUTED, query));

                     // Retrieve column output values of interest and persist down into marshal into file
                     ResultSet resultSet = s.executeQuery(query);
                     final ResultSetMetaData metaData = resultSet.getMetaData();
                     int resColumns = metaData.getColumnCount();
                     List<String> columns = IntStream.range(1, resColumns+1)
                                                     .mapToObj(i -> {
                                                         try {
                                                             return metaData.getColumnName(i);
                                                         } catch(SQLException e) {
                                                         }
                                                         return "";
                                                     })
                                                     .filter(n -> definition.getOutputColumns().contains(n))
                                                     .collect(Collectors.toList());

                     List<Map<String,String>> results = new LinkedList<>();
                     while (resultSet.next()) {
                        HashMap<String,String> rowResult = new HashMap<>();
                        columns.forEach(c -> {
                            try {
                                rowResult.put(c,resultSet.getString(c));
                            } catch(SQLException e) {
                                rowResult.put(c, null);
                            }
                        });
                        results.add(rowResult);
                     }

                     // Persisting cache entry...
                    queryResult = new QueryResult(results);
                    QUERY_RESULT_CACHE.put(cacheIdentifier, queryResult);
                     Path STORE_ROOT = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                            .<Path>getAdditionalConfig(CK_STORE_ROOT)
                            .get();
                    final Path cachedFile = Paths.get(STORE_ROOT.toString(), "db-cache", "data", cacheIdentifier);

                    // Clear existing query with identifier
                    Files.deleteIfExists(cachedFile);
                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cachedFile.toString()))) {
                        oos.writeObject(queryResult);
                        oos.flush();
                     }
                } catch(SQLException | IOException e) {
                    // LOG: Issue retrieving data (query Id = %s) from upstream datasource and persisting to local cache. Exception type: %s. Exception Message: %s./nStack trace:/n%s
                    LOGGER.warn(resolveTextKey(LOG_ISSUE_RETRIEVE_QUERY_DATA,
                                               definition.getId(),
                                               e.getClass().getCanonicalName(),
                                               e.getMessage(),
                                               LoggingUtil.getStackTraceFromException(e))
                    );
                }
            } else {
                LOGGER.info(resolveTextKey(LOG_USING_CACHE_FOR_QUERY, definition.getId()));
                queryResult = QUERY_RESULT_CACHE.get(cacheIdentifier);
            }
        }
    }

    private Connection initConnection() throws SQLException {
            if (Objects.isNull(connection) || connection.isClosed()) {
                return DriverManager.getConnection(
                        System.getProperty(CNS_NTAK_PEARL_ZIP_JDBC_URL), System.getProperty(CNS_NTAK_PEARL_ZIP_JDBC_USER), System.getProperty(CNS_NTAK_PEARL_ZIP_JDBC_PASSWORD));
            } else {
                return connection;
            }
    }

    public static class QueryExecutorBuilder {

        private String cacheIdentifier;
        private String identifier;
        private Map<String,Object> parameters = new HashMap<>();
        private Map<String,Function<Object, String>> extractors = new HashMap<>();
        private boolean isRefreshForced = false;

        public QueryExecutorBuilder withCacheIdentifier(String cacheIdentifier) {
            this.cacheIdentifier = cacheIdentifier;
            return this;
        }

        public QueryExecutorBuilder withQueryByIdentifier(String identifier) {
            if (Objects.nonNull(identifier)) {
                this.identifier = identifier;
            }

            return this;
        }

        public QueryExecutorBuilder withParameter(String parameterName, Object value) {
            if (Objects.nonNull(parameterName)) {
                parameters.put(String.format(":%s",parameterName), value);
            }

            return this;
        }

        public QueryExecutorBuilder withParameter(String parameterName, Object value, Function<Object,String> extractor) {
            if (Objects.nonNull(parameterName)) {
                final var parameter = String.format(":%s", parameterName);
                parameters.put(parameter, value);
                extractors.put(parameter, extractor);
            }

            return this;
        }

        public QueryExecutorBuilder withRefreshForced(boolean isRefreshForced) {
            this.isRefreshForced = isRefreshForced;

            return this;
        }

        public QueryExecutor build() {
            // 1. Retrieve QueryDefinition from Map Cache
            QueryDefinition queryDefinition = InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                                                                  .<Map<String,QueryDefinition>>getAdditionalConfig(CK_QUERY_DEFINITION_CACHE)
                                                                  .get()
                                                                  .get(identifier);

            // 2. Validate parameters set against expected parameters. Throw if unfulfilled.
            if (!queryDefinition.getInputParameters().containsAll(parameters.keySet())) {
                // LOG: Invalid parameter set for query: %s. Acceptable parameters are: %s
                throw new RuntimeException(resolveTextKey(LOG_INVALID_QUERY_PARAMETER_SET, queryDefinition.getId(), String.join(",", queryDefinition.getInputParameters())));
            }

            // 3. Initialise QueryExecutor with options and return
            return new QueryExecutor(cacheIdentifier, queryDefinition, isRefreshForced, parameters, extractors);
        }
    }
}
