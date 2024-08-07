 # Product Carbon Footprint(PCF) (SDE Maven module)
---
## Description

This module use for PCF submodel specification and descriptors. It's contain the codes related to PCF to validate, parse and transfer data for DigitalTwins and EDC to create aspect twins and data offer.

---
#### Version: 6.0.0
#### PCF Aspect Model URN: urn:samm:io.catenax.pcf:6.0.0#Pcf
#### Semantic Id: urn:samm:io.catenax.pcf:6.0.0
---
---
#### Version: 7.0.0
#### PCF Aspect Model URN: urn:samm:io.catenax.pcf:7.0.0#Pcf
#### Semantic Id: urn:samm:io.catenax.pcf:7.0.0
---

### Schema

Please find below links for schema details:

- [schema](src/main/resources/pcf-v6.0.0.json)
- [schema](src/main/resources/pcf-v7.0.0.json)


### PCF CSV file headers

| Headers Name       	       			| Mandatory                     	| Position 	|
|----------------------------------		|-----------------------------	    |--------	|
| id		                   							| No		             		    |    1     	|
| specVersion					   						| Yes						      	|    2    	|
| partialFullPcf										| Yes							    |	 3		|
| precedingPfId    										| Yes 							    | 	 4	   	|
| version  	    										| No                            	| 	 5	  	|
| created 		      									| Yes                           	| 	 6	  	|
| extWBCSD_pfStatus		 								| Yes                               |    7	 	|
| validityPeriodStart	 								| Yes                           	|    8 	 	|
| validityPeriodEnd			   							| No						      	|    9   	|
| comment												| Yes							    |	 10		|
| pcfLegalStatement    									| Yes 							    | 	 11	   	|
| companyName  	    									| No                            	| 	 12	  	|
| companyId 		      								| Yes                           	| 	 13	  	|
| productDescription		 							| Yes                               |    14	 	|
| productId	 											| Yes                           	|    15	 	|
| extWBCSD_productCodeCpc	   							| No						      	|    16   	|
| productName											| Yes							    |	 17		|
| declaredUnit    										| Yes 							    | 	 18	   	|
| unitaryProductAmount  	    						| No                            	| 	 19	  	|
| productMassPerDeclaredUnit       						| Yes                           	| 	 20	  	|
| exemptedEmissionsPercent	 							| Yes                               |    21	 	|
| exemptedEmissionsDescription	 						| Yes                           	|    22	 	|
| extWBCSD_packagingEmissionsIncluded  					| No						      	|    23   	|
| boundaryProcessesDescription							| Yes							    |	 24		|
| geographyCountrySubdivision    						| Yes 							    | 	 25	   	|
| geographyCountry  	    							| No                            	| 	 26	  	|
| geographyRegionOrSubregion      						| Yes                           	| 	 27	  	|
| referencePeriodStart									| Yes                               |    28	 	|
| referencePeriodEnd	 								| Yes                           	|    30	 	|
| crossSectoralStandard		   							| No						      	|    31   	|
| extWBCSD_operator										| Yes							    |	 32		|
| ruleName    											| Yes 							    | 	 33	   	|
| extWBCSD_otherOperatorName  	   						| No                            	| 	 34	  	|
| extWBCSD_characterizationFactors     					| Yes                           	| 	 35	  	|
| extWBCSD_allocationRulesDescription 					| Yes                               |    36	 	|
| extTFS_allocationWasteIncineration					| Yes                           	|    37	 	|
| primaryDataShare				   						| No						      	|    38   	|
| secondaryEmissionFactorSource							| Yes							    |	 39		|
| coveragePercent    									| Yes 							    | 	 40	   	|
| technologicalDQRtemporalDQR  	    					| No                            	| 	 41	  	|
| geographicalDQR 		      							| Yes                           	| 	 42	  	|
| completenessDQR		 								| Yes                               |    43	 	|
| reliabilityDQR	 									| Yes                           	|    44	 	|
| pcfExcludingBiogenic									| No						      	|    45   	|
| pcfIncludingBiogenic									| Yes							    |	 46		|
| fossilGhgEmissions    								| Yes 							    | 	 47	   	|
| biogenicCarbonEmissionsOtherThanCO2 					| No                            	| 	 48	  	|
| biogenicCarbonWithdrawal 		      					| Yes                           	| 	 49	  	|
| dlucGhgEmissions		 								| Yes                               |    50	 	|
| extTFS_luGhgEmissions	 								| Yes                           	|    51	 	|
| aircraftGhgEmissions			   						| No						      	|    52   	|
| extWBCSD_packagingGhgEmissions						| Yes							    |	 53		|
| distributionStagePcfExcludingBiogenic					| Yes 							    | 	 54	   	|
| distributionStagePcfIncludingBiogenic					| No                            	| 	 55	  	|
| distributionStageFossilGhgEmissions  					| Yes                           	| 	 56	  	|
| distributionStageBiogenicCarbonEmissionsOtherThanCO2	| Yes                               |    57	 	|
| distributionStageBiogenicCarbonWithdrawal				| Yes                           	|    58	 	|
| extTFS_distributionStageDlucGhgEmissions		   		| No						      	|    59   	|
| extTFS_distributionStageLuGhgEmissions				| Yes							    |	 60		|
| carbonContentTotal    								| Yes 							    | 	 61	   	|
| extWBCSD_fossilCarbonContent  	    				| No                            	| 	 62	  	|
| distributionStageAircraftGhgEmissions					| No								|	 63		|
| carbonContentBiogenic 		      					| Yes                           	| 	 64	  	|
| assetLifeCyclePhase		 							| Yes                               |    65	 	|


#### [CSV Sample File Link]

#### Example for submodel PCF

### Work Flow 

 - CSV to POJO
 - CSV column validation and mandatory field validation
 - POJO TO DTO
 - UUID generate v4
 - DigitalTwins API's calls 
 - EDC API's calls
 - BPN Discovery API Call
 - DB Store
 
### External Services Call

 - DigitalTwins
 - EDC Connectors
 - BPN Discovery
 
