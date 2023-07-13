 # Single Level BoM As Built (SDE Maven module)
---
## Description

This module use for SingleLevelBoMAsBuilt submodel specification and descriptors. It's contain the codes related to SingleLevelBoMAsBuilt to validate, parse and transfer data for DigitalTwins and EDC to create aspect twins and data offer.

---
#### Version: 1.0.0
#### SingleLevelBoMAsBuilt Aspect Model URN: urn:bamm:io.catenax.single_level_bom_as_built:1.0.0#SingleLevelBomAsBuilt
#### Semantic Id: urn:bamm:io.catenax.single_level_bom_as_built:1.0.0
---

### Schema

Please find below links for schema details:

- [schema](src/main/resources/assembly-part-relationship.json)


### AssemblyPartRelationship CSV file headers

| Headers Name       	       		| Mandatory                     | Position 	|
|-------------------------------	|-----------------------------	|--------	|
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
| quantity_number		 			| Yes                           |    12	 	|
| measurement_unit				 	| Yes                           |    13	 	|
| created_on	 					| Yes                           |    14	 	|
| last_modified_on	 				| Yes                           |    15	 	|


#### [CSV Sample File Link]

#### Example for submodel SingleLevelBoMAsBuilt

<br/><br/><img src="src/main/resources/images/assemblypartrelationship.png" height="60%" width="80%"/><br/><br/>

### Work Flow 

 - CSV to POJO
 - CSV column validation and mandatory field validation
 - POJO TO DTO
 - UUID Lookup call for already generate v4
 - DigitalTwins API's calls 
 - EDC API's calls
 - DB Store
 
### External Services Call

 - DigitalTwins
 - EDC Connectors
 
[CSV Sample File Link]: src/main/resources/assemblyPartRelationship.csv
