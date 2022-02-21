package com.catenax.dft.gateways.file;

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

@Slf4j
@Service
public class CsvGateway {

    public static final String CSV_FILE_EXTENSION = ".csv";
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
    public List<String> processFile(String fileName) {

        ArrayList<String> resultData = new ArrayList<>();
        log.debug(String.format("Start processing '%s.csv' file", fileName));
        String filePath = getFilePath(fileName);
        File file = new File(filePath);
        if (!file.exists()) {
            throw new CsvGatewayException("no such file");
        }

        Scanner scanner = new Scanner(file);
        int numberOfRows = 0;
        while (scanner.hasNextLine()) {
            String row = scanner.nextLine();
            if (numberOfRows != 0) {
                resultData.add(row);
            }

            numberOfRows++;
        }
        scanner.close();

        if (deleteFile(fileName)) {
            log.debug(String.format("File %s deleted", fileName));
        }

        log.debug(String.format("File '%s.csv' is fully processed. Total of lines: %s", fileName, numberOfRows));
        return resultData;
    }

    public boolean deleteFile(String fileName) {
        File file = new File(getFilePath(fileName));

        return file.delete();
    }

    public String getFilePath(String fileName) {
        String fileNameWithExtension = new StringBuffer()
                .append(fileName)
                .append(CSV_FILE_EXTENSION)
                .toString();
        Path targetLocation = this.fileStorageLocation.resolve(fileNameWithExtension);

        return targetLocation.toString();
    }
}