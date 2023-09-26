package org.eclipse.tractusx.sde.controllers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.eclipse.tractusx.sde.EnableTestContainers;
import org.eclipse.tractusx.sde.common.entities.SubmodelPolicyRequest;
import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.DurationEnum;
import org.eclipse.tractusx.sde.common.enums.PolicyAccessEnum;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.core.policy.entity.PolicyEntity;
import org.eclipse.tractusx.sde.core.policy.repository.PolicyRepository;
import org.eclipse.tractusx.sde.core.policy.service.PolicyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@EnableTestContainers
@ActiveProfiles("test")
@WithMockUser(username = "Admin", authorities = { "Admin" })
class PolicyControllerTest {


    @Autowired
    private MockMvc mvc;

    @Autowired
    private PolicyRepository policyRepository;
    
    @Autowired
    private PolicyService policyService;

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
                .andExpect(status().isBadRequest());

        Assertions.assertEquals(1, policyRepository.findAll().size());

    }
    
    @Test
    void findMatchingPolicyBasedOnFileName() throws Exception {
    	
    	String fileName = "Mysubmodel_new_policy.csv";
    	mvc.perform(MockMvcRequestBuilders
                .post("/policy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getPolicy("new_policy"))))
        .andExpect(status().isOk());
    	
    	List<SubmodelPolicyRequest> findMatchingPolicyBasedOnFileName = policyService.findMatchingPolicyBasedOnFileName(fileName);
        Assertions.assertEquals(1, findMatchingPolicyBasedOnFileName.size());
        Assertions.assertEquals("new_policy", findMatchingPolicyBasedOnFileName.get(0).getPolicyName());

    }



    private SubmodelPolicyRequest getPolicy(String policyName) {
        SubmodelPolicyRequest request = new SubmodelPolicyRequest();
        UsagePolicies policies = new UsagePolicies();
        policies.setType(UsagePolicyEnum.DURATION);
        policies.setValue("10");
        policies.setDurationUnit(DurationEnum.DAY);
        policies.setTypeOfAccess(PolicyAccessEnum.RESTRICTED);
        request.setPolicyName(policyName);
        request.setUsagePolicies(List.of(policies));
        request.setBpnNumbers(List.of("BPNL00000005PROV", "BPNL00000005PROW", "BPNL00000005PROB"));
        request.setTypeOfAccess("restricted");
        return request;
    }
}