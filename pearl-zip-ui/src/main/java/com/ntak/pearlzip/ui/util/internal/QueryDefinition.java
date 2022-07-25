/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util.internal;

import java.util.List;

/**
 *  POJO encapsulating a simple SQL query to help set up execution.
 *
 *  @author Aashutos Kakshepati
 */
public class QueryDefinition {
    private String id;
    private String query;
    private List<String> outputColumns;
    private List<String> inputParameters;

    public QueryDefinition() {
        super();
    }

    public QueryDefinition(String id, String query, List<String> outputColumns, List<String> inputParameters) {
        this.id = id;
        this.query = query;
        this.outputColumns = outputColumns.stream().toList();
        this.inputParameters = inputParameters.stream().toList();
    }

    public String getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public List<String> getOutputColumns() {
        return outputColumns;
    }

    public List<String> getInputParameters() {
        return inputParameters;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setOutputColumns(List<String> outputColumns) {
        this.outputColumns = outputColumns;
    }

    public void setInputParameters(List<String> inputParameters) {
        this.inputParameters = inputParameters;
    }
}
