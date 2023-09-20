package org.eclipse.tractusx.sde.controllers;

import org.eclipse.tractusx.sde.EnableTestContainers;
import org.eclipse.tractusx.sde.common.entities.SubmodelFileRequest;
import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.DurationEnum;
import org.eclipse.tractusx.sde.common.enums.PolicyAccessEnum;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.core.policy.entity.PolicyEntity;
import org.eclipse.tractusx.sde.core.policy.repository.PolicyRepository;
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

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@EnableTestContainers
public class PolicyControllerTest {


    @Autowired
    private MockMvc mvc;

    @Autowired
    private PolicyRepository policyRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        policyRepository.deleteAll();
    }

    @Test
    void testSaveAndRetrievePolicy() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getPolicy("new_policy"))))
                .andExpect(status().isOk());

        Assertions.assertEquals(1, policyRepository.findAll().size());
        List<PolicyEntity> entities = policyRepository.findAll();
        String uuid = entities.get(0).getUuid();

        mvc.perform(MockMvcRequestBuilders
                        .get("/policy/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testUniquePolicies() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getPolicy("new_policy"))))
                .andExpect(status().isOk());

        Assertions.assertEquals(1, policyRepository.findAll().size());

        mvc.perform(MockMvcRequestBuilders
                        .post("/policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getPolicy("new_policy"))))
                .andExpect(status().is5xxServerError());

        Assertions.assertEquals(1, policyRepository.findAll().size());

    }



    private SubmodelFileRequest getPolicy(String policyName) {
        SubmodelFileRequest request = new SubmodelFileRequest();
        UsagePolicies policies = new UsagePolicies();
        policies.setType(UsagePolicyEnum.DURATION);
        policies.setValue("");
        policies.setDurationUnit(DurationEnum.DAY);
        policies.setTypeOfAccess(PolicyAccessEnum.RESTRICTED);
        request.setPolicyName(policyName);
        request.setUsagePolicies(List.of(policies));
        request.setBpnNumbers(List.of("BPNL00000005PROV", "BPNL00000005PROW", "BPNL00000005PROB"));
        request.setTypeOfAccess("restricted");
        return request;
    }
}
