 # PartAsPlanned (SDE Maven module)
---
## Description

This module use for PartAsPlanned submodel specification and descriptors. It's contain the codes related to PartAsPlanned to validate, parse and transfer data for DigitalTwins and EDC to create aspect twins and data offer.

---
#### Version: 1.0.0
#### PartAsPlanned Aspect Model URN: urn:bamm:io.catenax.part_as_planned:1.0.0#PartAsPlanned
#### Semantic Id: urn:bamm:io.catenax.part_as_planned:1.0.0
---

### Schema

Please find below links for schema details:

- https://portal.int.demo.catena-x.net/semantichub/urn%3Abamm%3Aio.catenax.part_as_planned%3A1.0.0%23PartAsPlanned
- blob:https://portal.dev.demo.catena-x.net/48fe44cd-273d-4062-9999-d2c323b2be7f
- https://github.com/catenax-ng/product-dft-backend/blob/modules-branch/modules/sde-submodules/part-as-planned/src/main/resources/part-as-planned.json

### CSV file headers

| Headers Name              	| Mandatory                     	| Position 	|
|------------------------	|-----------------------------	|--------	|
| uuid                      	| Yes			            	    |    1     	|
| manufacturer_part_id      	| Yes					      	|    2    	|
| valid_from      			| No 							| 	 3	   	|
| valid_to   		       	| No                            	| 	 4	  	|
| customer_part_id       	| No                           	| 	 5	  	|
| classification    		 	| Yes                     		| 	 6	 	|
| name_at_manufacturer	 	| Yes                           	|    7 	 	|

#### [CSV Sample File Link]

#### Example for submodel PartAsPlanned

<br/><br/><img src="src/main/resources/images/partasplanned.png" height="60%" width="80%"/><br/><br/>

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
 
 
[CSV Sample File Link]: src/main/resources/partAsPlanned.csv
