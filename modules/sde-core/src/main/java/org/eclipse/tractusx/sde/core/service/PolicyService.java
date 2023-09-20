package org.eclipse.tractusx.sde.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.sde.common.entities.SubmodelFileRequest;
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

    public String savePolicy(SubmodelFileRequest request) throws Exception {
        Optional<PolicyEntity> optionalPolicy = repository.findByName(request.getPolicyName());
        if (optionalPolicy.isEmpty()) {
            PolicyEntity policy = new PolicyEntity();
            policy.setName(request.getPolicyName());
            policy.setUuid(UUID.randomUUID().toString());
            policy.setContent(objectMapper.writeValueAsString(request));
            repository.save(policy);
            return policy.getUuid();
        } else throw new Exception("Such policy name already exists");
    }

    public SubmodelFileRequest getPolicy(String uuid) throws JsonProcessingException {
        Optional<PolicyEntity> optionalPolicy = repository.findById(uuid);
        if (optionalPolicy.isEmpty()) return null;
        else {
            PolicyEntity policy = optionalPolicy.get();
            return objectMapper.readValue(policy.getContent(), SubmodelFileRequest.class);
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
