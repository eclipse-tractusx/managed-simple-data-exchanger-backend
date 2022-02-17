package com.catenax.dft.rest;

import com.catenax.dft.storage.CSVStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class UploadFileController {

    private final CSVStorageService storageService;

    @Autowired
    public UploadFileController(CSVStorageService storageService) {
        this.storageService = storageService;
    }

    @RequestMapping(value="/upload")
    public ResponseEntity<String> fileUpload(@RequestParam("file") MultipartFile file) {

        String processId = storageService.storeFile(file);
        return ok().body(processId);
    }
}
