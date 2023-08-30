package org.eclipse.tractusx.sde.service;

import org.eclipse.tractusx.sde.EnableTestContainers;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.sftp.service.SftpRetrieverFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@EnableTestContainers
public class SftpRetrieverTest {

    @Autowired
    CsvHandlerService csvHandlerService;

    @Autowired
    SftpRetrieverFactory sftpRetrieverFactory;

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