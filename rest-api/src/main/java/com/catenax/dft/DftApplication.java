package com.catenax.dft;

import com.catenax.dft.gateways.file.CsvGatewayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        CsvGatewayProperties.class
})
public class DftApplication {

    public static void main(String[] args) {
        SpringApplication.run(DftApplication.class, args);

    }
}
