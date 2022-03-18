/*
 *
 *  * Copyright 2022 CatenaX
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package com.catenax.dft.usecases.csvHandler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
@Component
@Getter
@Setter
public class CsvErrorHandler {

    private String processId;
    private int successfulRows = 0;
    private int failedRows = 0;

    public void addSuccess() {
        successfulRows++;
    }

    public void addFailure() {
        failedRows++;
    }

    public void reset() {
        successfulRows = 0;
        failedRows = 0;
        processId = null;
    }
}
