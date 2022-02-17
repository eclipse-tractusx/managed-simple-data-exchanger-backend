package com.catenax.dft.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;


@Service
public class CSVStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public CSVStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile multipartFile) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));

        try {
            if (!fileName.endsWith(".csv")) {
                throw new FileStorageException("Unsupported multipartFile. DFT only supports .csv files");
            }

            UUID uuid = UUID.randomUUID();
            fileName = fileName.replace(
                    fileName.substring(0, fileName.lastIndexOf(".") - 1),
                    uuid.toString()
            );
            Path targetLocation = this.fileStorageLocation.resolve(fileName);

            Files.copy(multipartFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return uuid.toString();

        } catch (IOException ex) {
            throw new FileStorageException("Could not store multipartFile " + multipartFile.getOriginalFilename() + ". Please try again!", ex);
        }
    }
}
