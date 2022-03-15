package com.catenax.dft.gateways.database;

import com.catenax.dft.entities.database.AspectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AspectRepository extends JpaRepository<AspectEntity, String> {
}
