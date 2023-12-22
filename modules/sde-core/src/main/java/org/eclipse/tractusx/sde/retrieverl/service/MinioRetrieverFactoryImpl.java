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

package org.eclipse.tractusx.sde.retrieverl.	service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.agent.ConfigService;
import org.eclipse.tractusx.sde.agent.model.MinioConfigModel;
import org.eclipse.tractusx.sde.common.ConfigurableFactory;
import org.eclipse.tractusx.sde.common.ConfigurationProvider;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.validators.SpringValidator;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("objectstorage")
@RequiredArgsConstructor
public class MinioRetrieverFactoryImpl implements ConfigurableFactory<MinioRetriever>, ConfigurationProvider<MinioConfigModel> {

	@Value("${minio.endpoint:}")
	private String endpoint;
	@Value("${minio.access-key:}")
	private String accessKey;
	@Value("${minio.secret-key:}")
	private String secretKey;
	@Value("${minio.bucket-name:}")
	private String bucketName;
	@Value("${minio.location.tobeprocessed:}")
	private String minioToBeProcessed;
	@Value("${minio.location.inprogress:}")
	private String minioInProgress;
	@Value("${minio.location.success:}")
	private String minioSuccess;
	@Value("${minio.location.partialsucess:}")
	private String minioPartialSuccess;
	@Value("${minio.location.failed:}")
	private String minioFailed;

	private final ConfigService configService;
	private final CsvHandlerService csvHandlerService;
	private final SpringValidator validator;


	private String removeFirstSlashForMinio(String path) {
		return path != null && !path.isBlank() && path.charAt(0) == '/' ? path.substring(1) : path;
	}

	@Override
	public MinioRetriever create() throws IOException, ValidationException {
		var objectStorageConfigModel = getConfiguration();
		if(StringUtils.isBlank(objectStorageConfigModel.getEndpoint()))
			throw new ValidationException("Object storage config is empty, unable to process job.");
		return new MinioRetriever(csvHandlerService,
							objectStorageConfigModel.getEndpoint(),
							objectStorageConfigModel.getAccessKey(),
							objectStorageConfigModel.getSecretKey(),
							objectStorageConfigModel.getBucketName(),
							removeFirstSlashForMinio(objectStorageConfigModel.getToBeProcessedLocation()),
							removeFirstSlashForMinio(objectStorageConfigModel.getInProgressLocation()),
							removeFirstSlashForMinio(objectStorageConfigModel.getSuccessLocation()),
							removeFirstSlashForMinio(objectStorageConfigModel.getPartialSuccessLocation()),
							removeFirstSlashForMinio(objectStorageConfigModel.getFailedLocation())
				);
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

	@Override
	public Class<MinioConfigModel> getConfigClass() {
		return MinioConfigModel.class;
	}

	private MinioConfigModel getDefaultConfig() {
		return MinioConfigModel.builder()
				.endpoint(endpoint)
				.accessKey(accessKey)
				.secretKey(secretKey)
				.bucketName(bucketName)
				.failedLocation(minioFailed)
				.toBeProcessedLocation(minioToBeProcessed)
				.inProgressLocation(minioInProgress)
				.partialSuccessLocation(minioPartialSuccess)
				.successLocation(minioSuccess)
				.build()
		;
	}
}
