package com.catenax.dft.repository;

import com.catenax.dft.dao.AspectDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AspectDaoRepository extends JpaRepository<AspectDao, String> {



}
