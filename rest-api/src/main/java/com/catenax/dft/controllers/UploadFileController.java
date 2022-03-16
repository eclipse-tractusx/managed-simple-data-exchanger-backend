/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
            csvHandlerOrchestrator.execute(csvContent, processId);
        };

        new Thread(runnable).start();

        return ok().body(processId);
    }
}