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

    public String savePolicy(SubmodelFileRequest request) throws JsonProcessingException {
        PolicyEntity policy = new PolicyEntity();
        policy.setName(request.getPolicyName());
        policy.setUuid(UUID.randomUUID().toString());
        policy.setContent(objectMapper.writeValueAsString(request));
        repository.save(policy);
        return policy.getUuid();
    }

    public SubmodelFileRequest getPolicy(String uuid) {
        Optional<PolicyEntity> optionalPolicy = repository.findById(uuid);
        if (optionalPolicy.isEmpty()) return null;
        else {
            PolicyEntity policy = optionalPolicy.get();
            return objectMapper.convertValue(policy.getContent(), SubmodelFileRequest.class);
        }
    }

    public List<SubmodelFileRequest> getAllPolicies() {
        List<PolicyEntity> policyEntities = repository.findAll();
        return policyEntities.stream()
                .map(policyEntity ->
                        objectMapper.convertValue(policyEntity.getContent(), SubmodelFileRequest.class)).toList();
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
