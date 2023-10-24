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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.sde.agent.ConfigService;
import org.eclipse.tractusx.sde.agent.model.MinioConfigModel;
import org.eclipse.tractusx.sde.agent.repository.AutoUploadAgentConfigRepository;
import org.eclipse.tractusx.sde.common.ConfigurableFactory;
import org.eclipse.tractusx.sde.common.ConfigurationProvider;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MinioRetrieverFactoryImpl implements ConfigurableFactory<MinioRetriever>, ConfigurationProvider<MinioConfigModel> {

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
	private final ObjectMapper mapper;


	@Override
	public MinioRetriever create() {
		var configModel = getConfiguration();
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

	@Override
	public Class<MinioRetriever> getCreatedClass() {
		return MinioRetriever.class;
	}

	@Override
	public MinioConfigModel getConfiguration() {
		return configService.getConfigurationAsObject(MinioConfigModel.class)
				.orElseGet(() -> {
					var minioConfigModel = getDefaultConfig();
					saveConfig(minioConfigModel);
					return minioConfigModel;
				});
	}

	@Override
	public void saveConfig(MinioConfigModel config) {
		configService.saveConfiguration(config);
	}

	private MinioConfigModel getDefaultConfig() {
		return MinioConfigModel.builder()
				.endpoint(endpoint)
				.accessKey(accessKey)
				.secretKey(secretKey)
				.bucketName(bucketName)
				.failedLocation(failed)
				.toBeProcessedLocation(toBeProcessed)
				.inProgressLocation(inProgress)
				.partialSuccessLocation(partialSuccess)
				.successLocation(success)
				.build();
	}
}
