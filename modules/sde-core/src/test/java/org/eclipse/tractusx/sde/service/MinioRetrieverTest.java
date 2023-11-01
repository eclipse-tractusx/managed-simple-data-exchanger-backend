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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;


@Slf4j
public abstract class MinioRetrieverTest {

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

	private record MyFile(
			String name,
			String content
	){};

	private final MyFile file1 = new MyFile("file1.csv", "test 1 content\n");
	private final MyFile file2 = new MyFile("file2.csv", "test 2 content\n");

	private static Supplier<String> getMinioPath(Supplier<String> stringSupplier) {
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
		for(var item: minioClient.listObjects(
		ListObjectsArgs.builder()
				.bucket(config.getBucketName())
				.recursive(true)
				.build())) {
			var objName = item.get().objectName();
			log.info("Removing {}", objName);
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
		var getToBeProcessedLocation = getMinioPath(minioRetrieverFactory.getConfiguration()::getToBeProcessedLocation).get();
		minioClient.putObject(
				PutObjectArgs.builder()
						.bucket(minioRetrieverFactory.getConfiguration().getBucketName())
						.object( getToBeProcessedLocation + file1.name())
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
	}

	private String getFileContent(String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
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

	@Test
	public void testMoveFileAround() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
		var config = minioRetrieverFactory.getConfiguration();
		var contentSet = new HashSet<String>();
		try (var minio = minioRetrieverFactory.create()) {
			for (String fileId : minio) {
				final var filePath = Path.of(csvHandlerService.getFilePath(fileId));
				final var retrievedContent = Files.readString(filePath);
				minio.setProgress(fileId);
				Assertions.assertEquals(
						retrievedContent,
						getFileContent(getMinioPath(config::getInProgressLocation).get() + minio.getFileName(fileId))
				);
				minio.setSuccess(fileId);
				Assertions.assertEquals(
						retrievedContent,
						getFileContent(getMinioPath(config::getSuccessLocation).get() + minio.getFileName(fileId))
				);
				minio.setFailed(fileId);
				Assertions.assertEquals(
						retrievedContent,
						getFileContent(getMinioPath(config::getFailedLocation).get() + minio.getFileName(fileId))
				);
				minio.setPartial(fileId);
				Assertions.assertEquals(
						retrievedContent,
						getFileContent(getMinioPath(config::getPartialSuccessLocation).get() + minio.getFileName(fileId))
				);
				contentSet.add(Files.readString(filePath));
				Files.delete(filePath);
			}
			Assertions.assertEquals(Set.of(file1.content(), file2.content()), contentSet);
		}
	}

}