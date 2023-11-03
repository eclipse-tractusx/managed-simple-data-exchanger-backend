/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.service;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.sde.agent.ConfigService;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.notification.config.EmailConfiguration;
import org.eclipse.tractusx.sde.notification.manager.EmailManager;
import org.eclipse.tractusx.sde.retrieverl.service.MinioRetrieverFactoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Slf4j
public abstract class MinioBase {
    protected final MyFile file1 = new MyFile("file1.csv", "test 1 content\n");
    protected final MyFile file2 = new MyFile("file2.csv", "test 2 content\n");
    protected final MyFile sampleBatch9 = new MyFile("sample-batch-9.csv", """
            uuid;batch_id;part_instance_id;manufacturing_date;manufacturing_country;manufacturer_part_id;classification;name_at_manufacturer
            urn:uuid:8eea5f45-0823-48ce-a4fc-c3bf1cdfa4c2;NO-159040131155901488695376;PINO-34634534535;2022-02-04T14:48:54.709Z;DEU;37754B7-76;component;Sensor             
            """);
    @Autowired
    CsvHandlerService csvHandlerService;
    @MockBean
    EmailManager emailManager;
    @MockBean
    EmailConfiguration emailConfiguration;
    @Autowired
    ConfigService configService;
    @Autowired
    ApplicationContext context;
    MinioRetrieverFactoryImpl minioRetrieverFactory;

    protected static Supplier<String> getMinioPath(Supplier<String> stringSupplier) {
        return () -> Optional.ofNullable(stringSupplier.get()).filter(Predicate.not(String::isBlank)).map(str -> str.substring(1).concat("/")).orElse("");
    }

    @PostConstruct
    public void init() {
        minioRetrieverFactory = (MinioRetrieverFactoryImpl) context.getBean("minio");
        configService.deleteAllConfig();
    }

    @AfterEach
    public void cleanupMioBucket() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        var config = minioRetrieverFactory.getConfiguration();
        MinioClient minioClient = MinioClient.builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .build();
        for (var item : minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(config.getBucketName())
                        .recursive(true)
                        .build())) {
            var objName = item.get().objectName();
            MinioBase.log.info("Removing {}", objName);
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(config.getBucketName()).object(objName).build());
        }
        minioClient.removeBucket(
                RemoveBucketArgs.builder().bucket(config.getBucketName()).build()
        );
    }

    @BeforeEach
    public void createFilesInToBeProcessedLocation() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        var config = minioRetrieverFactory.getConfiguration();
        MinioClient minioClient = MinioClient
                .builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .build();
        minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(config.getBucketName()).build()
        );
        var getToBeProcessedLocation = MinioBase.getMinioPath(minioRetrieverFactory.getConfiguration()::getToBeProcessedLocation).get();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioRetrieverFactory.getConfiguration().getBucketName())
                        .object(getToBeProcessedLocation + file1.name())
                        .stream(new ByteArrayInputStream(file1.content().getBytes()), file1.content().length(), -1)
                        .build()
        );
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioRetrieverFactory.getConfiguration().getBucketName())
                        .object(getToBeProcessedLocation + file2.name())
                        .stream(new ByteArrayInputStream(file2.content().getBytes()), file2.content().length(), -1)
                        .build()
        );
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioRetrieverFactory.getConfiguration().getBucketName())
                        .object(getToBeProcessedLocation + sampleBatch9.name())
                        .stream(new ByteArrayInputStream(sampleBatch9.content().getBytes()), sampleBatch9.content().length(), -1)
                        .build()
        );
    }

    protected String getFileContent(String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        var config = minioRetrieverFactory.getConfiguration();
        MinioClient minioClient = MinioClient
                .builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .build();
        return new String(minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(config.getBucketName())
                        .object(fileName)
                        .build()
        ).readAllBytes());
    }

    protected record MyFile(
            String name,
            String content
    ) {
    }
}
