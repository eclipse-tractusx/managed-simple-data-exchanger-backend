 # PartTypeInformation (SDE Maven module)
---
## Description

This module use for PartTypeInformation submodel specification and descriptors. It's contain the codes related to PartTypeInformation to validate, parse and transfer data for DigitalTwins and EDC to create aspect twins and data offer.

---
#### Version: 1.0.0
#### PartTypeInformation Aspect Model URN: urn:bamm:io.catenax.part_as_planned:1.0.0#PartTypeInformation
#### Semantic Id: urn:samm:io.catenax.part_type_information:1.0.0#PartTypeInformation
---

### Schema

Please find below links for schema details:

- [PartTypeInformation V1.0.0 schema](src/main/resources/part-type-information-v1.0.0.json)

### CSV file headers

| Headers Name              		| Mandatory                 | Position 	|
|------------------------	    |---------------------------|--------	|
| uuid                      		| Yes			            |    1     	|
| manufacturer_part_id      		| Yes					    |    2    	|
| classification_standard		| Yes 						| 	 3	   	|
| classification_id   	      	| Yes                       | 	 4	  	|
| classification_description	    | No                        | 	 5	  	|
| function						| Yes						|	 6 		|
| function_valid_from	    		| No                     	| 	 7	 	|
| function_valid_until	    		| No                     	| 	 8	 	|
| name_at_manufacturer	 	    | Yes                       |    9 	 	|

#### [CSV Sample File Link]

#### Example for submodel PartAsPlanned

### Work Flow 

 - CSV to POJO
 - CSV column validation and mandatory field validation
 - POJO TO DTO
 - UUID generate v4
 - DigitalTwins API's calls 
 - EDC API's calls
 - BPN Discovery calls
 - DB Store
 
### External Services Call

 - DigitalTwins
 - EDC Connectors
 - BPN Discovery
