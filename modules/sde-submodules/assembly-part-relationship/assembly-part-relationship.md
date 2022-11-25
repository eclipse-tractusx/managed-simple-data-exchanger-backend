 # Assembly Part Relationship (SDE Maven module)
---
## Description

This module use for AssemblyPartRelationship submodel specification and descriptors. It's contain the codes related to AssemblyPartRelationship to validate, parse and transfer data for DigitalTwins and EDC to create aspect twins and data offer.

---
#### Version: 1.1.0
#### AssemblyPartRelationship Aspect Model URN: urn:bamm:io.catenax.assembly_part_relationship:1.1.0#AssemblyPartRelationship
#### Semantic Id: urn:bamm:io.catenax.assembly_part_relationship:1.1.0
---

### Schema

Please find below links for schema details:

- https://portal.int.demo.catena-x.net/semantichub/urn%3Abamm%3Aio.catenax.assembly_part_relationship%3A1.1.0%23AssemblyPartRelationship
- blob:https://portal.int.demo.catena-x.net/cf9f1c9f-2d31-4b41-82e3-5285e410f76f
- https://github.com/catenax-ng/product-dft-backend/blob/modules-branch/modules/sde-submodules/serial-part-typization/src/main/resources/serial-part-typization.json


### AssemblyPartRelationship CSV file headers

| Headers Name       	       		| Mandatory                     	| Position 	|
|-------------------------------		|-----------------------------	|--------	|
| parent_uuid		                	| Yes		             	    |    1     	|
| parent_part_instance_id	     	| Yes		             	    |    2    	|
| parent_manufacturer_part_id	    	| Yes		             	    |    3    	|
| parent_optional_identifier_key	    	| No			             	    |    4    	|
| parent_optional_identifier_value 	| No			             	    |    5    	|
| uuid		                   		| Yes		             	    |    6     	|
| part_instance_id			   		| Yes					      	|    7    	|
| manufacturer_part_id 		      	| Yes                           	| 	 8	  	|
| optional_identifier_key	 		| No                           	|    9 	 	|
| optional_identifier_value			| No                           	|    10 	 	|
| lifecycle_context		    		 	| Yes                     		| 	 11	 	|
| quantity_number		 			| Yes                           	|    12	 	|
| measurement_unit_lexical_value	 	| Yes                           	|    13	 	|
| datatype_URI	 					| Yes                          	|    14	 	|
| assembled_on	 					| Yes                           	|    15	 	|


#### [CSV Sample File Link]

#### Example for submodel AssemblyPartRelationship

<br/><br/><img src="src/main/resources/images/assemblypartrelationship.png" height="60%" width="80%"/><br/><br/>

### Work Flow 

 - CSV to POJO
 - CSV column validation and mandatory field validation
 - POJO TO DTO
 - UUID generate v4
 - DigitalTwins API's calls 
 - EDC API's calls
 - DB Store
 
### External Services Call

 - DigitalTwins
 - EDC Connectors
 
[CSV Sample File Link]: src/main/resources/assemblyPartRelationship.csv
