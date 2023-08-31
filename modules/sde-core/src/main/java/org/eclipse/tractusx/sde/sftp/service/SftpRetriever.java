package org.eclipse.tractusx.sde.sftp.service;

import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.core.utils.TryUtils;
import org.eclipse.tractusx.sde.sftp.RetrieverI;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@RequiredArgsConstructor
public class SftpRetriever implements RetrieverI {
    private ChannelSftp channelSftp;
    private final Session session;
    private final CsvHandlerService csvHandlerService;
    private final Map<String, String> idToPath;
    private final String inProgressLocation;
    private final String successLocation;
    private final String partialSuccessLocation;
    private final String failedLocation;
    private final String host;

    public SftpRetriever(CsvHandlerService csvHandlerService,
                         String host,
                         int port,
                         String username,
                         String password,
                         String pKey,
                         String toBeProcessedLocation,
                         String inProgressLocation,
                         String successLocation,
                         String partialSuccessLocation,
                         String failedLocation
                         ) throws JSchException, SftpException {
        this.csvHandlerService = csvHandlerService;
        JSch jsch = new JSch();
        if (pKey != null) {
            jsch.addIdentity(host + "-agent", pKey.getBytes(), null, null);
        }
        session = jsch.getSession(username, host, port);
        if (password != null) {
            session.setPassword(password);
        }
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("PreferredAuthentications", "publickey,password");
        session.connect();
        channelSftp = (ChannelSftp) session.openChannel("sftp");
        idToPath = ensureConnected().ls(toBeProcessedLocation).stream()
                .filter(lsEntry -> !lsEntry.getAttrs().isDir())
                .filter(lsEntry -> lsEntry.getFilename().toLowerCase().endsWith(".csv"))
                .map(lsEntry -> toBeProcessedLocation + "/" + lsEntry.getFilename())
                .collect(Collectors.toMap(
                        path -> UUID.randomUUID().toString(),
                        Function.identity()
                ));
        this.inProgressLocation = inProgressLocation;
        this.successLocation = successLocation;
        this.partialSuccessLocation = partialSuccessLocation;
        this.failedLocation = failedLocation;
        this.host = host;
    }

    private ChannelSftp ensureConnected() throws JSchException {
        if (!session.isConnected()) {
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
        }
        if (!channelSftp.isConnected()) {
            channelSftp.connect();
        }
        return channelSftp;
    }

    private void disconnect() {
        if (channelSftp.isConnected()) {
            channelSftp.disconnect();
        }
        if (session.isConnected()) {
            session.disconnect();
        }
    }

    private void moveTo(String id, String newLocation) throws IOException {
        try {
            var newPath = newLocation + "/" + getFileName(id);
            ensureConnected().rename(idToPath.get(id), newPath);
            idToPath.put(id, newPath);
        } catch (JSchException | SftpException e) {
            throw new IOException(e);
        }
    }
    public String getFileName(String id) {
        return Optional.ofNullable(idToPath.get(id))
                .map(path -> path.substring(path.lastIndexOf('/') + 1))
                .orElseThrow();
    }

    @Override
    public void setProgress(String id) throws IOException {
        moveTo(id, inProgressLocation);
    }

    @Override
    public void setSuccess(String id) throws IOException {
        moveTo(id, successLocation);
    }

    @Override
    public void setPartial(String id) throws IOException {
        moveTo(id, partialSuccessLocation);
    }

    @Override
    public void setFailed(String id) throws IOException {
        moveTo(id, failedLocation);
    }

    @Override
    public void close() {
        disconnect();
        log.debug("Ftps client {} disconnected", host);
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
                            Files.copy(ensureConnected().get(o.filePath), o.localFile.toPath());
                            return o.id;
                        },
                        err -> o.localFile.delete()
                ).stream()
        ).iterator();
    }
}
