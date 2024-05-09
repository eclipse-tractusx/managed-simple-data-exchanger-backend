/********************************************************************************
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022,2024 T-Systems International GmbH
 * Copyright (c) 2022,2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.sde.common.constants;

public class CommonConstants {

	private CommonConstants() {
		throw new IllegalStateException("Constant class");
	}

	public static final String CSV_FILE_EXTENSION = ".csv";
	public static final String SEPARATOR = ";";

	public static final String UPDATED_Y = "Y";
	public static final String DELETED_Y = "Y";

	public static final String PART_INSTANCE_ID = "partInstanceId";
	public static final String MANUFACTURER_PART_ID = "manufacturerPartId";
	public static final String MANUFACTURER_ID = "manufacturerId";
	public static final String CUSTOMER_PART_ID = "customerPartId";
	public static final String ASSET_LIFECYCLE_PHASE = "assetLifecyclePhase";
	public static final String HTTP = "HTTP";
	public static final String HTTPS = "HTTPS";
	public static final String ENDPOINT_PROTOCOL_VERSION = "1.1";
	public static final String PREFIX = "urn:uuid:";

	public static final String INTERFACE_EDC = "EDC";
	public static final String SUB_PROTOCOL = "DSP";
	public static final String SUBMODEL_CONTEXT_URL = "/submodel";

	public static final String AS_PLANNED = "AsPlanned";
	
	public static final String EXTERNAL_REFERENCE = "ExternalReference";
	public static final String GLOBAL_REFERENCE = "GlobalReference";
	public static final String SUBMODEL = "Submodel";
	public static final String BODY_ENCODING = "plain";
	public static final String INTERFACE = "SUBMODEL-3.0";
	
}