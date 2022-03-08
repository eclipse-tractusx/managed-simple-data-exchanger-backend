package com.catenax.dft.entities.usecases;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Aspect {

    private String uuid;
    private String localIdentifiersKey;
    private String localIdentifiersValue;
    private String manufacturingDate;
    private String manufacturingCountry;
    private String manufacturerPartId;
    private String customerPartId;
    private String classification;
    private String nameAtManufacturer;
    private String nameAtCustomer;
}
