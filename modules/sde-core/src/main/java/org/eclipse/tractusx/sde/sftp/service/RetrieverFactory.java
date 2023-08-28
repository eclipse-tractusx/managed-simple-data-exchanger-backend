package org.eclipse.tractusx.sde.sftp.service;

import org.eclipse.tractusx.sde.sftp.RetrieverI;

import java.io.IOException;

public interface RetrieverFactory {
    /***
     * Successful creation of the retriever means the RetrieverConfiguration was correct
     * and the retriever managed to log in to the remote resource
     * @return retriever
     */
    RetrieverI create() throws IOException;
}
