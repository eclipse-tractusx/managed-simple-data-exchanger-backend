/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.sde.core.csv.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;

import org.eclipse.tractusx.sde.common.entities.csv.CsvContent;
import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.exception.CsvException;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CsvHandlerService {

    public static final String CSV_FILE_EXTENSION = ".csv";
    public static final String SEPARATOR = ";";
    private final Path fileStorageLocation;


    public CsvHandlerService(CsvConfigurationProperties csvGatewayProperties) {
        this.fileStorageLocation = Paths.get(csvGatewayProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new CsvException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile multipartFile) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try {
            InputStream stream = multipartFile.getInputStream();
            
            if (fileName.isBlank()) {
                throw new ValidationException("Request does not contains valid CSV file");
            }
            
            if (!fileName.endsWith(CSV_FILE_EXTENSION)) {
                throw new ValidationException("Unsupported multipartFile. SDE only supports .csv files");
            }

            UUID uuid = UUID.randomUUID();
            
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
        		throw new IllegalArgumentException("Invalid csv filename");
        	}
            
            fileName = fileName.replace(
                    fileName.substring(0, fileName.lastIndexOf(".")),
                    uuid.toString()
            );
            Path targetLocation = this.fileStorageLocation.resolve(fileName);

            if (!targetLocation.startsWith(this.fileStorageLocation + File.separator)) {
        		throw new IllegalArgumentException("Invalid csv filename");
        	}
            
            Files.copy(stream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            stream.close();

            return uuid.toString();

        } catch (IOException ex) {
            throw new CsvException("Could not store multipartFile " + multipartFile.getOriginalFilename() + ". Please try again!", ex);
        }
    }

    @SneakyThrows
    public CsvContent processFile(String fileName) {
        log.debug(String.format("Start processing '%s.csv' file", fileName));
        String filePath = getFilePath(fileName);
        File file = new File(filePath);
        if (!file.exists()) {
            throw new CsvException("no such file");
        }

        int numberOfRows = 0;
        List<String> fileColumns;
        ArrayList<RowData> rows = new ArrayList<>();
        CsvContent csvContent = new CsvContent();

        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            String row = scanner.nextLine();

            if (numberOfRows == 0) {
                fileColumns = Arrays.stream(row.split(SEPARATOR)).toList();
                csvContent.setColumns(fileColumns);

            } else {
                rows.add(new RowData(numberOfRows + 1, row));
            }
            numberOfRows++;
        }

        scanner.close();
        csvContent.setRows(rows);

        if (deleteFile(fileName)) {
            log.debug(String.format("File %s deleted", fileName));
        }

        log.debug(String.format("File '%s.csv' is fully processed. Total of lines: %s", fileName, numberOfRows));
        return csvContent;
    }

    public boolean deleteFile(String fileName) throws IOException {
        File file = new File(getFilePath(fileName));
        Files.delete(file.toPath());
        return true;
    }

    public String getFilePath(String fileName) {
        String fileNameWithExtension = fileName + CSV_FILE_EXTENSION;
        Path targetLocation = this.fileStorageLocation.resolve(fileNameWithExtension);

        return targetLocation.toString();
    }
}