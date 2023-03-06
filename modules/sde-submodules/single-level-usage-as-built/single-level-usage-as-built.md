 # SingleLevelUsageAsBuilt (SDE Maven module)
---
## Description

This module use for SingleLevelUsageAsBuilt submodel specification and descriptors. It's contain the codes related to SingleLevelUsageAsBuilt to validate, parse and transfer data for DigitalTwins and EDC to create aspect twins and data offer.

---
#### Version: 1.0.1
#### AssemblyPartRelationship Aspect Model URN: urn:bamm:io.catenax.single_level_usage_as_built:1.0.1#SingleLevelUsageAsBuilt
#### Semantic Id: urn:bamm:io.catenax.single_level_usage_as_built:1.0.1
---

### Schema

Please find below links for schema details:

- https://portal.int.demo.catena-x.net/semantichub/urn%3Abamm%3Aio.catenax.single_level_usage_as_built%3A1.0.1%23SingleLevelUsageAsBuilt
- blob:https://portal.int.demo.catena-x.net/353a115a-3150-4cbf-b7c7-2dd88e90a527
- https://github.com/catenax-ng/product-dft-backend/blob/modules-branch/modules/sde-submodules/single-level-usage-as-built/src/main/resources/single-level-usage-as-built.json


### SingleLevelUsageAsBuilt CSV file headers

| Headers Name       	       		| Mandatory                     | Position 	|
|-----------------------------------|-------------------------------|--------	|
| parent_uuid		                | Yes		             	    |    1     	|
| parent_part_instance_id	     	| Yes		             	    |    2    	|
| parent_manufacturer_part_id	    | Yes		             	    |    3    	|
| parent_optional_identifier_key	| No			             	|    4    	|
| parent_optional_identifier_value 	| No			             	|    5    	|
| uuid		                   		| Yes		             	    |    6     	|
| part_instance_id			   		| Yes					      	|    7    	|
| manufacturer_part_id 		      	| Yes                           | 	 8	  	|
| optional_identifier_key	 		| No                           	|    9 	 	|
| optional_identifier_value			| No                           	|    10 	|
| quantity_number		 			| Yes                           |    11	 	|
| measurement_unit 					| Yes                           |    12	 	|
| created_on	 					| Yes                          	|    13	 	|
| last_modified_on	 				| No                           	|    14	 	|


#### [CSV Sample File Link]

#### Example for submodel SingleLevelUsageAsBuilt

<br/><br/><img src="src/main/resources/images/singlelevelusageasbuilt.png" height="60%" width="80%"/><br/><br/>

### Work Flow 

 - CSV to POJO
 - CSV column validation and mandatory field validation
 - POJO TO DTO
 - Lookup parent UUID and child UUID
 - DigitalTwins API's calls 
 - EDC API's calls
 - DB Store
 
### External Services Call

 - DigitalTwins
 - EDC Connectors
 
[CSV Sample File Link]: src/main/resources/singleLevelUsageAsBuilt.csv