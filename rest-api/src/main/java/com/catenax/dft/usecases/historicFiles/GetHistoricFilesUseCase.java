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

package com.catenax.dft.usecases.historicFiles;

import com.catenax.dft.entities.database.HistoricFilesEntity;
import com.catenax.dft.entities.usecases.HistoricFile;
import com.catenax.dft.enums.ProgressStatusEnum;
import com.catenax.dft.enums.CsvTypeEnum;
import com.catenax.dft.gateways.database.HistoricFileRepository;
import com.catenax.dft.mapper.HistoricFileMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class GetHistoricFilesUseCase {
    private  final HistoricFileRepository repository;
    private  HistoricFile historicFile;
    private  final HistoricFileMapper mapper;


    public GetHistoricFilesUseCase(HistoricFileRepository repository, HistoricFileMapper mapper) {
            this.repository = repository;
            this.mapper = mapper;
    }

    public void startBuildHistoricFile(String processId, CsvTypeEnum aspect, int size, LocalDateTime now){
        historicFile = new HistoricFile();
        historicFile.setProcessId(processId);
        historicFile.setCsvType(aspect);
        historicFile.setStatus(ProgressStatusEnum.IN_PROGRESS);
        historicFile.setNumberOfItems(size);
        historicFile.setStartDate(now);
        saveHistoric(historicFile);
    }

    public void inProgressBuildHistoricFile(int numberOfSucceeded, int numberOfFailures){
        historicFile.setNumberOfSucceededItems(numberOfSucceeded);
        historicFile.setNumberOfFailedItems(numberOfFailures);
        saveHistoric(historicFile);
    }

    public void finishBuildHistoricFile(LocalDateTime now, int successes, int failures){
        historicFile.setEndDate(now);
        historicFile.setStatus(ProgressStatusEnum.COMPLETED);
        historicFile.setNumberOfSucceededItems(successes);
        historicFile.setNumberOfFailedItems(failures);
        saveHistoric(historicFile);
    }

    public void unknownFileToHistoricFile(String processId,LocalDateTime now){
        historicFile = new HistoricFile();
        historicFile.setProcessId(processId);
        historicFile.setCsvType(CsvTypeEnum.UNKNOWN);
        historicFile.setStartDate(now);
        historicFile.setEndDate(now);
        historicFile.setStatus(ProgressStatusEnum.FAILED);
        saveHistoric(historicFile);
    }

    private void saveHistoric(HistoricFile input) {
        HistoricFilesEntity entity = mapper.mapFrom(input);
        repository.save(entity);
        log.debug("Historic store successfully");
    }

    public Page<HistoricFilesEntity> listAllHistoric(int page, int size){
        return repository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate")));
    }



}
