 # Product Carbon Footprint(PCF) (SDE Maven module)
---
## Description

This module use for PCF submodel specification and descriptors. It's contain the codes related to PCF to validate, parse and transfer data for DigitalTwins and EDC to create aspect twins and data offer.

---
#### Version: 4.0.1
#### PCF Aspect Model URN: urn:bamm:io.catenax.pcf:4.0.1#Pcf
#### Semantic Id: urn:bamm:io.catenax.pcf:4.0.1
---

### Schema

Please find below links for schema details:

- [schema](src/main/resources/pcf.json)


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
| carbonContentBiogenic 		      					| Yes                           	| 	 63	  	|
| assetLifeCyclePhase		 							| Yes                               |    64	 	|


#### [CSV Sample File Link]

#### Example for submodel PCF

<br/><br/><img src="src/main/resources/images/pcf.png" height="60%" width="80%"/><br/><br/>

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
 
[CSV Sample File Link]: src/main/resources/pcf.csv
