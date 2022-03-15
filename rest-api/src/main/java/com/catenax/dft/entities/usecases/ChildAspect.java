package com.catenax.dft.entities.usecases;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ChildAspect {

    private String parentIdentifierKey;
    private String parentIdentifierValue;
    private String lifecycleContext;
    private int quantityNumber;
    private String measurementUnitLexicalValue;
}
