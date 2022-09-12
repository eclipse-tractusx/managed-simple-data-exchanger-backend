/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.catenax.dft.facilitator;

import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

public class AbstractEDCStepsHelper {

	@Value("${edc.consumer.apikeyheader}")
	private String edcApiKeyHeader;

	@Value("${edc.consumer.apikey}")
	private String edcApiKeyValue;

	public Map<String,String>  getAuthHeader() {
		 Map<String,String> requestHeader =new HashMap<>();
		 requestHeader.put(edcApiKeyHeader, edcApiKeyValue);
		return requestHeader;
	}

}
