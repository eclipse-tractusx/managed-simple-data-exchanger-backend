package com.catenax.dft.entities.usecases;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessReportPageResponse {
    private int pageNumber;
    private int pageSize;
    private long totalItems;
    private List<ProcessReport> items;

}
