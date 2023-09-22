/********************************************************************************
 * Copyright (c) 2023 BMW GmbH
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

package org.eclipse.tractusx.sde.sftp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.sde.agent.entity.CsvUploadConfigEntity;
import org.eclipse.tractusx.sde.agent.repository.CsvUploadConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MetadataProviderImpl implements MetadataProvider {

    @Autowired
    private CsvUploadConfigRepository repository;

    private String metadata = "{\"bpn_numbers\":[\"BPNL00000005PROV\",\"BPNL00000005CONS\",\"TESTVV009\"],\"type_of_access\":\"restricted\",\"usage_policies\":[{\"type\":\"DURATION\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\",\"durationUnit\":\"SECOND\"},{\"type\":\"ROLE\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\"},{\"type\":\"PURPOSE\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\"},{\"type\":\"CUSTOM\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\"}]}";


    @Override
    public String saveMetadata(JsonNode metadata) {
        Optional<CsvUploadConfigEntity> config = repository.findById(CsvUploadConfigEntity.METADATA_CONFIG_ID);
        CsvUploadConfigEntity configEntity;
        if (config.isPresent()) {
            configEntity = config.get();
            configEntity.setContent(metadata.toString());
        } else {
            configEntity = new CsvUploadConfigEntity();
            configEntity.setUuid(CsvUploadConfigEntity.METADATA_CONFIG_ID);
            configEntity.setContent(metadata.toString());
            configEntity.setType(ConfigType.METADATA.toString());
        }
        repository.save(configEntity);
        return configEntity.getUuid();
    }

    @Override
    public JsonNode getMetadata() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Optional<CsvUploadConfigEntity> config = repository.findById(CsvUploadConfigEntity.METADATA_CONFIG_ID);
            if (config.isEmpty()) return objectMapper.readTree(metadata);
            else return objectMapper.readTree(config.get().getContent());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
