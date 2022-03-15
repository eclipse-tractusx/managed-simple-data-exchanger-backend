package com.catenax.dft.gateways.database;

import com.catenax.dft.entities.database.ChildAspectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChildAspectRepository extends JpaRepository<ChildAspectEntity, Long> {
}
