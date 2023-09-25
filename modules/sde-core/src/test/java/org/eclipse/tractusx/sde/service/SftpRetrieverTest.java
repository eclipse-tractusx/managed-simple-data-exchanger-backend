/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.tractusx.sde.EnableTestContainers;
import org.eclipse.tractusx.sde.TestContainerInitializer;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.notification.config.EmailConfiguration;
import org.eclipse.tractusx.sde.notification.manager.EmailManager;
import org.eclipse.tractusx.sde.sftp.RetrieverI;
import org.eclipse.tractusx.sde.sftp.service.SftpRetrieverFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@EnableTestContainers
@Execution(ExecutionMode.SAME_THREAD)
@ActiveProfiles("test")
@WithMockUser(username = "Admin", authorities = { "Admin" })
class SftpRetrieverTest {

    @Autowired
    CsvHandlerService csvHandlerService;

    @MockBean
    EmailManager emailManager;

    @MockBean
    EmailConfiguration emailConfiguration;

    @Autowired
    SftpRetrieverFactoryImpl sftpRetrieverFactory;

    @BeforeEach
    public void before() {
        TestContainerInitializer.sftp.stop();
        TestContainerInitializer.sftp.start();
        sftpRetrieverFactory.saveDefaultConfig();
    }

    @FunctionalInterface
    interface ThrowableExec {
        void exec(String param) throws IOException;
    }

    @RequiredArgsConstructor
    @ToString(of="name")
    static class TestMethod implements Function<RetrieverI, ThrowableExec>{
        @Delegate
        private final Function<RetrieverI, ThrowableExec> delegate;
        private final String name;
    }

    static Stream<Function<RetrieverI, ThrowableExec>> provider() {
        return Stream.of(
                new TestMethod(r -> r::setSuccess, "SetSuccess"),
                new TestMethod(r -> r::setFailed,"SetFailed"),
                new TestMethod(r-> r::setPartial, "SetPartial"),
                new TestMethod(r->r::setProgress, "SetProgress")
        );
    }

    @ParameterizedTest(name = "testFtps Test: {index}, {argumentsWithNames}")
    @MethodSource("provider")
    void testFtps(Function<RetrieverI, ThrowableExec> tr) throws Exception {
        try(var sftp = sftpRetrieverFactory.create(OptionalInt.of(TestContainerInitializer.sftp.getMappedPort(22)))) {
            for (String fileId: sftp) {
                final var filePath = Path.of(csvHandlerService.getFilePath(fileId));
                log.info(fileId);
                tr.apply(sftp).exec(fileId);
                Files.copy(filePath, System.out);
                Files.delete(filePath);
            }
        }
    }
}