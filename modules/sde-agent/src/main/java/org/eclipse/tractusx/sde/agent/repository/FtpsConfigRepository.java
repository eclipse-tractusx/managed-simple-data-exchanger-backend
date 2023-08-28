package org.eclipse.tractusx.sde.agent.repository;

import org.eclipse.tractusx.sde.agent.entity.SftpConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FtpsConfigRepository extends JpaRepository<SftpConfigEntity, String> {

    List<SftpConfigEntity> findAllByType(String type);
}
