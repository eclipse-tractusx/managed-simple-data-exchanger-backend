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

package org.eclipse.tractusx.sde.controllers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eclipse.tractusx.sde.EnableTestContainers;
import org.eclipse.tractusx.sde.agent.model.ConfigType;
import org.eclipse.tractusx.sde.agent.repository.AutoUploadAgentConfigRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONObject;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@EnableTestContainers
@ActiveProfiles("test")
@WithMockUser(username = "Admin", authorities = { "Admin" })
class AutoUploadAgentConfigControllerTest {

	@Autowired
	private MockMvc mvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	AutoUploadAgentConfigRepository ftpsConfigRepository;


	@Test
	void testSaveSFTPConfig() throws Exception {

		mvc.perform(MockMvcRequestBuilders.put("/sftp").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(getJsonBody()))).andExpect(status().isOk());

		mvc.perform(MockMvcRequestBuilders.get("/sftp").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		Assertions.assertEquals(ConfigType.SFTP.toString(),
				ftpsConfigRepository.findAllByType(ConfigType.SFTP.toString()).get().getType());
	}

	@Test
	void testSaveSchedulerHourlyTypeConfig() throws Exception {

		mvc.perform(MockMvcRequestBuilders.put("/scheduler").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(getJsonSchedulerHourBody()))).andExpect(status().isOk());

		mvc.perform(MockMvcRequestBuilders.get("/scheduler")).andExpect(status().isOk());

		Assertions.assertEquals(ConfigType.SCHEDULER.toString(),
				ftpsConfigRepository.findAllByType(ConfigType.SCHEDULER.toString()).get().getType());
	}

	@Test
	void testSaveSchedulerDailyTypeConfig() throws Exception {

		mvc.perform(MockMvcRequestBuilders.put("/scheduler").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(getJsonSchedulerDailyBody()))).andExpect(status().isOk());

		mvc.perform(MockMvcRequestBuilders.get("/scheduler")).andExpect(status().isOk());

		Assertions.assertEquals(ConfigType.SCHEDULER.toString(),
				ftpsConfigRepository.findAllByType(ConfigType.SCHEDULER.toString()).get().getType());
	}

	@Test
	void testSaveSchedulerWeeklyTypeConfig() throws Exception {

		mvc.perform(MockMvcRequestBuilders.put("/scheduler").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(getJsonSchedulerWeeklyBody()))).andExpect(status().isOk());

		mvc.perform(MockMvcRequestBuilders.get("/scheduler").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		Assertions.assertEquals(ConfigType.SCHEDULER.toString(),
				ftpsConfigRepository.findAllByType(ConfigType.SCHEDULER.toString()).get().getType());
	}

	private JSONObject getJsonBody() {
		JSONObject json = new JSONObject();
		json.put("host", "127.0.0.1");
		json.put("port", 22);
		json.put("username", "foo");
		json.put("password", "pass");
		json.put("toBeProcessedLocation", "/upload/sftp/tobe");
		json.put("failedLocation", "/upload/sftp/failed");
		json.put("partialSuccessLocation", "/upload/sftp/partial");
		json.put("successLocation", "/upload/sftp/success");
		json.put("inProgressLocation", "/upload/sftp/inprogress");
		return json;
	}

	private JSONObject getJsonSchedulerHourBody() {
		JSONObject json = new JSONObject();
		json.put("type", "HOURLY");
		json.put("time", "2");
		return json;
	}

	private JSONObject getJsonSchedulerDailyBody() {
		JSONObject json = new JSONObject();
		json.put("type", "DAILY");
		json.put("time", "13:30");
		return json;
	}

	private JSONObject getJsonSchedulerWeeklyBody() {
		JSONObject json = new JSONObject();
		json.put("type", "WEEKLY");
		json.put("day", "1");
		json.put("time", "13:30");
		return json;
	}

}
