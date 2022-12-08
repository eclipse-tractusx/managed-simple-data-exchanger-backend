 # PartSiteInformationAsPlanned (SDE Maven module)
---
## Description

This module use for PartSiteInformationAsPlanned submodel specification and descriptors. It's contain the codes related to PartSiteInformationAsPlanned to validate, parse and transfer data for DigitalTwins and EDC to create aspect twins and data offer.

---
#### Version: 1.0.0
#### PartSiteInformationAsPlanned Aspect Model URN: urn:bamm:io.catenax.part_site_information_as_planned:1.0.0#PartSiteInformationAsPlanned
#### Semantic Id: urn:bamm:io.catenax.part_site_information_as_planned:1.0.0
---

### Schema

Please find below links for schema details:

- https://portal.int.demo.catena-x.net/semantichub/urn%3Abamm%3Aio.catenax.part_site_information_as_planned%3A1.0.0%23PartSiteInformationAsPlanned
- blob:https://portal.int.demo.catena-x.net/5e30d83f-24bb-4a2a-a26a-48bf20b41860
- https://github.com/catenax-ng/product-dft-backend/blob/modules-branch/modules/sde-submodules/part-site-information-as-planned/src/main/resources/part-site-information-as-planned.json


### CSV file headers

| Headers Name       	       		| Mandatory                     	| Position 	|
|-------------------------------		|-----------------------------	|--------	|
| uuid 			     				| Yes 							| 	 1	   	|
| manufacturer_part_id  	       		| Yes                           	| 	 2	  	|
| catenax_site_id 		      		| Yes                           	| 	 3	  	|
| name_at_manufacturer    		 	| Yes                     		| 	 4	 	|
| function						 	| Yes                           	|    5 	 	|
| function_valid_from	 			| No                           	|    6 	 	|
| function_valid_until	 			| No                           	|    7 	 	|

#### [CSV Sample File Link]

#### Example for submodel PartSiteInformationAsPlanned

<br/><br/><img src="src/main/resources/images/partsiteinformationasplanned.png" height="60%" width="80%"/><br/><br/>

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
 
[CSV Sample File Link]: src/main/resources/partSiteInformationAsPlanned.csv
