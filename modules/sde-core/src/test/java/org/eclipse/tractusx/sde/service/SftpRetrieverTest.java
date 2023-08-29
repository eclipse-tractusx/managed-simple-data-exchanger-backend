package org.eclipse.tractusx.sde.service;

import org.eclipse.tractusx.sde.core.csv.service.CsvConfigurationProperties;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.sftp.service.SftpRetriever;
import org.junit.Test;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SftpRetrieverTest {

    @Test
    public void testFtps() throws Exception {
        var cvsProp = new CsvConfigurationProperties();
        var dir = Files.createTempDirectory("tmpDirPrefix");
        dir.toFile().deleteOnExit();
        cvsProp.setUploadDir(dir.toFile().getAbsolutePath());
        var csvHandlerService = new CsvHandlerService(cvsProp);
        SftpRetriever.SshConfiguration sshConfiguration = new SftpRetriever.SshConfiguration(
                "192.168.1.50",
                "test",
                "ntcn123",
                null,
                "/home/test/tobe",
                "/home/test/inprogress",
                "/home/test/success",
                "partial",
                "failed"
        );
        List<String> ids = new ArrayList<>();
        try(var sftp = new SftpRetriever(sshConfiguration, csvHandlerService)) {
            for (String fileId: sftp) {
                System.out.println(fileId);
                ids.add(fileId);
            }
            for (String id : ids) {
                //sftp.setSuccess(id);
                Files.copy(dir.resolve(id + ".csv"), System.out);
            }
        }
    }

}