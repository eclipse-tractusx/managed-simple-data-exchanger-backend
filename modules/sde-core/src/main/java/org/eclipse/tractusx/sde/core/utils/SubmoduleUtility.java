package org.eclipse.tractusx.sde.core.utils;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.tractusx.sde.common.constants.SubmoduleCommonColumnsConstant;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

@Component
public class SubmoduleUtility {

	public String getTableName(Submodel schemaObj) throws ServiceException {
		String tableName = schemaObj.getProperties().get("tableName").toString();
		if (tableName == null)
			throw new ServiceException("The submodel table name not found for processing");
		return tableName;
	}

	public List<String> getTableColomnHeaderForCSV(Submodel schemaObj) {
		Object autoPopulatefieldObj = schemaObj.getProperties().get("autoPopulatedfields");
		List<String> headerName = getCSVHeader(schemaObj, autoPopulatefieldObj);
		headerName.add(SubmoduleCommonColumnsConstant.SHELL_ID);
		headerName.add(SubmoduleCommonColumnsConstant.SUBMODULE_ID);
		headerName.add(SubmoduleCommonColumnsConstant.ASSET_ID);
		headerName.add(SubmoduleCommonColumnsConstant.ACCESS_POLICY_ID);
		headerName.add(SubmoduleCommonColumnsConstant.USAGE_POLICY_ID);
		headerName.add(SubmoduleCommonColumnsConstant.CONTRACT_DEFINATION_ID);
		return headerName;
	}

	public List<String> getTableColomnHeader(Submodel schemaObj) {
		List<String> tableColomnHeader = getTableColomnHeaderForCSV(schemaObj);
		tableColomnHeader.add(SubmoduleCommonColumnsConstant.PROCESS_ID);
		tableColomnHeader.add(SubmoduleCommonColumnsConstant.DELETED);
		tableColomnHeader.add(SubmoduleCommonColumnsConstant.UPDATED);
		return tableColomnHeader;
	}

	public List<String> getCSVHeader(Submodel schemaObj, Object autoPopulatefieldObj) {
		JsonObject schemaObject = schemaObj.getSchema();
		return createCSVColumnHeader(schemaObject, autoPopulatefieldObj);
	}

	private List<String> createCSVColumnHeader(JsonObject schemaObject, Object autoPopulatefieldObj) {

		JsonObject asJsonObject = schemaObject.get("items").getAsJsonObject().get("properties").getAsJsonObject();
		List<String> headerList = asJsonObject.keySet().stream().toList();

		List<String> headerName = new LinkedList<>();
		headerName.addAll(headerList);

		if (autoPopulatefieldObj != null) {
			@SuppressWarnings("unchecked")
			List<String> autoPopulatefield = (List<String>) autoPopulatefieldObj;
			headerName.addAll(autoPopulatefield);
		}
		return headerName;
	}
}
