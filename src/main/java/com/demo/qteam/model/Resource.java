package com.demo.qteam.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Resource {

    private String id;
    private String name;
    private int type; // 0 for file, 1 for folder
}
