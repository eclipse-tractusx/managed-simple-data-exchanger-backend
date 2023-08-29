package org.eclipse.tractusx.sde.service;

import org.eclipse.tractusx.sde.core.csv.service.CsvConfigurationProperties;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.sftp.service.FtpRetriever;
import org.junit.Test;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FtpRetrieverTest {

    @Test
    public void testFtps() throws Exception {
        var cvsProp = new CsvConfigurationProperties();
        var dir = Files.createTempDirectory("tmpDirPrefix");
        dir.toFile().deleteOnExit();
        cvsProp.setUploadDir(dir.toFile().getAbsolutePath());
        var csvHandlerService = new CsvHandlerService(cvsProp);
        FtpRetriever.FtpConfiguration ftpConfig = new FtpRetriever.FtpConfiguration(
                "192.168.1.50",
                "test",
                "ntcn123",
                "/home/test/tobe",
                "/home/test/inprogress",
                "/home/test/success",
                "partial",
                "failed"
        );
        List<String> ids = new ArrayList<>();
        try(var ftps = new FtpRetriever(ftpConfig, csvHandlerService)) {
            for (String fileId: ftps) {
                System.out.println(fileId);
                ids.add(fileId);
            }
            for (String id : ids) {
                Files.copy(dir.resolve(id + ".csv"), System.out);
            }
        }
    }

}