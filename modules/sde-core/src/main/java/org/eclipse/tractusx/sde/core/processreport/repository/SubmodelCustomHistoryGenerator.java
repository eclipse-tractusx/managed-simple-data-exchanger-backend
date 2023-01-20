package org.eclipse.tractusx.sde.core.processreport.repository;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.SneakyThrows;

@Component
public class SubmodelCustomHistoryGenerator {

	@Autowired
	private EntityManager entityManager;

	ObjectMapper objMapper = new ObjectMapper();

	@SuppressWarnings("unchecked")
	@Modifying
	@Transactional
	@SneakyThrows
	public List<List<String>> findAllSubmodelCsvHistory(String colname, String tableEntityName, String processId) {
		List<List<String>> records = new LinkedList<>();
		try {
			Query query = entityManager
					.createNativeQuery("SELECT " + colname + " FROM " + tableEntityName + " as p Where p.process_id=?");
			query.setParameter(1, processId);
			List<Object[]> resultList = query.getResultList();

			for (Object[] objectArray : resultList) {
				List<String> list = new LinkedList<>();
				for (Object object : objectArray) {
					list.add(object == null ? "" : object.toString());
				}
				records.add(list);
			}
		} catch (Exception e) {
			throw new ServiceException("Unable to process dyanamic table read for history " + e.getMessage());
		}
		return records;
	}
}
