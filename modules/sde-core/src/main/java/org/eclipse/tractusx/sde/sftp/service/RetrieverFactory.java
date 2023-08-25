package org.eclipse.tractusx.sde.sftp.service;

import org.eclipse.tractusx.sde.sftp.RetrieverConfiguration;
import org.eclipse.tractusx.sde.sftp.RetrieverI;

public interface RetrieverFactory {
    /***
     * Successful creation of the retriever means the RetrieverConfiguration was correct
     * and the retriever managed to log in to the remote resource
     * @param credentials
     * @return retriever
     */
    RetrieverI create(RetrieverConfiguration credentials);
}
