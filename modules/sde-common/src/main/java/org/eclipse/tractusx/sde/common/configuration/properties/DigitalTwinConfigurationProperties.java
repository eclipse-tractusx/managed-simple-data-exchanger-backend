/********************************************************************************
 #* Copyright (c) 2024 T-Systems International GmbH
 #* Copyright (c) 2024 Contributors to the Eclipse Foundation
 #*
 #* See the NOTICE file(s) distributed with this work for additional
 #* information regarding copyright ownership.
 #*
 #* This program and the accompanying materials are made available under the
 #* terms of the Apache License, Version 2.0 which is available at
 #* https://www.apache.org/licenses/LICENSE-2.0.
 #*
 #* Unless required by applicable law or agreed to in writing, software
 #* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 #* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 #* License for the specific language governing permissions and limitations
 #* under the License.
 #*
 #* SPDX-License-Identifier: Apache-2.0
 #********************************************************************************/

package org.eclipse.tractusx.sde.common.configuration.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class DigitalTwinConfigurationProperties {

    @Value("${digital-twins.hostname:default}")
    private String digitalTwinsHostname;

    @Value("${digital-twins.managed.thirdparty:false}")
    private boolean digitalTwinsManagedThirdparty;

    @Value("${digital-twins.authentication.url:default}")
    private String digitalTwinsAuthenticationUrl;

    @Value("${digital-twins.authentication.clientId:default}")
    private String digitalTwinsAuthenticationClientId;

    @Value("${digital-twins.authentication.clientSecret:default}")
    private String digitalTwinsAuthenticationClientSecret;

    @Value("${digital-twins.authentication.scope:}")
    private String digitalTwinsAuthenticationScope;

    @Value(value = "${digital-twins.authentication.grantType}")
    private String digitalTwinsAuthenticationGrantType;

    @Value("${digital-twins.registry.uri:/api/v3.0}")
    private String digitalTwinsRegistryPath;

    @Value("${digital-twins.lookup.uri:/api/v3.0}")
    private String digitalTwinsLookupPath;

    @Value(value = "${manufacturerId}")
    private String manufacturerId;

    @Value(value = "${edc.hostname}")
    private String edcHostname;

    @Value(value = "${edc.dsp.endpointpath:/api/v1/dsp}")
    private String edcDspEndpointpath;

    @Value(value = "${edc.dataplane.endpointpath:/api/public}")
    private String edcDataplaneEndpointpath;

    public String getDigitalTwinEdcDspEndpoint() {
        return this.edcHostname+this.edcDspEndpointpath;
    }

    public String getDigitalTwinEdcDataplaneEndpoint() {
        return this.edcHostname+this.edcDataplaneEndpointpath;
    }

}
