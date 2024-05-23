/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.pcfexchange.service.impl;

import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.model.PagingResponse;
import org.eclipse.tractusx.sde.common.utils.LogUtil;
import org.eclipse.tractusx.sde.pcfexchange.entity.PcfRequestEntity;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFRequestStatusEnum;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFTypeEnum;
import org.eclipse.tractusx.sde.pcfexchange.mapper.PcfExchangeMapper;
import org.eclipse.tractusx.sde.pcfexchange.repository.PcfRequestRepository;
import org.eclipse.tractusx.sde.pcfexchange.request.PcfRequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PCFRepositoryService {

	private static final String SUCCESS = "SUCCESS";
	private final PcfRequestRepository pcfRequestRepository;
	private final PcfExchangeMapper pcfMapper;

	public PcfRequestModel savePcfRequestData(String requestId, String productId, String bpnNumber, String message,
			PCFTypeEnum type, PCFRequestStatusEnum status, String remark) {

		PcfRequestModel pojo = PcfRequestModel.builder().requestId(requestId).productId(productId).bpnNumber(bpnNumber)
				.status(status).message(message).type(type).requestedTime(Instant.now().getEpochSecond())
				.lastUpdatedTime(Instant.now().getEpochSecond()).remark(remark).build();

		PcfRequestEntity entity = pcfMapper.mapFrom(pojo);

		return pcfMapper.mapFrom(pcfRequestRepository.save(entity));
	}

	public PCFRequestStatusEnum updatePCFPushStatus(PCFRequestStatusEnum status, String requestId, String sendNotificationStatus) {
		
		if ((PCFRequestStatusEnum.APPROVED.equals(status) || PCFRequestStatusEnum.PUSHING_DATA.equals(status))
				&& SUCCESS.equalsIgnoreCase(sendNotificationStatus)) {
			status = PCFRequestStatusEnum.PUSHED;
			sendNotificationStatus ="PCF data successfuly pushed";
		} else if (PCFRequestStatusEnum.PUSHING_UPDATED_DATA.equals(status)
				&& SUCCESS.equalsIgnoreCase(sendNotificationStatus)) {
			status = PCFRequestStatusEnum.PUSHED_UPDATED_DATA;
			sendNotificationStatus ="PCF updated data successfuly pushed";
		} else if ((PCFRequestStatusEnum.REJECTED.equals(status)
				|| PCFRequestStatusEnum.SENDING_REJECT_NOTIFICATION.equals(status))
				&& SUCCESS.equalsIgnoreCase(sendNotificationStatus)) {
			status = PCFRequestStatusEnum.REJECTED;
			sendNotificationStatus = "PCF request rejected successfuly";
		} else if (PCFRequestStatusEnum.APPROVED.equals(status)
				|| PCFRequestStatusEnum.FAILED_TO_PUSH_DATA.equals(status)
				|| PCFRequestStatusEnum.PUSHING_DATA.equals(status)
				|| PCFRequestStatusEnum.PUSHING_UPDATED_DATA.equals(status)) {
			status = PCFRequestStatusEnum.FAILED_TO_PUSH_DATA;
		} else if (PCFRequestStatusEnum.REJECTED.equals(status)
				|| PCFRequestStatusEnum.SENDING_REJECT_NOTIFICATION.equals(status)
				|| PCFRequestStatusEnum.FAILED_TO_SEND_REJECT_NOTIFICATION.equals(status))
			status = PCFRequestStatusEnum.FAILED_TO_SEND_REJECT_NOTIFICATION;
		else {
			status = PCFRequestStatusEnum.FAILED;
		}

		savePcfStatus(requestId, status, sendNotificationStatus);
		
		return status;
	}
	
	public PCFRequestStatusEnum identifyRunningStatus(String requestId, PCFRequestStatusEnum status) {
		
		boolean isApproval = PCFRequestStatusEnum.APPROVED.equals(status)
				|| PCFRequestStatusEnum.FAILED_TO_PUSH_DATA.equals(status);

		boolean isRejection = PCFRequestStatusEnum.REJECTED.equals(status)
				|| PCFRequestStatusEnum.FAILED_TO_SEND_REJECT_NOTIFICATION.equals(status);

		if (isApproval) {
			status = PCFRequestStatusEnum.PUSHING_DATA;
		} else if (isRejection) {
			status = PCFRequestStatusEnum.SENDING_REJECT_NOTIFICATION;
		}
		
		savePcfStatus(requestId, status);
		
		return status;
	}

	@SneakyThrows
	public PcfRequestEntity savePcfStatus(String requestId, PCFRequestStatusEnum status) {
		return savePcfStatus(requestId, status, null);
	}
	
	@SneakyThrows
	public List<PcfRequestEntity> findByProductId(String productId) {
		return pcfRequestRepository.findByProductId(productId);
	}
	
	@SneakyThrows
	public PcfRequestEntity savePcfStatus(String requestId, PCFRequestStatusEnum status, String remark) {

		PcfRequestEntity pcfRequestEntity = pcfRequestRepository.getReferenceById(requestId);
		pcfRequestEntity.setLastUpdatedTime(Instant.now().getEpochSecond());
		pcfRequestEntity.setStatus(status);
		
		if(StringUtils.isNotBlank(remark))
			pcfRequestEntity.setRemark(remark);
		
		log.info(LogUtil.encode("'" + pcfRequestEntity.getProductId() + "' pcf request saved in the database successfully as " +
				status));
		
		pcfRequestRepository.save(pcfRequestEntity);
		return pcfRequestEntity;

	}

	public PagingResponse getPcfData(List<PCFRequestStatusEnum> status, PCFTypeEnum type, Integer page, Integer pageSize) {

		Page<PcfRequestEntity> result = null;
		if (status == null || status.isEmpty()) {
			result = pcfRequestRepository.findByType(PageRequest.of(page, pageSize), type);
		} else {
			result = pcfRequestRepository.findByTypeAndStatusIn(PageRequest.of(page, pageSize), type, status);
		}

		List<PcfRequestModel> requestList = result.stream().map(pcfMapper::mapFrom).toList();

		return PagingResponse.builder().items(requestList).pageSize(result.getSize()).page(result.getNumber())
				.totalItems(result.getTotalElements()).build();
	}
}