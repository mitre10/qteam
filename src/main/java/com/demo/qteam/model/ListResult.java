package com.demo.qteam.model;

import java.util.List;

public class ListResult<T> {

    private List<T> resources;
    private String cursor; // Used for pagination

    public ListResult() {
    }

    public ListResult(List<T> resources, String cursor) {
        this.resources = resources;
        this.cursor = cursor;
    }

    public List<T> getResources() {
        return resources;
    }

    public String getCursor() {
        return cursor;
    }
}
