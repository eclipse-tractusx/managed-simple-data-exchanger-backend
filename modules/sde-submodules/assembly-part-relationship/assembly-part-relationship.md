 # Single Level BoM As Built (SDE Maven module)
---
## Description

This module use for SingleLevelBoMAsBuilt submodel specification and descriptors. It's contain the codes related to SingleLevelBoMAsBuilt to validate, parse and transfer data for DigitalTwins and EDC to create aspect twins and data offer. We are building relationship between parent and child twins using lookup.    

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
|----------------------------------	|-----------------------------	|--------	|
| parent_part_instance_id	     	| Yes		             	    |    1    	|
| parent_manufacturer_part_id	    | Yes		             	    |    2    	|
| parent_optional_identifier_key		| No				             	|    3    	|
| parent_optional_identifier_value 	| No				             	|    4    	|
| part_instance_id			   		| Yes					      	|    5    	|
| manufacturer_part_id 		      	| Yes                           | 	 6	  	|
| optional_identifier_key	 		| No                           	|    7 	 	|
| optional_identifier_value			| No                           	|    8	 	|
| quantity_number		 			| Yes                           |    9	 	|
| measurement_unit				 	| Yes                           |    10	 	|
| created_on	 						| Yes                           |    11	 	|
| last_modified_on	 				| Yes                           |    12	 	|


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
