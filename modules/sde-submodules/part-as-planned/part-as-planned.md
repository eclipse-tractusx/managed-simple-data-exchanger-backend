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

- [schema](src/main/resources/part-as-planned.json)

### CSV file headers

| Headers Name              	| Mandatory                 | Position 	|
|------------------------	    |---------------------------|--------	|
| uuid                      	| Yes			            |    1     	|
| manufacturer_part_id      	| Yes					    |    2    	|
| valid_from      			    | No 						| 	 3	   	|
| valid_to   		         	| No                        | 	 4	  	|
| customer_part_id       	    | No                        | 	 5	  	|
| classification    		 	| Yes                     	| 	 6	 	|
| name_at_manufacturer	 	    | Yes                       |    7 	 	|

#### [CSV Sample File Link]

#### Example for submodel PartAsPlanned

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
