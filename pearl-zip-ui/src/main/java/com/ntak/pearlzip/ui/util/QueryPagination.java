/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;

public class QueryPagination {

    private int offset = 0;
    private int pagination;
    private int count;

    public QueryPagination(int offset, int pagination, int count) {
        this.offset = offset;
        this.pagination = pagination;
        this.count = count;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getPagination() {
        return pagination;
    }

    public void setPagination(int pagination) {
        this.pagination = pagination;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isNextPageAvailable() {
        return count > (offset + 1) * pagination;
    }

    public boolean isPreviousPageAvailable() {
        return offset > 0;
    }
}
