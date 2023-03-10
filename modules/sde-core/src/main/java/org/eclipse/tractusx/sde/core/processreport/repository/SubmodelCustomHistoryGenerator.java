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
package org.eclipse.tractusx.sde.core.processreport.repository;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.SneakyThrows;

@Component
public class SubmodelCustomHistoryGenerator {

	@Autowired
	private EntityManager entityManager;

	ObjectMapper objMapper = new ObjectMapper();

	@SuppressWarnings("unchecked")
	@Modifying
	@Transactional
	@SneakyThrows
	public List<List<String>> findAllSubmodelCsvHistory(String colname, String tableEntityName, String processId) {
		List<List<String>> records = new LinkedList<>();
		try {
			Query query = entityManager
					.createNativeQuery("SELECT " + colname + " FROM " + tableEntityName + " as p Where p.process_id=?");
			query.setParameter(1, processId);
			List<Object[]> resultList = query.getResultList();

			for (Object[] objectArray : resultList) {
				List<String> list = new LinkedList<>();
				for (Object object : objectArray) {
					list.add(object == null ? "" : object.toString());
				}
				records.add(list);
			}
		} catch (Exception e) {
			throw new ServiceException("Unable to process dyanamic table read for history " + e.getMessage());
		}
		return records;
	}
}
