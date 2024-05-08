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
package org.eclipse.tractusx.sde.pcfexchange.service;

import org.eclipse.tractusx.sde.common.model.PagingResponse;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFRequestStatusEnum;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFTypeEnum;
import org.eclipse.tractusx.sde.pcfexchange.request.PcfRequestModel;

import com.fasterxml.jackson.databind.JsonNode;

public interface IPCFExchangeService {

	public String actionOnPcfRequestAndSendNotificationToConsumer(PcfRequestModel pcfRequestModel);
	
	public Object requestForPcfDataExistingOffer(String productId, ConsumerRequest consumerRequest);

	public PcfRequestModel savePcfRequestData(String requestId, String productId, String bpnNumber, String message);

	public PagingResponse getPcfData(PCFRequestStatusEnum status, PCFTypeEnum type, Integer page, Integer pageSize);

	public void recievedPCFData(String productId, String bpnNumber, String requestId, String message,
			JsonNode pcfData);

	public Object viewForPcfDataOffer(String requestId);

	public Object requestForPcfNotExistDataOffer(PcfRequestModel pcfRequestModel);

}
