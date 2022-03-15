package com.catenax.dft.entities.csv;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class CsvContent {

    private Set<String> columns;
    private List<String> rows;
}
