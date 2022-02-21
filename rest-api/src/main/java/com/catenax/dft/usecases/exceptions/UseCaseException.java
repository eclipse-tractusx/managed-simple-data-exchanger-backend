package com.catenax.dft.usecases.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
public class UseCaseException extends Exception {

    private String useCaseName;
    private String errorMessage;

    @Override
    public String toString() {
        return "UseCaseException{" +
                "useCaseName='" + useCaseName + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
