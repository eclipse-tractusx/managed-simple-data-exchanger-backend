package org.eclipse.tractusx.sde.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.sde.common.entities.SubmodelFileRequest;
import org.eclipse.tractusx.sde.core.service.PolicyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping("/policy")
    public String savePolicy(@NotBlank @RequestBody SubmodelFileRequest request) throws Exception {
        return policyService.savePolicy(request);
    }

    @GetMapping("/policy/{uuid}")
    public SubmodelFileRequest getPolicy(@PathVariable String uuid) throws JsonProcessingException {
        return policyService.getPolicy(uuid);
    }

    @GetMapping("/policy")
    public List<SubmodelFileRequest> getAllPolicies() throws JsonProcessingException {
        return policyService.getAllPolicies();
    }

    @DeleteMapping("/policy/{uuid}")
    public void deletePolicy(@PathVariable String uuid) {
        policyService.deletePolicy(uuid);
    }

    @PutMapping("/policy/{uuid}")
    public String updatePolicy(@PathVariable String uuid,
                              @NotBlank @RequestBody SubmodelFileRequest request) throws JsonProcessingException {
        return policyService.updatePolicy(uuid, request);
    }






}
