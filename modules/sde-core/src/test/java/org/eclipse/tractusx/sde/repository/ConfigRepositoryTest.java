package org.eclipse.tractusx.sde.repository;


import org.eclipse.tractusx.sde.EnableTestContainers;
import org.eclipse.tractusx.sde.agent.entity.SftpConfigEntity;
import org.eclipse.tractusx.sde.agent.repository.FtpsConfigRepository;
import org.eclipse.tractusx.sde.agent.repository.SftpConfigRepository;
import org.eclipse.tractusx.sde.sftp.service.ConfigType;
import org.eclipse.tractusx.sde.sftp.service.SftpRetrieverFactory;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

@SpringBootTest
@EnableTestContainers
public class ConfigRepositoryTest {

    private String metadata = "{\"bpn_numbers\":[\"BPNL00000005PROV\",\"BPNL00000005CONS\",\"TESTVV009\"],\"type_of_access\":\"restricted\",\"usage_policies\":[{\"type\":\"DURATION\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\",\"durationUnit\":\"SECOND\"},{\"type\":\"ROLE\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\"},{\"type\":\"PURPOSE\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\"},{\"type\":\"CUSTOM\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\"}]}";


    @Autowired
    FtpsConfigRepository ftpsConfigRepository;

    @Test
    void testRepo() {
        SftpConfigEntity entity = new SftpConfigEntity();
        entity.setUuid(UUID.randomUUID().toString());
        entity.setType(ConfigType.METADADA.toString());
        entity.setContent(metadata);
        ftpsConfigRepository.save(entity);
        List<SftpConfigEntity> all = ftpsConfigRepository.findAll();
        Assertions.assertEquals(all.size(), 1);
    }

}
