/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.sde.sftp.service;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.common.utils.TryUtils;
import org.eclipse.tractusx.sde.sftp.RetrieverI;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class MinioRetriever implements RetrieverI {
    private final MinioClient minioClient;
    private final CsvHandlerService csvHandlerService;
    private final Map<String, String> idToPath;
    private final Map<String, String> idToPolicy;
    private final String inProgressLocation;
    private final String successLocation;
    private final String partialSuccessLocation;
    private final String failedLocation;
    private final String bucketName;



    public MinioRetriever(CsvHandlerService csvHandlerService, String endpoint, String accessKey, String secretKey, String bucketName,
                          String toBeProcessedLocation, String inProgressLocation, String successLocation, String partialSuccessLocation, String failedLocation) {
        this.csvHandlerService = csvHandlerService;
        this.inProgressLocation = inProgressLocation;
        this.successLocation = successLocation;
        this.partialSuccessLocation = partialSuccessLocation;
        this.failedLocation = failedLocation;
        this.bucketName = bucketName;

        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        idToPath = StreamSupport.stream(minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(toBeProcessedLocation+"/")
                        .recursive(false)
                .build()).spliterator(), false
        ).flatMap(r -> TryUtils.tryExec(r::get, TryUtils.IGNORE()).stream())
                .filter(Predicate.not(Item::isDir))
                .map(Item::objectName)
                .filter(name -> name.toLowerCase().endsWith(".csv"))
                .collect(Collectors.toMap(path -> UUID.randomUUID().toString(), Function.identity()));
        idToPolicy = new ConcurrentHashMap<>();
    }

    private void moveTo(String id, String newLocation) throws IOException {
        try {
            var sourceObjectKey = idToPath.get(id);
            var newPath = newLocation + "/" + getFileName(id);
            // Copy the object within the same bucket
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(newPath)
                    .source(CopySource.builder()
                            .bucket(bucketName)
                            .object(sourceObjectKey)
                            .build())
                    .build());

            // Remove the source object if you want to move and not just copy
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(sourceObjectKey)
                    .build());
            idToPath.put(id, newPath);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public String getFileName(String id) {
        return Optional.ofNullable(idToPath.get(id)).map(path -> path.substring(path.lastIndexOf('/') + 1))
                .orElseThrow();
    }

    @Override
    public void setPolicyName(String id, String policyName) {
        idToPolicy.put(id, policyName);
    }

    @Override
    public String getPolicyName(String id) {
        return Optional.ofNullable(idToPolicy.get(id)).map(path -> path.substring(path.lastIndexOf('/') + 1))
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
        log.debug("Minio client is closing");
    }

    @Override
    public Iterator<String> iterator() {
        return idToPath.entrySet().stream()
                .map(next -> new Object() {
                    final String id = next.getKey();
                    final String filePath = next.getValue();
                    final File localFile = new File(csvHandlerService.getFilePath(id));
                }).flatMap(o -> TryUtils.tryExec(
                        () -> {
                            log.info("Fetching data from " + bucketName + " bucket, for file: " + o.filePath);
                            GetObjectArgs args = GetObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(o.filePath)
                                    .build();
                            GetObjectResponse res = minioClient.getObject(args);
                            log.info("file fetched: "+res.object());
                            Files.copy(res, o.localFile.toPath());
                            return o.id;
                        }, err -> o.localFile.delete()).stream()
                ).iterator();
    }

    @Override
    public int size() {
        return idToPath.size();
    }
}
