 # Serial Part Typization (SDE Maven module)
---
## Description

This module use for SerialPartTypization submodel specification and descriptors. It's contain the codes related to SerialPartTypization to validate, parse and transfer data for DigitalTwins and EDC to create aspect twins and data offer.

---
#### Version: 1.1.0
#### SerialPartTypization Aspect Model URN: urn:bamm:io.catenax.serial_part_typization:1.1.0#SerialPartTypization
#### Semantic Id: urn:bamm:io.catenax.serial_part_typization:1.1.0
---

### Schema

Please find below links for schema details:

- https://portal.int.demo.catena-x.net/semantichub/urn%3Abamm%3Aio.catenax.serial_part_typization%3A1.1.0%23SerialPartTypization
- blob:https://portal.int.demo.catena-x.net/421bf0de-72a6-4261-93cb-dcbb16a110a2
- https://github.com/catenax-ng/product-dft-backend/blob/modules-branch/modules/sde-submodules/serial-part-typization/src/main/resources/serial-part-typization.json


### SerialPartTypization CSV file headers

| Headers Name       	       		| Mandatory                     	| Position 	|
|-------------------------------		|-----------------------------	|--------	|
| uuid		                   		| No		             		    |    1     	|
| part_instance_id			   		| Yes					      	|    2    	|
| manufacturing_date    				| Yes 							| 	 3	   	|
| manufacturing_country  	    		| No                           	| 	 4	  	|
| manufacturer_part_id 		      	| Yes                           	| 	 5	  	|
| customer_part_id		    		 	| No                     		| 	 6	 	|
| classification		 				| Yes                           	|    7 	 	|
| name_at_manufacturer	 			| Yes                           	|    8 	 	|
| name_at_customer	 				| No                           	|    9 	 	|
| optional_identifier_key	 		| No                           	|    10 	 	|
| optional_identifier_value			| No                           	|    11 	 	|


#### [CSV Sample File Link]

#### Example for submodel SerialPartTypization

<br/><br/><img src="src/main/resources/images/serialparttypization.png" height="60%" width="80%"/><br/><br/>

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
 
[CSV Sample File Link]: src/main/resources/serialPartTypization.csv
