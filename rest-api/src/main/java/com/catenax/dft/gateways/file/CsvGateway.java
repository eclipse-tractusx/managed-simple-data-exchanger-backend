package com.catenax.dft.gateways.file;

import com.catenax.dft.entities.csv.CsvContent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CsvGateway {

    public static final String CSV_FILE_EXTENSION = ".csv";
    public static final String SEPARATOR = ";";
    private static final String PARENT_ASPECT_COLUMNS = "local_identifiers_key;local_identifiers_value;manufacturing_date;manufactoring_country;manufacturer_part_id;customer_part_id;classification;name_at_manufacturer;name_at_customer";
    private final Path fileStorageLocation;


    @Autowired
    public CsvGateway(CsvGatewayProperties csvGatewayProperties) {
        this.fileStorageLocation = Paths.get(csvGatewayProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new CsvGatewayException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile multipartFile) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try {
            InputStream stream = multipartFile.getInputStream();
            if (!fileName.endsWith(CSV_FILE_EXTENSION)) {
                throw new CsvGatewayException("Unsupported multipartFile. DFT only supports .csv files");
            }

            UUID uuid = UUID.randomUUID();
            fileName = fileName.replace(
                    fileName.substring(0, fileName.lastIndexOf(".")),
                    uuid.toString()
            );
            Path targetLocation = this.fileStorageLocation.resolve(fileName);


            Files.copy(stream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            stream.close();

            return uuid.toString();

        } catch (IOException ex) {
            throw new CsvGatewayException("Could not store multipartFile " + multipartFile.getOriginalFilename() + ". Please try again!", ex);
        }
    }

    @SneakyThrows
    public CsvContent processFile(String fileName) {


        log.debug(String.format("Start processing '%s.csv' file", fileName));
        String filePath = getFilePath(fileName);
        File file = new File(filePath);
        if (!file.exists()) {
            throw new CsvGatewayException("no such file");
        }

        int numberOfRows = 0;
        Set<String> fileColumns;
        ArrayList<String> rows = new ArrayList<>();
        CsvContent csvContent = new CsvContent();

        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            String row = scanner.nextLine();

            if (numberOfRows == 0) {
                fileColumns = Arrays.stream(row.split(";")).collect(Collectors.toSet());
                csvContent.setColumns(fileColumns);

            } else {
                rows.add(row);
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

    public boolean deleteFile(String fileName) {
        File file = new File(getFilePath(fileName));

        return file.delete();
    }

    public String getFilePath(String fileName) {
        String fileNameWithExtension = fileName + CSV_FILE_EXTENSION;
        Path targetLocation = this.fileStorageLocation.resolve(fileNameWithExtension);

        return targetLocation.toString();
    }
}