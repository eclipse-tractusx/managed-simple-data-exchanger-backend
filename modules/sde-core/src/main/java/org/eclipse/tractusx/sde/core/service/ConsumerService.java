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

package org.eclipse.tractusx.sde.core.service;

import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Service
@AllArgsConstructor
public class ConsumerService {

	private final ConsumerControlPanelService consumerControlPanelService;

	@SneakyThrows
	public ResponseEntity<Object> subscribeAndDownloadDataOffers(ConsumerRequest consumerRequest) {
		Map<String, Object> subscribeAndDownloadDataOffers = consumerControlPanelService
				.subscribeAndDownloadDataOffers(consumerRequest);
		
		subscribeAndDownloadDataOffers.entrySet().stream().forEach(entry->{
			entry.getValue();
//			JsonNode jsonTree = null;
//			Builder csvSchemaBuilder = CsvSchema.builder();
//			JsonNode firstObject = jsonTree.elements().next();
//			firstObject.fieldNames().forEachRemaining(fieldName -> {csvSchemaBuilder.addColumn(fieldName);} );
//			CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();
//			CsvMapper csvMapper = new CsvMapper();
//			
//			csvMapper.writerFor(JsonNode.class)
//			  .with(csvSchema)
//			  .writeValue(new File(""), jsonTree);
		});
		
		
		return ok().body(subscribeAndDownloadDataOffers);
	}

	public ResponseEntity<Object> downloadFileFromEDCUsingifAlreadyTransferStatusCompleted(List<String> assetId) {
		return ok().body(consumerControlPanelService.downloadFileFromEDCUsingifAlreadyTransferStatusCompleted(assetId));
	}

}
