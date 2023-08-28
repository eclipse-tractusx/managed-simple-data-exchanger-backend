package org.eclipse.tractusx.sde.repository;

import org.eclipse.tractusx.sde.agent.entity.SftpConfigEntity;
import org.eclipse.tractusx.sde.agent.repository.FtpsConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;


@DataJpaTest
public class ConfigRepositoryTest {

    @Autowired
    private FtpsConfigRepository repository;

    @Test
    void testRepo() {
      //  List<SftpConfigEntity> all = repository.findAll();
    }


}
