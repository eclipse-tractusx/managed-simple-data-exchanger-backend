package org.eclipse.tractusx.sde.agent.repository;

import org.eclipse.tractusx.sde.agent.entity.SftpMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SftpMetadataRepository extends JpaRepository<SftpMetadataEntity, String> {
}
