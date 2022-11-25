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

package org.eclipse.tractusx.sde.edc.enums;

import lombok.Getter;

@Getter
public enum UsagePolicyType {

    //IDS default use policy start
    PROVIDE_ACCESS("PROVIDE_ACCESS", "Provides data usage without any restrictions"),

    PROHIBIT_ACCESS("PROHIBIT_ACCESS", "Prohibit data usage"),

    N_TIMES_USAGE("N_TIMES_USAGE", "Allows data usage for n times"),

    USAGE_DURING_INTERVAL("USAGE_DURING_INTERVAL", "Provides data usage within a specified time interval"),

    DURATION_USAGE("DURATION_USAGE", "Allows data usage for a specified time period"),

    USAGE_UNTIL_DELETION("USAGE_UNTIL_DELETION", "Must delete after usage timeframe"),

    USAGE_LOGGING("USAGE_LOGGING", "Allows data usage if logged to the Clearing House"),

    USAGE_NOTIFICATION("USAGE_NOTIFICATION", "Allows data usage with notification message"),

    CONNECTOR_RESTRICTED_USAGE("CONNECTOR_RESTRICTED_USAGE", "Allows data usage for a specific connector"),

    SECURITY_PROFILE_RESTRICTED_USAGE("SECURITY_PROFILE_RESTRICTED_USAGE", "Allows data access only for connectors with a specified security level"),

    //EDC default use policy start	
    USE("USE", "Default use p[olicy for EDC");

    private String label;
    private String description;

    private UsagePolicyType(String label, String description) {
        this.label = label;
        this.description = description;
    }

}
