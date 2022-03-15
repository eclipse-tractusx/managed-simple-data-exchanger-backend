package com.catenax.dft;

import com.catenax.dft.gateways.file.CsvGatewayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableConfigurationProperties({
        CsvGatewayProperties.class
})
public class DftApplication {

    public static void main(String[] args) {
        SpringApplication.run(DftApplication.class, args);
    }
}
