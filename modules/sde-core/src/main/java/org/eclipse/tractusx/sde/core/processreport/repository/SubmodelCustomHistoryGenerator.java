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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.utils.JsonObjectUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubmodelCustomHistoryGenerator {

	private final EntityManager entityManager;

	@Autowired
	private DataSourceProperties dataSourceProperties;

	ObjectMapper objectMapper = new ObjectMapper();

	@SuppressWarnings("unchecked")
	@Modifying
	@Transactional
	@SneakyThrows
	public List<List<String>> findAllSubmodelCsvHistory(List<String> colNames, String tableEntityName,
			String processId) {
		List<List<String>> records = new LinkedList<>();
		try {
			String colname = String.join(",", colNames);
			Query query = entityManager
					.createNativeQuery("SELECT " + colname + " FROM " + tableEntityName + " as p Where p.process_id=?");
			query.setParameter(1, processId);
			List<Object[]> resultList = query.getResultList();

			if (resultList.isEmpty())
				throw new NoDataFoundException(String.format("No data found for processid %s ", processId));
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

	@Modifying
	@Transactional
	@SneakyThrows
	public List<JsonObject> findAllSubmoduleAsJsonList(List<String> colNames, String tableEntityName, String processId,
			boolean isNotDeleted) {
		List<JsonObject> records = new LinkedList<>();

		try {
			String columns = String.join(",", colNames);
			String isDeleted = "";
			if (isNotDeleted)
				isDeleted = "  AND p.deleted!= 'Y'";

			Query query = entityManager.createNativeQuery(
					"SELECT " + columns + " FROM " + tableEntityName + " as p Where p.process_id=? " + isDeleted + "");
			query.setParameter(1, processId);
			List<Object[]> resultList = query.getResultList();

			if (resultList.isEmpty())
				throw new NoDataFoundException(String.format("No data found for processid %s ", processId));

			for (Object[] objectArray : resultList) {
				records.add(getJsonNodes(colNames, objectArray));
			}
		} catch (Exception e) {
			throw new ServiceException("Unable to process dyanamic table read for history as Json " + e.getMessage());
		}
		return records;
	}

	@Modifying
	@Transactional
	@SneakyThrows
	public JsonObject readCreatedTwinsDetails(List<String> colNames, String tableEntityName, String uuid,
			String pkColomn) {

		JsonObject jsonObject = null;
		try {
			String columns = String.join(",", colNames);
			Query query = entityManager.createNativeQuery(
					"SELECT " + columns + " FROM " + tableEntityName + " as p Where p." + pkColomn + "=? ");
			query.setParameter(1, uuid);
			List<Object[]> resultList = query.getResultList();

			if (resultList.isEmpty())
				throw new NoDataFoundException(String.format("No data found for %s ", uuid));

			for (Object[] objectArray : resultList) {
				jsonObject = getJsonNodes(colNames, objectArray);
			}
		} catch (Exception e) {
			throw new ServiceException("Unable to process dyanamic table read for as Json " + e.getMessage());
		}
		return jsonObject;
	}

	private JsonObject getJsonNodes(List<String> colNames, Object[] objectArray) {
		JsonObject innerObject = new JsonObject();
		int colCounter = 0;
		for (Object object : objectArray) {
			innerObject.addProperty(colNames.get(colCounter++), object == null ? "" : object.toString());
		}
		return innerObject;
	}

	@Modifying
	@Transactional
	@SneakyThrows
	public JsonNode saveSubmodelData(List<String> colNames, String tableEntityName, String processId,
			JsonNode submodelData) {
		try {
			StringBuilder colname = new StringBuilder();
			StringBuilder parameters = new StringBuilder();
			colNames.forEach(ele -> {
				if (parameters.isEmpty()) {
					colname.append(ele);
					parameters.append("?");
				} else {
					colname.append("," + ele);
					parameters.append(",?");
				}
			});

			Query query = entityManager.createNativeQuery(
					"INSERT INTO " + tableEntityName + " (" + colname + ") VALUES (" + parameters + ")");
			AtomicInteger i = new AtomicInteger(1);
			colNames.forEach(col -> query.setParameter(i.getAndIncrement(),
					JsonObjectUtility.getValueFromJsonObject(submodelData, col)));

			query.executeUpdate();

		} catch (Exception e) {
			throw new ServiceException("Unable to process dyanamic table insert " + e.getMessage());
		}
		return null;
	}

	@Modifying
	@Transactional
	@SneakyThrows
	public void saveAspectWithDeleted(String uuid, String tableEntityName, String pkColomn) {
		try {
			Query query = entityManager
					.createNativeQuery("UPDATE " + tableEntityName + " set deleted='Y' WHERE " + pkColomn + "= ? )");
			query.setParameter(1, uuid);
			query.executeUpdate();
		} catch (Exception e) {
			throw new ServiceException("Unable to marked delete " + e.getMessage());
		}
	}

	@Modifying
	@Transactional
	@SneakyThrows
	public int countUpdatedRecordCount(String tableEntityName, String updated, String processId) {
		try {
			Query query = entityManager.createNativeQuery(
					"select count(ae) from " + tableEntityName + " as p WHERE ae.updated = ? and process_id= ? )");
			query.setParameter(1, updated);
			query.setParameter(2, processId);
			return query.getMaxResults();
		} catch (Exception e) {
			throw new ServiceException("Unable to count updated records " + e.getMessage());
		}
	}

	@SneakyThrows
	public void checkTableIfNotExist(JsonObject schema, List<String> columns, String tableName, String pkCol) {
		boolean isTableNotExist = false;
		try (Connection con = DriverManager.getConnection(dataSourceProperties.getUrl(),
				dataSourceProperties.getUsername(), dataSourceProperties.getPassword())) {
			StringBuilder colname = new StringBuilder();

			columns.forEach(ele -> {
				if (colname.isEmpty()) {
					colname.append(ele);
				} else {
					colname.append("," + ele);
				}
			});
			try (PreparedStatement pmt = con
					.prepareStatement("SELECT " + colname + " FROM " + tableName + " as p LIMIT 1")) {
				ResultSet rs = pmt.executeQuery();
				if (rs.isBeforeFirst()) {
					log.debug(tableName + " is exist, so ignore create table");
				}
			}
		} catch (Exception e) {
			isTableNotExist = true;
			log.error(tableName + " is not exist, so creating table");
		}

		if (isTableNotExist) {
			createTable(schema, columns, tableName, pkCol);
		}
	}

	@SneakyThrows
	public void createTable(JsonObject schema, List<String> columns, String tableName, String pkCol) {

		try (Connection con = DriverManager.getConnection(dataSourceProperties.getUrl(),
				dataSourceProperties.getUsername(), dataSourceProperties.getPassword())) {

			JsonObject items = schema.get("items").getAsJsonObject().get("properties").getAsJsonObject();

			StringBuilder colname = new StringBuilder();
			columns.forEach(ele -> {
				JsonObject jObject = JsonObjectUtility.getValueFromJsonObjectAsObject(items, ele);
				String dataType = "";
				if (isNumberTypeField(jObject)) {
					dataType = "float8";
				} else {
					dataType = "varchar(255)";
				}
				if (pkCol.equals(ele)) {
					colname.append(ele + " " + dataType + " NOT NULL, ");
				} else {
					colname.append(ele + " " + dataType + "  NULL, ");
				}
			});

			colname.append(" CONSTRAINT " + tableName + "_pkey PRIMARY KEY (" + pkCol + ")");

			try (Statement stmt = con.createStatement()) {
				stmt.execute("CREATE TABLE " + tableName + " (" + colname + ")");
				log.info(tableName + " created successfully");
			}

		} catch (Exception e) {
			log.error("Error in table create " + e);
			throw new ServiceException("Unable to create table for " + tableName);
		}
	}

	private boolean isNumberTypeField(JsonObject jObject) {

		if (jObject != null && !jObject.isJsonNull()) {
			JsonElement jsonElement = JsonParser.parseString("number");
			if (jObject.get("type") != null && jObject.get("type").isJsonArray()) {
				JsonArray types = jObject.get("type").getAsJsonArray();
				return types.contains(jsonElement);
			}
			return false;
		}
		return false;
	}
}
