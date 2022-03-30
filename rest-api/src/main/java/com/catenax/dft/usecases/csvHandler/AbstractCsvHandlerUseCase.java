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
 *
 */

package com.catenax.dft.usecases.csvHandler;

import com.catenax.dft.usecases.processReport.ProcessReportUseCase;
import com.catenax.dft.usecases.logs.FailureLogsUseCase;
import com.catenax.dft.entities.database.FailureLogEntity;
import com.catenax.dft.enums.CsvTypeEnum;
import com.catenax.dft.usecases.csvHandler.aspects.MapToAspectException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
public abstract class AbstractCsvHandlerUseCase<I, T> implements CsvHandlerUseCase<I> {

    protected CsvHandlerUseCase<T> nextUseCase;

    public AbstractCsvHandlerUseCase(CsvHandlerUseCase<T> nextUseCase) {
        this.nextUseCase = nextUseCase;
    }

    protected abstract T executeUseCase(I input, String processId);

    @Autowired
    protected ProcessReportUseCase historicFilesUseCase;

    @Autowired
    FailureLogsUseCase failureLogsUseCase;

    @Override
    public void run(I input, String processId) {

        try {

            T result = executeUseCase(input, processId);

            if (nextUseCase != null) {
                log.info(String.format("[%s] is running now", this.getClass().getCanonicalName()));
                nextUseCase.run(result, processId);
            }
        } catch (RuntimeException e) {

            FailureLogEntity entity = FailureLogEntity.builder()
                    .uuid("urn:uuid:"+UUID.randomUUID())
                    .processId(processId)
                    .log(e.getMessage())
                    .dateTime(LocalDateTime.now())
                    .type(e instanceof MapToAspectException ? CsvTypeEnum.ASPECT :CsvTypeEnum.CHILD_ASPECT)
                    .build();
            failureLogsUseCase.saveLog(entity);
            System.out.println(e);
            log.debug(String.valueOf(e));
        }
    }
}
