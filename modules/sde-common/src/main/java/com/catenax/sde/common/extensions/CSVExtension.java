package com.catenax.sde.common.extensions;

import com.catenax.sde.common.entities.SubmodelFileRequest;
import com.catenax.sde.common.entities.csv.RowData;
import com.catenax.sde.common.model.SubmodelPojo;
import com.google.gson.JsonObject;

public interface CSVExtension<T> {

	/*default SubmodelPojo<T> parseCSV(InputCSV data) {
		//JSONObjet.get get scema
		
		//schema iteration->
		//{
			//field-?
			//read field from input data	
			//get value
			//apply val if there idation?
			//prepare new pojo or JSON object
		//}
		return null;x
	}*/

	//SubmodelPojo<T> getCSVData();
	
	T parseCSVDataToSubmodel(RowData rowData, String processId, SubmodelFileRequest submodelFileRequest,JsonObject asJsonObject);
}
