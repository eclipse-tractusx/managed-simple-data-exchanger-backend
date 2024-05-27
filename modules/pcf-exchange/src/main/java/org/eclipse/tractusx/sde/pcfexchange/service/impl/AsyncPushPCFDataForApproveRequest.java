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

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.common.model.PagingResponse;
import org.eclipse.tractusx.sde.common.utils.JsonObjectUtility;
import org.eclipse.tractusx.sde.common.utils.LogUtil;
import org.eclipse.tractusx.sde.common.utils.PolicyOperationUtil;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFRequestStatusEnum;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFTypeEnum;
import org.eclipse.tractusx.sde.pcfexchange.request.PcfRequestModel;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class AsyncPushPCFDataForApproveRequest {

	private static final String PRODUCT_ID = "productId";

	private final PCFRepositoryService pcfRepositoryService;

	private final ProxyRequestInterface proxyRequestInterface;

	public void pushPCFDataForApproveRequest(List<JsonObject> originalObjectList, PolicyModel policy) {

		List<String> accessBPNList = PolicyOperationUtil.getAccessBPNList(policy);

		List<JsonObject> csvObjectList= originalObjectList.stream()
				.map(obj-> obj.get("csv").getAsJsonObject())
				.toList();
		
		List<String> productList = csvObjectList.stream()
				.map(ele -> JsonObjectUtility.getValueFromJsonObject(ele, PRODUCT_ID))
				.toList();

		markedPCFDataForPendingProviderRequestAsRequested(productList, csvObjectList);

		PagingResponse pcfData = pcfRepositoryService.getPcfData(
				List.of(PCFRequestStatusEnum.PUSHED, PCFRequestStatusEnum.PUSHED_UPDATED_DATA), PCFTypeEnum.PROVIDER, 0,
				100000);
		List<PcfRequestModel> requestList = (List<PcfRequestModel>) pcfData.getItems();

		if (!requestList.isEmpty()) {

			requestList.forEach(request -> {

				if (productList.contains(request.getProductId()) && accessBPNList.contains(request.getBpnNumber())) {

					String msg = "";

					try {
						request.setStatus(PCFRequestStatusEnum.PUSHING_UPDATED_DATA);

						JsonObject calculatedPCFValue = originalObjectList.stream()
								.filter(ele -> {
									ele = ele.get("csv").getAsJsonObject();
									return request.getProductId().equals(JsonObjectUtility.getValueFromJsonObject(ele, PRODUCT_ID));
								})
								.map(obj-> obj.get("json").getAsJsonObject())
								.findAny()
								.orElseThrow(() -> new NoDataFoundException(
										"No data found for product_id " + request.getProductId()));

						PCFRequestStatusEnum status = pcfRepositoryService.identifyRunningStatus(request.getRequestId(),
								request.getStatus());

						// push api call
						Runnable runnable = () -> proxyRequestInterface.sendNotificationToConsumer(status,
								calculatedPCFValue, request.getProductId(), request.getBpnNumber(),
								request.getRequestId(), "Pushed PCF updated data", false);

						new Thread(runnable).start();

						msg = "PCF request '" + request.getStatus()
								+ "' and asynchronously sending notification to consumer";

					} catch (NoDataFoundException e) {
						msg = "Unable to take action on PCF request becasue pcf calculated value does not exist, please provide/upload PCF value for "
								+ request.getProductId() + ", request Id " + request.getRequestId();
						log.error("Async PushPCFDataForApproveRequest" + msg);
						throw new NoDataFoundException(msg);
					} catch (Exception e) {
						pcfRepositoryService.savePcfStatus(request.getRequestId(), PCFRequestStatusEnum.FAILED);
					}
				}
			});
		} else {
			log.debug("Async PushPCFDataForApproveRequest - No APPROVED request found");
		}

	}

	public void markedPCFDataForPendingProviderRequestAsRequested(List<String> productList,
			List<JsonObject> csvObjectList) {

		PagingResponse pcfData = pcfRepositoryService
				.getPcfData(List.of(PCFRequestStatusEnum.PENDING_DATA_FROM_PROVIDER), PCFTypeEnum.PROVIDER, 0, 1000000);
		List<PcfRequestModel> requestList = (List<PcfRequestModel>) pcfData.getItems();

		if (!requestList.isEmpty()) {
			requestList.forEach(request -> {
				if (productList.contains(request.getProductId())) {
					String msg = "";
					try {
						
						csvObjectList.stream()
								.filter(ele -> request.getProductId().equals(JsonObjectUtility.getValueFromJsonObject(ele, PRODUCT_ID)))
								.findAny()
								.orElseThrow(() -> new NoDataFoundException(
										"No data found for product_id " + request.getProductId()));
						
						pcfRepositoryService.savePcfStatus(request.getRequestId(), PCFRequestStatusEnum.REQUESTED, "PCF data available please take action on request");

					} catch (NoDataFoundException e) {
						msg = "Unable to markedPCFDataForPendingProviderRequestAsRequested becasue pcf calculated value does not exist  "
								+ request.getProductId() + ", requestId " + request.getRequestId();
						log.warn(LogUtil.encode(msg));
					} catch (Exception e) {
						msg = "Unable to markedPCFDataForPendingProviderRequestAsRequested for " + request.getProductId()
								+ ", requestId " + request.getRequestId() + ", becasue " + e.getMessage();
						log.error(LogUtil.encode(msg));
					}
				}
			});
		} else {
			log.debug("Async PushPCFDataForApproveRequest - No 'PENDING_DATA_FROM_PROVIDER' request found");
		}

	}
}