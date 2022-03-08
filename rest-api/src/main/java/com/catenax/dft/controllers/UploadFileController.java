package com.catenax.dft.controllers;

import com.catenax.dft.entities.csv.CsvContent;
import com.catenax.dft.gateways.file.CsvGateway;
import com.catenax.dft.usecases.csvHandler.CsvHandlerOrchestrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
public class UploadFileController {

    private final CsvGateway csvGateway;
    private final CsvHandlerOrchestrator csvHandlerOrchestrator;

    public UploadFileController(CsvGateway storageService, CsvHandlerOrchestrator csvHandlerOrchestrator) {
        this.csvGateway = storageService;
        this.csvHandlerOrchestrator = csvHandlerOrchestrator;
    }

    @RequestMapping(value = "/upload")
    public ResponseEntity<String> fileUpload(@RequestParam("file") MultipartFile file) {

        String processId = csvGateway.storeFile(file);

        Runnable runnable = () ->
        {
            CsvContent csvContent = csvGateway.processFile(processId);
            csvHandlerOrchestrator.execute(csvContent);
        };

        new Thread(runnable).start();
        return ok().body(processId);
    }
}