package com.demo.qteam.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ListResult<T> {

    private List<T> resources;
    private String cursor; // Used for pagination
}
