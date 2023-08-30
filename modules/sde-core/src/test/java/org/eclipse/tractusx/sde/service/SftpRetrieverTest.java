package org.eclipse.tractusx.sde.service;

import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.sftp.service.SftpRetrieverFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Testcontainers
public class SftpRetrieverTest {

    @Autowired
    CsvHandlerService csvHandlerService;

    @Autowired
    SftpRetrieverFactory sftpRetrieverFactory;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4")
            .withDatabaseName("dft")
            .withUsername("root")
            .withPassword("P@ssword21");

    @Container
    static GenericContainer<?> sftp = new GenericContainer<>("dvasunin/sftp:latest")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("sftp/", 0777),
                    "/home/foo/upload/sftp"
            )
            .withExposedPorts(22)
            .withCommand("foo:pass:::upload");

    @DynamicPropertySource
    static void postresqlProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("sftp.location.tobeprocessed", () -> "/upload/sftp/tobe");
        registry.add("sftp.username", () -> "foo");
        registry.add("sftp.password", ()-> "pass");
        registry.add("sftp.port", () -> sftp.getMappedPort(22));
    }



    @Test
    public void testFtps() throws Exception {
        List<String> ids = new ArrayList<>();
        try(var sftp = sftpRetrieverFactory.create()) {
            for (String fileId: sftp) {
                System.out.println(fileId);
                ids.add(fileId);
            }
            for (String id : ids) {
                //sftp.setSuccess(id);
                Files.copy(Path.of(csvHandlerService.getFilePath(id)), System.out);
            }
        }
    }

}