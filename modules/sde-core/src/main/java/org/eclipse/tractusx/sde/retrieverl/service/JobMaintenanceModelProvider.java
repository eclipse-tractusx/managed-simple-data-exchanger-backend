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

package org.eclipse.tractusx.sde.retrieverl.service;

import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.sde.agent.ConfigService;
import org.eclipse.tractusx.sde.agent.model.JobMaintenanceModel;
import org.eclipse.tractusx.sde.common.ConfigurationProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobMaintenanceModelProvider implements ConfigurationProvider<JobMaintenanceModel> {

	private final ConfigService configService;

    @Override
    public JobMaintenanceModel getConfiguration() {
        return configService.getConfigurationAsObject(JobMaintenanceModel.class)
                .orElseGet(() -> {
                    var jmp = getDefaultJobMaintenanceModel();
                    saveConfig(jmp);
                    return jmp;
                });
    }

    @Override
    public void saveConfig(JobMaintenanceModel config) {
        configService.saveConfiguration(config);
    }

    private JobMaintenanceModel getDefaultJobMaintenanceModel() {
        return new JobMaintenanceModel(true, true);
    }
}
