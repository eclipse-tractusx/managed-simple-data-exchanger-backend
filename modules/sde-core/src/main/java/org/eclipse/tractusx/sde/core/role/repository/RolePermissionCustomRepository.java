/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.core.role.repository;

import java.util.List;

import org.eclipse.tractusx.sde.core.role.entity.RolePermissionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@Component
public class RolePermissionCustomRepository {

	@Autowired
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	@Modifying
	@Transactional
	public List<RolePermissionEntity> findAll(List<String> role, List<String> permission) {
		Query query = entityManager.createQuery(
				"SELECT p FROM RolePermissionEntity p Where p.sdeRole IN :role and p.sdePermission IN :permission ");
		query.setParameter("role", role);
		query.setParameter("permission", permission);
		return query.getResultList();
	}

}
