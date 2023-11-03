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

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;


@Slf4j
public abstract class MinioRetrieverTest extends MinioBase {
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
			Assertions.assertEquals(Set.of(file1.content(), file2.content(), sampleBatch9.content()), contentSet);
		}
	}

}