package org.eclipse.tractusx.sde.sftp.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.core.utils.TryUtils;
import org.eclipse.tractusx.sde.sftp.RetrieverConfiguration;
import org.eclipse.tractusx.sde.sftp.RetrieverI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class FtpRetriever implements RetrieverI {

    public record FtpConfiguration(
        String url,
        String username,
        String password,
        String toBeProcessedLocation,
        String inProgressLocation,
        String successLocation,
        String partialSuccessLocation,
        String failedLocation
    ) implements RetrieverConfiguration {}

    private final FTPClient ftpClient;
    private final FtpConfiguration ftpConfiguration;
    private final CsvHandlerService csvHandlerService;
    private final Map<String, String> idToPath;

    public static final FTPFileFilter CSV_FILTER = file -> file.isFile() && file.getName().substring(file.getName().lastIndexOf('.')).equalsIgnoreCase(".csv");

    public FtpRetriever(FtpConfiguration ftpConfiguration, CsvHandlerService csvHandlerService) throws IOException {
        this.ftpConfiguration = ftpConfiguration;
        this.csvHandlerService = csvHandlerService;
        ftpClient = new FTPClient();
        //ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        idToPath = Arrays.stream(ensureConnected().listFiles(ftpConfiguration.toBeProcessedLocation(), CSV_FILTER))
                .map(ftpFile -> ftpConfiguration.toBeProcessedLocation() + "/" + ftpFile.getName())
                //.peek(System.out::println)
                .collect(Collectors.toMap(
                        path -> UUID.randomUUID().toString(),
                        Function.identity()
                ));

    }

    private FTPClient ensureConnected() throws IOException {
        if (!ftpClient.isConnected()) {
            connect();
        }
        return ftpClient;
    }

    private void connect() throws IOException {
        ftpClient.connect(ftpConfiguration.url());
        ftpClient.login(ftpConfiguration.username(), ftpConfiguration.password());
        ftpClient.enterLocalPassiveMode();
    }

    private void disconnect() throws IOException {
        if (ftpClient.isConnected()) {
            ftpClient.logout();
            ftpClient.disconnect();
        }
    }

    private void moveTo(String id, String newLocation) throws IOException {
        var newPath = newLocation + "/"+ getFileName(id);
        ensureConnected().rename(idToPath.get(id), newPath);
        idToPath.put(id, newPath);
    }
    public String getFileName(String id) {
        return Optional.ofNullable(idToPath.get(id))
                .map(path -> path.substring(path.lastIndexOf('/') + 1))
                .orElseThrow();
    }

    @Override
    public void setProgress(String id) throws IOException {
        moveTo(id, ftpConfiguration.inProgressLocation());
    }

    @Override
    public void setSuccess(String id) throws IOException {
        moveTo(id, ftpConfiguration.successLocation());
    }

    @Override
    public void setPartial(String id) throws IOException {
        moveTo(id, ftpConfiguration.partialSuccessLocation());
    }

    @Override
    public void setFailed(String id) throws IOException {
        moveTo(id, ftpConfiguration.failedLocation());
    }

    @Override
    public void close() throws Exception {
        disconnect();
        log.debug("Ftps client {} disconnected", ftpConfiguration.url);
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Iterator<String> iterator() {
        var it = idToPath.entrySet().iterator();
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED),
                false
        ).map(next ->
                new Object() {
                    final String id = next.getKey();
                    final String filePath = next.getValue();
                    final File localFile = new File(csvHandlerService.getFilePath(id));
                }
        ).flatMap( o -> TryUtils.tryExec(
                    () -> {
                        try (FileOutputStream fos = new FileOutputStream(o.localFile)) {
                            if (!ensureConnected().retrieveFile(o.filePath, fos)) {
                                throw new Exception("Error when retrieveFile " + o.filePath);
                            }
                            return o.id;
                        }},
                    err -> o.localFile.delete()
                ).stream()
        ).iterator();
    }
}
