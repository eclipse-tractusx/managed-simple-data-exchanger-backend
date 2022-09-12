/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.catenax.dft.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catenax.dft.entities.SubmodelJsonRequest;
import com.catenax.dft.entities.batch.BatchRequest;
import com.catenax.dft.entities.batch.BatchResponse;
import com.catenax.dft.exceptions.DftException;
import com.catenax.dft.usecases.batchs.GetBatchsUseCase;
import com.catenax.dft.usecases.csvhandler.batchs.CreateBatchsUseCase;

import javax.validation.Valid;

@RestController
@RequestMapping("batch")
@Validated
public class BatchController {

    private final GetBatchsUseCase batchsUseCase;
    private final CreateBatchsUseCase createBatchsUseCase;

	public BatchController( GetBatchsUseCase batchsUseCase, 
							CreateBatchsUseCase createBatchsUseCase) {
		this.batchsUseCase = batchsUseCase;
		this.createBatchsUseCase = createBatchsUseCase;
	}

    @GetMapping(value="/public/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<BatchResponse> getBatch(@PathVariable("id") String uuid) {
        BatchResponse response = batchsUseCase.execute(uuid);

        if (response == null) {
            return notFound().build();
        }
        return ok().body(response);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createAspectBatch(@RequestBody @Valid SubmodelJsonRequest<BatchRequest> batchAspects){
        String processId = UUID.randomUUID().toString();

        Runnable runnable = () -> {
            try {
                createBatchsUseCase.createBatchs(batchAspects, processId);
            } catch (JsonProcessingException e) {
                throw new DftException(e);
            }
        };
        new Thread(runnable).start();

        return ok().body(processId);
    }
}