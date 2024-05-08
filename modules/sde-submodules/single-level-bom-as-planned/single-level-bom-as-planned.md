 # SingleLevelBomAsPlanned (SDE Maven module)
---
## Description

This module use for SingleLevelBomAsPlanned submodel specification and descriptors. It's contain the codes related to SingleLevelBomAsPlanned to validate, parse and transfer data for DigitalTwins and EDC to create aspect twins and data offer.

---
#### Version: 1.0.1
#### SingleLevelBomAsPlanned Aspect Model URN: urn:bamm:io.catenax.single_level_bom_as_planned:1.0.1#SingleLevelBomAsPlanned
#### Semantic Id: urn:bamm:io.catenax.single_level_bom_as_planned:1.0.1

#### Version: 3.0.3
#### SingleLevelBomAsPlanned Aspect Model URN: urn:bamm:io.catenax.single_level_bom_as_planned:1.0.1#SingleLevelBomAsPlanned
#### Semantic Id: urn:bamm:io.catenax.single_level_bom_as_planned:3.0.0
---

### Schema

Please find below links for schema details:

- [single-level-bom-as-planned-v1.0.1-schema](src/main/resources/single-level-bom-as-planned-v1.0.1.json)
- [single-level-bom-as-planned-v3.0.0-schema](src/main/resources/single-level-bom-as-planned-v3.0.0.json)


### CSV file headers

| Headers Name       	       		| Mandatory                     | Position 	|
|-------------------------------	|-----------------------------	|--------	|
| parent_uuid                   	| Yes		             	    |    1     	|
| parent_manufacturer_part_id   	| Yes					      	|    2    	|
| uuid 			     				| Yes 							| 	 3	   	|
| manufacturer_part_id  	       	| Yes                           | 	 4	  	|
| customer_part_id 		      		| No                           	| 	 5	  	|
| quantity_number    		 		| Yes                     		| 	 6	 	|
| measurement_unit_lexical_value	| Yes                           |    7 	 	|
| datatype_uri	 					| Yes                           |    8 	 	|
| created_on	 					| Yes                           |    9 	 	|
| last_modified_on	 				| No                           	|    10 	|

#### [CSV Sample File Link]

#### Example for submodel SingleLevelBomAsPlanned

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
 - BPD Discovery
