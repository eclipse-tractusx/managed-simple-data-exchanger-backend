package com.catenax.dft;

import com.catenax.dft.storage.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class DftApplication {

    public static void main(String[] args) {
        SpringApplication.run(DftApplication.class, args);

    }
}
