package com.catenax.dft.entities.database;

import lombok.Data;
import org.springframework.lang.Nullable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "childAspect")
@Data
public class ChildAspectEntity {

    @Id
    @GeneratedValue
    private Long Id;
    @Nullable
    private String uuid;
    private String parentIdentifierKey;
    private String parentIdentifierValue;
    private String lifecycleContext;
    private int quantityNumber;
    private String measurementUnitLexicalValue;
}


