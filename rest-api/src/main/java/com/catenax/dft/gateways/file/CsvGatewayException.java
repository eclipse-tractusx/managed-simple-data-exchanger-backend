package com.catenax.dft.gateways.file;

public class CsvGatewayException extends RuntimeException {

    public CsvGatewayException(String message) {
        super(message);
    }

    public CsvGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}