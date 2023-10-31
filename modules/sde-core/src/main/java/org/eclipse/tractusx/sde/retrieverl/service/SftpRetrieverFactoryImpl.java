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

package org.eclipse.tractusx.sde.retrieverl.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.tractusx.sde.agent.ConfigService;
import org.eclipse.tractusx.sde.agent.model.SftpConfigModel;
import org.eclipse.tractusx.sde.common.ConfigurableFactory;
import org.eclipse.tractusx.sde.common.ConfigurationProvider;
import org.eclipse.tractusx.sde.common.validators.SpringValidator;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.OptionalInt;

@Service("sftp")
@RequiredArgsConstructor
public class SftpRetrieverFactoryImpl implements ConfigurableFactory<SftpRetriever>, ConfigurationProvider<SftpConfigModel> {

	@Value("${sftp.host}")
	private String host;
	@Value("${sftp.port:22}")
	private int port;
	@Value("${sftp.username}")
	private String username;
	@Value("${sftp.password}")
	private String password;
	@Value("${sftp.location.tobeprocessed}")
	private String toBeProcessed;
	@Value("${sftp.location.inprogress}")
	private String inProgress;
	@Value("${sftp.location.success}")
	private String success;
	@Value("${sftp.location.partialsucess}")
	private String partialSuccess;
	@Value("${sftp.location.failed}")
	private String failed;
	@Value("${sftp.retries:5}")
	private int numberOfRetries;
	@Value("${sftp.retryDelay.from:500}")
	private int retryDelayFrom;
	@Value("${sftp.retryDelayTo:3500}")
	private int retryDelayTo;

	private final ConfigService configService;
	private final CsvHandlerService csvHandlerService;
	private final SpringValidator validator;


	@SneakyThrows
	public SftpRetriever create(OptionalInt port) {
		SftpConfigModel configModel = getConfiguration();
		return new SftpRetriever(csvHandlerService, 
				configModel.getHost(),
				port.orElse(configModel.getPort()),
				configModel.getUsername(), 
				configModel.getPassword(),
				configModel.getAccessKey(),
				configModel.getToBeProcessedLocation(), 
				configModel.getInProgressLocation(),
				configModel.getSuccessLocation(),
				configModel.getPartialSuccessLocation(),
				configModel.getFailedLocation(),
				numberOfRetries,
				retryDelayFrom,
				retryDelayTo
		);

	}

	@Override
	public SftpRetriever create() {
		return create(OptionalInt.empty());
	}

	private SftpConfigModel getDefaultConfig() {
		return validator.validate(SftpConfigModel.builder()
				.host(host)
				.port(port)
				.failedLocation(failed)
				.username(username)
				.password(password)
				.toBeProcessedLocation(toBeProcessed)
				.inProgressLocation(inProgress)
				.partialSuccessLocation(partialSuccess)
				.successLocation(success)
				.build()
		);
	}

	@Override
	public SftpConfigModel getConfiguration() {
		return configService.getConfigurationAsObject(SftpConfigModel.class)
				.orElseGet(() -> {
					var configModel = getDefaultConfig();
					saveConfig(configModel);
					return configModel;
				});
	}

	@Override
	public void saveConfig(SftpConfigModel config) {
		configService.saveConfiguration(config);
	}

	@Override
	public Class<SftpConfigModel> getConfigClass() {
		return SftpConfigModel.class;
	}
}
