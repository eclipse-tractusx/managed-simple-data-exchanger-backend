/********************************************************************************
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.tractusx.sde.EnableMinio;
import org.eclipse.tractusx.sde.agent.model.SchedulerConfigModel;
import org.eclipse.tractusx.sde.agent.model.SchedulerType;
import org.eclipse.tractusx.sde.bpndiscovery.handler.BPNDiscoveryUseCaseHandler;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsUtility;
import org.eclipse.tractusx.sde.retrieverl.service.ActiveStorageMediaProvider;
import org.eclipse.tractusx.sde.retrieverl.service.ProcessRemoteCsv;
import org.eclipse.tractusx.sde.retrieverl.service.SchedulerConfigService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootTest
@EnableMinio
@Execution(ExecutionMode.SAME_THREAD)
@WithMockUser(username = "Admin", authorities = { "Admin" })
@ActiveProfiles("miniotest-empty-tobeprocessed")
public class SchedulerMinioTest extends MinioBase{

	@Autowired
	SchedulerConfigService schedulerConfigService;
	@MockBean
	DigitalTwinsFacilitator digitalTwinsFacilitator;
	@MockBean
	DigitalTwinsUtility digitalTwinsUtility;
	@MockBean
	BPNDiscoveryUseCaseHandler bPNDiscoveryUseCaseHandler;
	@Autowired
	ApplicationContext applicationContext;
	@Autowired
	ActiveStorageMediaProvider activeStorageMediaProvider;
	@SpyBean
	ProcessRemoteCsv processRemoteCsv;

	@Test
	public void testScheduler() throws ServiceException, ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
		Mockito.when(digitalTwinsFacilitator.shellLookup(any())).thenReturn(List.of());
		@SuppressWarnings("unchecked") ArgumentCaptor<Map<String, Object>> emailContentCaptor = ArgumentCaptor.forClass(Map.class);
		var time = LocalTime.now().plus(Duration.ofMinutes(1));
		var timeStr = time.format(DateTimeFormatter.ofPattern("HH:mm"));
		var schedulerConfig = SchedulerConfigModel.builder()
				.type(SchedulerType.DAILY)
				.time(timeStr)
				.build();
		schedulerConfigService.saveConfig(schedulerConfig);
		Mockito.verify(emailManager, Mockito.timeout(80000).only()).sendEmail(emailContentCaptor.capture(), any(), any());
		var reportStr = emailContentCaptor.getValue().get("content").toString();
		
		var pattern = Pattern.compile("<tr>.*?" + Pattern.quote(sampleBatch9.name()) + ".*?</tr>");
		var matcher = pattern.matcher(reportStr);
		Assertions.assertTrue(matcher.find());
		
		var notPolicyFoundpattern = Pattern.compile("<tr>.*?Not found.*?</tr>");
		var notFoundPolicymatcher = notPolicyFoundpattern.matcher(reportStr);
		Assertions.assertTrue(notFoundPolicymatcher.find());
		
		var sampleBatch9Report = matcher.group();
		Assertions.assertTrue(sampleBatch9Report.contains("SUCCESS"));
		var minioConfig = minioRetrieverFactory.getConfiguration();
		
		Assertions.assertEquals(
				sampleBatch9.content(),
				getFileContent(getMinioPath(minioConfig::getSuccessLocation).get() + sampleBatch9.name())
		);
		Assertions.assertEquals(
				file1.content(),
				getFileContent(getMinioPath(minioConfig::getFailedLocation).get() + file1.name())
		);
		Assertions.assertEquals(
				file2.content(),
				getFileContent(getMinioPath(minioConfig::getFailedLocation).get() + file2.name())
		);
	}


}
