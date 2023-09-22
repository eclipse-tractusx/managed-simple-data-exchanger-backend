/********************************************************************************
 * Copyright (c) 2023 BMW GmbH
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.sde.common.entities.SubmodelFileRequest;
import org.eclipse.tractusx.sde.core.processreport.model.ProcessReportPageResponse;
import org.eclipse.tractusx.sde.core.service.PolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.unprocessableEntity;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping("/policy")
    public ResponseEntity<String> savePolicy(@NotBlank @RequestBody SubmodelFileRequest request) {
        String res = policyService.savePolicy(request);
        if(res != null) {return ok().body(res);}
        else {return unprocessableEntity().body("Policy with same name is already present");}
    }

    @GetMapping("/policy/{uuid}")
    public ResponseEntity<SubmodelFileRequest> getPolicy(@PathVariable String uuid) {
        SubmodelFileRequest res = policyService.getPolicy(uuid);
        if(res != null) {return ok().body(res);}
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/policy")
    public List<SubmodelFileRequest> getAllPolicies() {
        return policyService.getAllPolicies();
    }

    @DeleteMapping("/policy/{uuid}")
    public ResponseEntity<Object> deletePolicy(@PathVariable String uuid) {
        policyService.deletePolicy(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/policy/{uuid}")
    public ResponseEntity<String> updatePolicy(@PathVariable String uuid,
                              @NotBlank @RequestBody SubmodelFileRequest request) throws JsonProcessingException {
        String res = policyService.updatePolicy(uuid, request);
        if(res != null) {return ok().body(res);}
        else {
            return ResponseEntity.notFound().build();
        }
    }
}
