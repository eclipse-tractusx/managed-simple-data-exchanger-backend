   # Data Format Transformer(Simple Data Exchanger)
---
## Description

This repository is part of the overarching Catena-X project. It contains the Backend for the DFT.
DFT is short for Data Format Transformer.

It is a standalone service which can be self-hosted. 
It enables companies to provide their data in the Catena-X network via an EDC.
Data is uploaded via two CSV-files. The DFT registers the data in the Digital Twin Registry and makes it accessible via an EDC.
The DFT project has two dependencies: Digital Twins and EDC.

##### For installation guide: see [InstallationGuide.md](InstallationGuide.md)

### How to run

DFT is a SpringBoot Java software project managed by Maven.

When running, the project requires a postgresql database to be available to connect to. Per default configuration the application expects postgres to run on localhost on port 5432.

You can find the standard credentials as well as further database configurations int the application.properties file in the resource folder.


### Prerequisites
- JDK18
- Postgres 13.2
- Docker

### Steps
1. Clone the GitHub Repository - https://github.com/eclipse-tractusx/dft-backend
2. Get your instance of postgres running.(Create **dftdb** new database)
3. Setup your project environment to JDK 18
4. Start the application from your IDE.


 ---   

## DFT(Simple Data Exchanger) Compatible with :=
1. ***File Uploads***
    * SerialPartTypization
    * AssemblyPartRelationship
    * Batch
    * PartAsPlanned
    * SingleLevelBoMAsPlanned
    * PartSiteInformationAsPlanned
    * SingleLevelUsageAsBuilt
2. ***Json Update***
    * SerialTypezation
    * AssemblyRelationship
    * Batch
    * PartAsPlanned
    * SingleLevelBoMAsPlanned
    * PartSiteInformationAsPlanned
 	* SingleLevelUsageAsBuilt
3. ***Application UI***
    * SerialTypezation
    * AssemblyRelationship
    * Batch
    * PartAsPlanned
    * SingleLevelBoMAsPlanned
    * PartSiteInformationAsPlanned
    * SingleLevelUsageAsBuilt


 ---   
##  RESTful APIs OF DFT (Simple Data Exchanger) 

###### ***Note: API_KEY, AUTHORISATION TOKEN Required as Headers***
---
| API | Description |  Request body | Response body |
| ------ | ------ | ------ | ------ |
| **GET:- localhost:8080/api/submodels** |This API is used to get all submodels list which is implemented/supported by SDE | Refer Api Doc | Refer Api Doc |
| **GET:- localhost:8080/api/submodels/schema-details** |This API is used to get schema details of submodels which is implemented/supported by SDE | Refer Api Doc | Refer Api Doc |
| **GET:- localhost:8080/api/submodels/{submodelName}** |This API is used to get the schema data of specific model | Refer Api Doc | Refer Api Doc |
| **POST:- localhost:8080/api/{submodel}/upload** |This API is used to uploading data From CSV file for particular selected submodel | Refer Api Doc |4ca03d5f-9e37-4c12-a8b8-6583b81892c8 |
| **POST:- localhost:8080/api/{submodel}/manualentry** |This API is used for uploading data From JSon/Tabular form | Refer Api Doc | 4ca03d5f-9e37-4c12-a8b8-6583b81892c8 |
| **GET:- localhost:8080/api/{submodel}/public/{uuid}** |This API is used for to get the specific submodel data | Refer Api Doc | Refer Api Doc |
| **DELETE:- localhost:8080/api/{submodel}/delete/{processId}** |This API is used to delete processed data from EDC and DigitalTwins | Refer Api Doc | Refer Api Doc |
| **GET:- localhost:8080/api/role/{role}/permissions** |This API is used to fetch all permissions associate with particular role | Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/user/role/permissions** |This API is used to fetch all list of permissions | Refer Api Doc |Refer Api Doc|
| **POST:- localhost:8080/api/role/{role}/permissions** |This API is used to apply list of permissions to the specific role | Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/processing-report/87d0aece-ae46-4006-904d-9ec41cddee8b** |This API Is Used For fetch Process Report by Process ID| Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/ping** |This API Is Used For Health Check| -- |2022-09-30T16:21:02.630868|
| **GET:- localhost:8080/api/processing-report?page=&pageSize=50** |This API Is Used For fetch Process Report| Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/query-data-offers** |This API is used to fetch all data offers of provider URL | Refer Api Doc |Refer Api Doc|
| **POST:- localhost:8080/api/subscribe-data-offers** |This API is used to subscribe data offers | Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/contract-offers** |This API is used to get all contract offers | Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/legal-entities** |This API is used to fetch legal entities (list of company's) for Process| Refer Api Doc |Refer Api Doc|
| **POST:- localhost:8080/api/connectors-discovery** |This API is used to fetch  connectores information | Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/submodels** |This API is used to get all submodels list which is implemented/supported by SDE | Refer Api Doc | Refer Api Doc |
| **GET:- localhost:8080/api/submodels/{submodelName}** |This API is used to get the schema data of specific model | Refer Api Doc | Refer Api Doc |
| **POST:- localhost:8080/api/{submodel}/upload** |This API is used to uploading data From CSV file for particular selected submodel | Refer Api Doc |4ca03d5f-9e37-4c12-a8b8-6583b81892c8 |
| **POST:- localhost:8080/api/{submodel}/manualentry** |This API is used for uploading data From JSon/Tabular form | Refer Api Doc | 4ca03d5f-9e37-4c12-a8b8-6583b81892c8 |
| **GET:- localhost:8080/api/{submodel}/public/{uuid}** |This API is used for to get the specific submodel data | Refer Api Doc | Refer Api Doc |
| **DELETE:- localhost:8080/api/{submodel}/delete/{processId}**|This API is used to delete processed data from EDC and DigitalTwins | Refer Api Doc | Refer Api Doc |
| **GET:- localhost:8080/api/role/{role}/permissions** |This API is used to fetch all permissions associate with particular role | Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/user/role/permissions** |This API is used to fetch all list of permissions | Refer Api Doc |Refer Api Doc|
| **POST:- localhost:8080/api/role/{role}/permissions** |This API is used to apply list of permissions to the specific role | Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/processing-report/87d0aece-ae46-4006-904d-9ec41cddee8b** |This API Is Used For fetch Process Report by Process ID| Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/ping** |This API Is Used For Health Check| -- |2022-09-30T16:21:02.630868|
| **GET:- localhost:8080/api/processing-report?page=&pageSize=50** |This API Is Used For fetch Process Report| Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/query-data-offers** |This API is used to fetch all data offers of provider URL | Refer Api Doc |Refer Api Doc|
| **POST:- localhost:8080/api/subscribe-data-offers** |This API is used to subscribe data offers | Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/contract-offers** |This API is used to get all contract offers | Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/legal-entities** |This API is used to fetch legal entities (list of company's) for Process| Refer Api Doc |Refer Api Doc|
| **POST:- localhost:8080/api/connectors-discovery** |This API is used to fetch connectors information | Refer Api Doc |Refer Api Doc|


---
#### **Response Status**
---
### SUCCESS RESPONSE CODES:
| Code |  DESCRIPTION |
| ------ | ------ |
| 200 OK  | Indicates that the request has succeeded. |
| 201 Created | Indicates that the request has succeeded and a new resource has been created as a result. |
| 202 Accepted | Indicates that the request has been received but not completed yet |
| 204 No Content | The server has fulfilled the request but does not need to return a response body. The server may return the updated meta information.|


### ERROR RESPONSE CODES:
| Code |  DESCRIPTION |
| ------ | ------ |
|400 Bad Request | The request could not be understood by the server due to incorrect syntax. |
|401 Unauthorized | Indicates that the request requires user authentication information. |
|403 Forbidden| Unauthorized request.  |
|404 Not Found| The server can not find the requested resource.  |
|405 Method Not Allowed| The request HTTP method is known by the server but has been disabled and cannot be used for that resource  |
|500 Internal Server Error| The server encountered an unexpected condition that prevented it from fulfilling the request.|
|502 Bad Gateway | The server got an invalid response while working as a gateway to get the response needed to handle the request.|
|503 Service Unavailable | The server is not ready to handle the request.|
|504 Gateway Timeout|The server is acting as a gateway and cannot get a response in time for a request. |

---
## Database
| Tables | Description |  Unique Id |
| ------ | ------ | ------ |
| **aspect** | Table used to Store Date About Serialized Part |  **Primary Key**:UUID |
| **aspect_relationship** |Data about the relationship of parts to its child-components. | **Primary Key**:parent_catenax_id, child_catenax_id |
| **batch** |Table used to Store Date about Serialized Part. |  **Primary Key**:UUID |
| **part_as_planned** |Table used to Store Date about Part As Planned. |  **Primary Key**:UUID |
| **single_level_bom_as_planned** |Data about the relationship of part As Planned to its child-components. | **Primary Key**:parent_catenax_id, child_catenax_id |
| **part_site_information_as_planned** |Table used to Store Date about Part Site Information As Planned. |  **Primary Key**:UUID |
| **contract_negotiation_info** |Tables Contains Cntract Negotiotion Info and offerid |  **Primary Key**: connector_id, offer_id |
| **failure_log** |Table Contains Data About Failure Entries |  **Primary Key**:UUID |
| **Flyway_Schema_History** |Table Contains data Migration History |  **Primary Key**:installed_rank |
| **Process_Report** |Table Contains status of Processing upload |  **Primary Key**:process_id |
| **sde_role** |Table Contains list of roles |  **Primary Key**:sde_role |
| **sde_permission** |Table Contains list of permissions |  **Primary Key**:sde_permission |
| **sde_role_permission_mapping** |Table Contains mapping of role with permissions |  **Primary Key**:sde_role, sde_permission |

---
## flyway
The scripts are in the folder: resources/flyway.<p>
File naming: <b>Vx__script_name.sql</b>, where x is the version number. <p>
When there is a need to change the last script, it is necessary to create a new script with the changes.

Link to flyway documentation: [Documentation](https://flywaydb.org/documentation/) 

## API authentication
Authentication for the backend is handled via an API Key. This can be set in the configuration file.

## ArgoCD
The latest version on main is automatically picked up by ArgoCD and deployed to the environment using Helm charts.
   
 helm repo add catenax-ng-product-dft-backend https://github.com/catenax-ng/product-dft-backend/tree/main/charts
   
 helm install release-name catenax-ng/product-dft-backend

### EDC
GitHub repository with correct version of the Eclipse DataSpace Connector Project: [repository](https://github.com/catenax-ng/product-edc)

### Licenses
Apache 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
