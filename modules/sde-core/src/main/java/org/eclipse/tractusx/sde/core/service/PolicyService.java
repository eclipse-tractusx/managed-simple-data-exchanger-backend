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

package org.eclipse.tractusx.sde.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.tractusx.sde.common.entities.SubmodelFileRequest;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.core.policy.entity.PolicyEntity;
import org.eclipse.tractusx.sde.core.policy.repository.PolicyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository repository;
    private ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public String savePolicy(SubmodelFileRequest request) {
        Optional<PolicyEntity> optionalPolicy = repository.findByName(request.getPolicyName());
        if (optionalPolicy.isEmpty()) {
            PolicyEntity policy = new PolicyEntity();
            policy.setName(request.getPolicyName());
            policy.setUuid(UUID.randomUUID().toString());
            try {
                policy.setContent(objectMapper.writeValueAsString(request));
            } catch (JsonProcessingException e) {
                throw new ServiceException("Error while saving the policy");
            }
            repository.save(policy);
            return policy.getUuid();
        } else return "Such policy name already exists";
    }

    @SneakyThrows
    public SubmodelFileRequest getPolicy(String uuid) {
        try {
            Optional<PolicyEntity> optionalPolicy = repository.findById(uuid);
            if (optionalPolicy.isEmpty()) return null;
            else {
                PolicyEntity policy = optionalPolicy.get();
                return objectMapper.readValue(policy.getContent(), SubmodelFileRequest.class);
            }
        } catch (JsonProcessingException e) {
            throw new ServiceException("Error while getting the policy details");
        }
    }

    public List<SubmodelFileRequest> getAllPolicies() {
        List<PolicyEntity> policyEntities = repository.findAll();
        return policyEntities.stream()
                .map(policyEntity ->
                {
                    try {
                        return objectMapper.readValue(policyEntity.getContent(), SubmodelFileRequest.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    public void deletePolicy(String uuid) {
        repository.deleteById(uuid);
    }

    public String updatePolicy(String uuid, SubmodelFileRequest request) throws JsonProcessingException {
        Optional<PolicyEntity> optionalPolicy = repository.findById(uuid);
        if (optionalPolicy.isEmpty()) return null;
        else {
            PolicyEntity policy = optionalPolicy.get();
            policy.setName(request.getPolicyName());
            policy.setContent(objectMapper.writeValueAsString(request));
            return policy.getUuid();
        }
    }
}
