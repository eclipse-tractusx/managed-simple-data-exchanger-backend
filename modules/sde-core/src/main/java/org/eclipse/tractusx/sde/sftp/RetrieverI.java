package org.eclipse.tractusx.sde.sftp;

import java.io.IOException;

public interface RetrieverI extends Iterable<String>, AutoCloseable {
    void setProgress(String id) throws IOException;
    void setSuccess(String id) throws IOException;
    void setPartial(String id) throws IOException;
    void setFailed(String id) throws IOException;
    String getFileName(String id);
}
