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
package org.eclipse.tractusx.sde.core.pcf.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.eclipse.tractusx.sde.common.entities.SubmodelJsonRequest;
import org.eclipse.tractusx.sde.common.enums.PCFRequestStatusEnum;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.validators.ValidatePolicyTemplate;
import org.eclipse.tractusx.sde.core.pcf.entity.PcfRequestEntity;
import org.eclipse.tractusx.sde.core.pcf.entity.PcfRequestMapper;
import org.eclipse.tractusx.sde.core.pcf.repository.PcfRequestRepository;
import org.eclipse.tractusx.sde.core.pcf.request.PcfRequestModel;
import org.springframework.stereotype.Service;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class PcfExchangeService {
	
	private final PcfRequestRepository pcfRequestRepository;
	private final PcfRequestMapper pcfMapper;
	
	
	public void findPcfData(String productId, String bpnNumber, String requestId, String message) {
		
		
	}

	public void processPcfSubmodel(String productId, String bpnNumber, String requestId, String message, @Valid @ValidatePolicyTemplate SubmodelJsonRequest pcfSubmodelJsonRequest) {
		
		String processId = UUID.randomUUID().toString();
		//process pcf data as per request for product Id.
		
		
		
	}
	
	
	public PcfRequestModel savePcfRequestData(String requestId, String productId, String bpnNumber, String message) {

		PcfRequestModel pcfRequest = PcfRequestModel.builder()
				.processId(UUID.randomUUID().toString())
				.requestId(requestId)
				.productId(productId)
				.bpnNumber(bpnNumber)
				.message(message)
				.build();

		return savePcfRequest(pcfRequest);
	}
	
	@SneakyThrows
	public PcfRequestModel savePcfRequest(PcfRequestModel request) {

		if (isPcfAlreadyRequestedforProduct(request.getRequestId(), request.getProductId())) {
			PcfRequestEntity pcfRequestEntity = pcfMapper.mapFrom(request);
			pcfRequestEntity.setStatus(PCFRequestStatusEnum.REQUESTED);
			pcfRequestEntity.setRequestedTime(LocalDateTime.now());
			pcfRequestEntity.setLastUpdatedTime(LocalDateTime.now());
			pcfRequestRepository.save(pcfRequestEntity);
			log.info("'" + request.getProductId() + "' pcf request saved in the database successfully");
			
			//Email notification process here to notify caller of pcf request
			 
			return request;
		} else
			throw new ValidationException(
					String.format("'%s' pcf details already requested", request.getProductId()));
	}
	
	public boolean isPcfAlreadyRequestedforProduct(String requestId, String productId) {

		if (StringUtils.isBlank(requestId) && StringUtils.isBlank(productId) )
			throw new ValidationException("The requestId or productId should not be not null or empty");

		return pcfRequestRepository.findByRequestIdAndProductId(requestId, productId).isEmpty();
	}

}
