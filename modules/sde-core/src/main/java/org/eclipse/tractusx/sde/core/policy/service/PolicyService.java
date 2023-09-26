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

package org.eclipse.tractusx.sde.core.policy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.eclipse.tractusx.sde.common.entities.SubmodelPolicyRequest;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.model.PagingResponse;
import org.eclipse.tractusx.sde.core.policy.entity.PolicyEntity;
import org.eclipse.tractusx.sde.core.policy.entity.PolicyMapper;
import org.eclipse.tractusx.sde.core.policy.repository.PolicyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyService {

	private final PolicyRepository repository;
	private final PolicyMapper policyMapper;

	public SubmodelPolicyRequest savePolicy(SubmodelPolicyRequest request) {
		String policyId = UUID.randomUUID().toString();
		return savePolicy(policyId, request);
	}

	public SubmodelPolicyRequest updatePolicy(String uuid, SubmodelPolicyRequest request) {
		return savePolicy(uuid, request);
	}

	@SneakyThrows
	public SubmodelPolicyRequest savePolicy(String uuid, SubmodelPolicyRequest request) {

		if (isPolicyNameValid(uuid, request.getPolicyName())) {
			PolicyEntity policy = policyMapper.mapFrom(request);
			policy.setUuid(uuid);
			policy.setLastUpdatedTime(LocalDateTime.now());
			request.setLastUpdatedTime(LocalDateTime.now());
			request.setUuid(uuid);
			repository.save(policy);
			log.info("'"+request.getPolicyName()+"' policy saved in the database successfully");
			return request;
		} else
			throw new ValidationException(
 					String.format("'%s' such policy name already exists", request.getPolicyName()));
	}

	public boolean isPolicyNameValid(String id, String name) {

		if (StringUtils.isBlank(name))
			throw new ValidationException("The policy name should not be not null or empty");

		return repository.findByIdAndName(id, name).isEmpty();
	}

	public SubmodelPolicyRequest getPolicy(String uuid) {
		return policyMapper.mapFrom(
				repository.findByUuid(uuid).orElseThrow(() -> new NoDataFoundException("No data found uuid " + uuid)));

	}

	public SubmodelPolicyRequest getPolicyByName(String policyName) {
		return policyMapper.mapFrom(repository.findByPolicyName(policyName).orElse(null));
	}

	public List<SubmodelPolicyRequest> findMatchingPolicyBasedOnFileName(String policyName) {
		List<PolicyEntity> findByPolicyNameLike = repository.findMatchingPolicyBasedOnFileName(policyName);
		return findByPolicyNameLike.stream().map(policyMapper::mapFrom).toList();
	}

	public PagingResponse getAllPolicies(Integer page, Integer pageSize) {
		Page<PolicyEntity> result = repository
				.findAll(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "lastUpdatedTime")));
		List<SubmodelPolicyRequest> reports = result.stream().map(policyMapper::mapFrom).toList();
		return PagingResponse.builder().items(reports).pageSize(result.getSize()).page(result.getNumber())
				.totalItems(result.getTotalElements()).build();

	}

	public void deletePolicy(String uuid) {
		if (getPolicy(uuid) != null)
			repository.deleteByUuid(uuid);
		else
			throw new NoDataFoundException("The policy not found for delete");
	}
}