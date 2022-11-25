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
package org.eclipse.tractusx.sde.submodels.pap.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class PartAsPlannedConstants {
	
	@Value(value = "${manufacturerId}")
	public String manufacturerId;
	
	@Value(value = "${edc.hostname}")
	public String edcEndpoint;
	
	public static final String MANUFACTURER_PART_ID = "manufacturerPartId";
	public static final String MANUFACTURER_ID = "manufacturerId";
	public static final String CUSTOMER_PART_ID = "customerPartId";
	public static final String ASSET_LIFECYCLE_PHASE = "assetLifecyclePhase";
	public static final String AS_PLANNED = "AsPlanned";
	public static final String HTTP = "HTTP";
	public static final String HTTPS = "HTTPS";
	public static final String ENDPOINT_PROTOCOL_VERSION = "0.0.1-SNAPSHOT";
	public static final String PREFIX = "urn:uuid:";
	public static final String DELETED_Y = "Y";

}
