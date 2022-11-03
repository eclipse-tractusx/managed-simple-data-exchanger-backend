package com.catenax.sde.common.extensions;

import com.catenax.sde.common.model.SubmodelPojo;

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
		return null;
	}*/

	SubmodelPojo<T> getCSVData();
}
