 # Batch (SDE Maven module)
---
## Description

This module use for Batch submodel specification and descriptors. It's contain the codes related to Batch to validate, parse and transfer data for DigitalTwins and EDC to create aspect twins and data offer.

---
#### Version: 2.0.0
#### Batch Aspect Model URN: urn:bamm:io.catenax.batch:2.0.0#Batch
#### Semantic Id: urn:samm:io.catenax.batch:2.0.0

#### Version: 3.0.0
#### Batch Aspect Model URN: urn:bamm:io.catenax.batch:3.0.0#Batch
#### Semantic Id: urn:samm:io.catenax.batch:3.0.0
---

### Schema

Please find below links for schema details:

- [batch-v2.0.0 schema](src/main/resources/batch-v2.0.0.json)
- [batch-v3.0.0 schema](src/main/resources/batch-v2.0.0.json)


### CSV file headers

| Headers Name       	       		| Mandatory                     	| Position 	|
|-------------------------------	----|-----------------------------	|--------	|
| uuid		                   		| No		             		    |    1     	|
| batch_id					   		| No						      	|    2    	|
| part_instance_id					| Yes							|	 3		|
| manufacturing_date    				| Yes 							| 	 4	   	|
| manufacturing_country  	    		| No                            	| 	 5	  	|
| manufacturer_part_id 		      	| Yes                           	| 	 6	  	|
| classification		 				| Yes                           |    7	 	|
| name_at_manufacturer	 			| Yes                           	|    8 	 	|


#### [CSV Sample File Link]

#### Example for submodel Batch

### Work Flow 

 - CSV to POJO
 - CSV column validation and mandatory field validation
 - POJO TO DTO
 - UUID generate v4
 - DigitalTwins API's calls 
 - EDC API's calls
 - BPNDiscovery API Call
 - DB Store
 
### External Services Call

 - DigitalTwins
 - EDC Connectors
 - BPN Discovery

