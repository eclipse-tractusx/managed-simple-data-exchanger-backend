package com.catenax.dft.rest;

import com.catenax.dft.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
public class UploadFileController {

    private final StorageService storageService;

    @Autowired
    public UploadFileController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/")
    public String fileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

        storageService.storeFile(file);

        return ":redirect/";
    }
}
