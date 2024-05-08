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
package org.eclipse.tractusx.sde.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@Getter
public class SdeCommonProperties {

	//DigitalTwin properties
		@Value("${digital-twins.hostname:default}")
		private  String digitalTwinRegistry;
				
		@Value("${digital-twins.managed.thirdparty:false}")
		private boolean dDTRManagedThirdparty;
		
		@Value("${digital-twins.registry.uri:/api/v3.0}")
		private  String digitalTwinRegistryURI;
		
		@Value("${digital-twins.registry.lookup.uri:/api/v3.0}")
		private  String digitalTwinRegistryLookUpURI;
		
		@Value("${digital-twins.authentication.url:default}")
		private String digitalTwinTokenUrl;
		
		@Value("${digital-twins.authentication.clientId:default}")
		private String digitalTwinClientId;
		
		@Value("${digital-twins.authentication.clientSecret:default}")
		private String digitalTwinClientSecret;
		
		@Value("${digital-twins.authentication.scope:}")
		private String digitalTwinAuthenticationScope;
		
		//manufacturer Id properties
		@Value(value = "${manufacturerId}")
		private String manufacturerId;
}