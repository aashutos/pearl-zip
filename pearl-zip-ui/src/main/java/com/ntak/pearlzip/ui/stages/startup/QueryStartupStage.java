/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.stages.startup;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.AbstractStartupStage;
import com.ntak.pearlzip.ui.util.internal.QueryDefinition;
import com.ntak.pearlzip.ui.util.internal.QueryResult;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

/**
 *  Sets up named SQL queries for use by PearlZip.
 *
 *  @author Aashutos Kakshepati
 */
public class QueryStartupStage extends AbstractStartupStage {
    @Override
    public void executeProcess() throws Exception {
        ////////////////////////////////////////////
        ///// Named Query Load ////////////////////
        //////////////////////////////////////////

        Path STORE_ROOT = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                              .<Path>getAdditionalConfig(CK_STORE_ROOT)
                                              .get();

        final var queryDataCache = new ConcurrentHashMap<String,QueryResult>();
        final var queryDefinitionCache = new ConcurrentHashMap<String,QueryDefinition>();

        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_QUERY_RESULT_CACHE, queryDataCache);
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_QUERY_DEFINITION_CACHE, queryDefinitionCache);

        // 1. Extract all files to internal query directory
        Path defQueryPath = Paths.get(STORE_ROOT.toAbsolutePath()
                                                .toString(), "db-cache", ".internal");
        String resource = "queries";
        String moduleName = "com.ntak.pearlzip.ui";
        com.ntak.pearlzip.ui.util.internal.JFXUtil.extractResources(defQueryPath, moduleName, resource);

        // 2. Load query definitions into cache
        Files.list(defQueryPath).forEach(c -> {
            XMLInputFactory f = XMLInputFactory.newFactory();
            try (
                    InputStream fis = Files.newInputStream(c)
            ) {
                XMLStreamReader sr = f.createXMLStreamReader(fis);
                XmlMapper mapper = new XmlMapper(f);
                QueryDefinition queryDef = mapper.readValue(sr, QueryDefinition.class);
                queryDefinitionCache.put(queryDef.getId(), queryDef);
            } catch(IOException | XMLStreamException e) {
            }
        });

        // 2. Load cached query results
        Path defDataPath = Paths.get(STORE_ROOT.toAbsolutePath()
                                               .toString(), "db-cache", "data");
        Files.createDirectories(defDataPath);
        Files.list(defDataPath).forEach(c -> {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(c))) {
                QueryResult result = (QueryResult)ois.readObject();
                queryDataCache.put(c.getFileName().toString(), result);
            } catch(IOException | ClassNotFoundException e) {
            }
        });
    }
}
