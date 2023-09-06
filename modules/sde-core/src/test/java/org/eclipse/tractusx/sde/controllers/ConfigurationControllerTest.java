package org.eclipse.tractusx.sde.controllers;


import net.minidev.json.JSONObject;
import org.eclipse.tractusx.sde.EnableTestContainers;
import org.eclipse.tractusx.sde.agent.repository.FtpsConfigRepository;
import org.eclipse.tractusx.sde.sftp.service.ConfigType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@EnableTestContainers
public class ConfigurationControllerTest {


    @Autowired
    private MockMvc mvc;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    FtpsConfigRepository ftpsConfigRepository;

    @BeforeEach
    public void init() {
        ftpsConfigRepository.deleteAll();
    }

    @Test
    void testSaveClientConfig() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/updateFtpsConfig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getJsonBody()))
                        .param("type", ConfigType.CLIENT.toString()))
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders
                        .post("/updateFtpsConfig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getJsonBody()))
                        .param("type", ConfigType.CLIENT.toString()))
                .andExpect(status().isOk());

        Assertions.assertEquals(1, ftpsConfigRepository.findAllByType(ConfigType.CLIENT.toString()).size());
    }

    @Test
    void testSaveDifferentTypeConfig() throws Exception {

        System.out.println(ftpsConfigRepository.findAll().size());

        mvc.perform(MockMvcRequestBuilders
                        .post("/updateFtpsConfig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getJsonBody()))
                        .param("type", ConfigType.CLIENT.toString()))
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders
                        .post("/updateFtpsConfig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getJsonBody()))
                        .param("type", ConfigType.METADATA.toString()))
                .andExpect(status().isOk());

        Assertions.assertEquals(2, ftpsConfigRepository.findAll().size());
    }


    private  JSONObject getJsonBody() {
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


}
