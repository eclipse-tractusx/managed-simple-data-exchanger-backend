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

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.tractusx.sde.agent.entity.ConfigEntity;
import org.eclipse.tractusx.sde.agent.model.ConfigType;
import org.eclipse.tractusx.sde.agent.model.MinioConfigModel;
import org.eclipse.tractusx.sde.agent.repository.AutoUploadAgentConfigRepository;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.sftp.RetrieverI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "retriever.name", havingValue="minio")
public class MinioRetrieverFactoryImpl implements RetrieverFactory {

	@Value("${minio.endpoint}")
	private String endpoint;
	@Value("${minio.access-key}")
	private String accessKey;
	@Value("${minio.secret-key}")
	private String secretKey;
	@Value("${minio.bucket-name}")
	private String bucketName;
	@Value("${minio.location.tobeprocessed}")
	private String toBeProcessed;
	@Value("${minio.location.inprogress}")
	private String inProgress;
	@Value("${minio.location.success}")
	private String success;
	@Value("${minio.location.partialsucess}")
	private String partialSuccess;
	@Value("${minio.location.failed}")
	private String failed;

	private final ConfigService configService;
	private final AutoUploadAgentConfigRepository configRepository;
	private final CsvHandlerService csvHandlerService;


	@Override
	public RetrieverI create() {
		MinioConfigModel configModel = configService.getMinioConfiguration();
		return new MinioRetriever(csvHandlerService,
				configModel.getEndpoint(),
				configModel.getAccessKey(),
				configModel.getSecretKey(),
				configModel.getBucketName(),
				configModel.getToBeProcessedLocation(), 
				configModel.getInProgressLocation(),
				configModel.getSuccessLocation(),
				configModel.getPartialSuccessLocation(),
				configModel.getFailedLocation()
		);
	}

	@SneakyThrows
	@Override
	public void saveDefaultConfig() {
		Optional<ConfigEntity> config =
				configRepository.findAllByType(ConfigType.MINIO.toString());
		if (config.isEmpty()) {
			MinioConfigModel minioConfigModel = MinioConfigModel.builder()
					.endpoint(endpoint)
					.accessKey(accessKey)
					.secretKey(secretKey)
					.bucketName(bucketName)
					.failedLocation(failed)
					.toBeProcessedLocation(toBeProcessed)
					.inProgressLocation(inProgress)
					.partialSuccessLocation(partialSuccess)
					.successLocation(success).build();
			configService.saveConfiguration(ConfigType.MINIO, minioConfigModel);
		}
	}
}
