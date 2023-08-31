package org.eclipse.tractusx.sde;


import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.MountableFile;

public class TestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4")
            .withDatabaseName("dft")
            .withUsername("root")
            .withPassword("P@ssword21");

    static public GenericContainer<?>  sftp = new GenericContainer<>("dvasunin/sftp:latest")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("sftp/", 0777),
                    "/home/foo/upload/sftp")
            .withExposedPorts(22)
            .withCommand("foo:pass:::upload");

    static {
        Startables.deepStart(postgres, sftp).join();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
            "spring.datasource.url=" + postgres.getJdbcUrl(),
            "sftp.location.tobeprocessed=/upload/sftp/tobe",
            "sftp.location.success=/upload/sftp/success",
            "sftp.location.failed=/upload/sftp/failed",
            "sftp.location.partialsucess=/upload/sftp/partial",
            "sftp.location.inprogress=/upload/sftp/inprogress",
            "sftp.username=foo",
            "sftp.password=pass",
            "sftp.host=127.0.0.1",
            "sftp.port=" + sftp.getMappedPort(22)
        ).applyTo(applicationContext);
    }
}
