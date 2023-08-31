package org.eclipse.tractusx.sde.service;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.eclipse.tractusx.sde.EnableTestContainers;
import org.eclipse.tractusx.sde.TestContainerInitializer;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.sftp.RetrieverI;
import org.eclipse.tractusx.sde.sftp.service.SftpRetrieverFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Stream;

@SpringBootTest
@EnableTestContainers
@Execution(ExecutionMode.SAME_THREAD)
public class SftpRetrieverTest {

    @Autowired
    CsvHandlerService csvHandlerService;

    @Autowired
    SftpRetrieverFactoryImpl sftpRetrieverFactory;

    @BeforeEach
    public void before() {
        TestContainerInitializer.sftp.stop();
        TestContainerInitializer.sftp.start();
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
                System.out.println(fileId);
                tr.apply(sftp).exec(fileId);
                Files.copy(filePath, System.out);
                Files.delete(filePath);
            }
        }
    }
}