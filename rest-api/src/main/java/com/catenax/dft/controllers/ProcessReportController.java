/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.catenax.dft.controllers;

import com.catenax.dft.entities.usecases.ProcessReport;
import com.catenax.dft.entities.usecases.ProcessReportPageResponse;
import com.catenax.dft.mapper.ProcessReportMapper;
import com.catenax.dft.usecases.processReport.ProcessReportUseCase;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
public class ProcessReportController {

    private final ProcessReportUseCase processReportUseCase;
    private final ProcessReportMapper mapper;

    public ProcessReportController(ProcessReportUseCase processReportUseCase, ProcessReportMapper mapper){
        this.processReportUseCase = processReportUseCase;
        this.mapper=mapper;
    }

    @GetMapping(path = "/processing-report")
    public ResponseEntity<ProcessReportPageResponse> getHistoricByDateDesc(@Param("page") Integer page, @Param("pageSize") Integer pageSize){

        page = page == null ? 0 : page;
        pageSize = pageSize == null ? 10 : pageSize;

        return ok().body(processReportUseCase.listAllHistoric(page, pageSize));

    }

    @GetMapping(value = "/processing-report/{id}")
    public ResponseEntity<ProcessReport> findHistoricFilesById(@PathVariable("id") String id){
        ProcessReport historicById = processReportUseCase.getReportsById(id);
        if(historicById == null){
            return notFound().build();
        }
        return ok().body(historicById);
    }

}
