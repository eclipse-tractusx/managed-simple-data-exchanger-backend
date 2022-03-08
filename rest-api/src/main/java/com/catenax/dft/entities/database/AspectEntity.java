package com.catenax.dft.entities.database;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "aspect")
@Entity
@Data
public class AspectEntity {

    @Id
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
