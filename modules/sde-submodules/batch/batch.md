 # Batch (SDE Maven module)
---
## Description

This module use for Batch submodel specification and descriptors. It's contain the codes related to Batch to validate, parse and transfer data for DigitalTwins and EDC to create aspect twins and data offer.

---
#### Version: 1.0.2
#### Batch Aspect Model URN: urn:bamm:io.catenax.batch:1.0.2#Batch
#### Semantic Id: urn:bamm:io.catenax.batch:1.0.2
---

### Schema

Please find below links for schema details:

- [schema](src/main/resources/batch.json)


### CSV file headers

| Headers Name       	       		| Mandatory                     	| Position 	|
|-------------------------------	|-----------------------------	    |--------	|
| uuid		                   		| No		             		    |    1     	|
| batch_id					   		| No						      	|    2    	|
| part_instance_id					| Yes							    |	 3		|
| manufacturing_date    			| Yes 							    | 	 4	   	|
| manufacturing_country  	    	| No                            	| 	 5	  	|
| manufacturer_part_id 		      	| Yes                           	| 	 6	  	|
| customer_part_id		    		| No                     		    | 	 7	 	|
| classification		 			| Yes                               |    8 	 	|
| name_at_manufacturer	 			| Yes                           	|    9 	 	|
| name_at_customer	 				| No                           	    |    10 	|


#### [CSV Sample File Link]

#### Example for submodel Batch
<br/><br/><img src="src/main/resources/images/batch.png" height="60%" width="80%" /><br/><br/>

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
 
[CSV Sample File Link]: src/main/resources/batch.csv
