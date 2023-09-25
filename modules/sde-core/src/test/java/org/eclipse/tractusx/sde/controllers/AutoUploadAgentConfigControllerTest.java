package org.eclipse.tractusx.sde.controllers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eclipse.tractusx.sde.EnableTestContainers;
import org.eclipse.tractusx.sde.agent.model.ConfigType;
import org.eclipse.tractusx.sde.agent.repository.AutoUploadAgentConfigRepository;
import org.eclipse.tractusx.sde.notification.config.EmailConfiguration;
import org.eclipse.tractusx.sde.notification.manager.EmailManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONObject;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
class AutoUploadAgentConfigControllerTest {

	@Autowired
	private MockMvc mvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	AutoUploadAgentConfigRepository ftpsConfigRepository;

	@MockBean
	EmailManager emailManager;

	@MockBean
	EmailConfiguration emailConfiguration;

	@BeforeEach
	public void init() {
		ftpsConfigRepository.deleteAll();
	}

	@Test
	void testSaveSFTPConfig() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put("/sftp").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(getJsonBody()))).andExpect(status().isOk());

		mvc.perform(MockMvcRequestBuilders.get("/sftp").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		Assertions.assertEquals(ConfigType.SFTP.toString(), ftpsConfigRepository.findAllByType(ConfigType.SFTP.toString()).get().getType());
	}

	@Test
	void testSaveSchedulerHourlyTypeConfig() throws Exception {

		mvc.perform(MockMvcRequestBuilders.put("/scheduler").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(getJsonSchedulerHourBody()))).andExpect(status().isOk());

		mvc.perform(MockMvcRequestBuilders.get("/scheduler")).andExpect(status().isOk());

		Assertions.assertEquals(ConfigType.SCHEDULER.toString(), ftpsConfigRepository.findAllByType(ConfigType.SCHEDULER.toString()).get().getType());
	}

	@Test
	void testSaveSchedulerDailyTypeConfig() throws Exception {

		mvc.perform(MockMvcRequestBuilders.put("/scheduler").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(getJsonSchedulerDailyBody()))).andExpect(status().isOk());

		mvc.perform(MockMvcRequestBuilders.get("/scheduler")).andExpect(status().isOk());

		Assertions.assertEquals(ConfigType.SCHEDULER.toString(), ftpsConfigRepository.findAllByType(ConfigType.SCHEDULER.toString()).get().getType());
	}

	@Test
	void testSaveSchedulerWeeklyTypeConfig() throws Exception {

		mvc.perform(MockMvcRequestBuilders.put("/scheduler").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(getJsonSchedulerWeeklyBody()))).andExpect(status().isOk());

		mvc.perform(MockMvcRequestBuilders.get("/scheduler").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		Assertions.assertEquals(ConfigType.SCHEDULER.toString(), ftpsConfigRepository.findAllByType(ConfigType.SCHEDULER.toString()).get().getType());
	}

	private JSONObject getJsonBody() {
		JSONObject json = new JSONObject();
		json.put("url", "value1");
		json.put("username", "value2");
		json.put("password", "value4");
		json.put("toBeProcessedLocation", "value5");
		json.put("inProgressLocation", "value6");
		json.put("successLocation", "value7");
		json.put("partialSuccessLocation", "value8");
		json.put("failedLocation", "value9");
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
