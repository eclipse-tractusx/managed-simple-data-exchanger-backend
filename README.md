   # Simple Data Exchanger (formally known Data Format Transformer)
---
## Description

This repository is part of the overarching Eclipse Tractus-X project. It contains the Backend for the SDE/DFT.
SDE Simple data exchanger(formally known DFT is short for Data Format Transformer)

It is a standalone service which can be self-hosted. 
It enables companies to provide their data in the Eclipse Tractus-X network via an EDC.

## Important !!!
### Deployment of SDE backend
The Auto-Setup is the central service orchestration component. The Auto-Setup hide all complex configuration properties for you and get SDE Backend as well as Frontend service deployed for you as service. The Auto-Setup taking all deployment through their specific helm charts. The Auto-Setup knows which prerequisites and which configurations are required for the components and creates them. All dependencies and any error messages are intercepted by Auto-Setup and treated correctly and meaningfully. Therefore, Auto-Setup meets your requirements exactly.

Once SDE deployed, The data is uploaded via CSV-files or tabular entry. The SDE registers the data in the Digital Twin Registry and makes it accessible via an EDC.

The SDE project has three dependencies: Digital Twins, Portal and EDC.

## How to run

For SDE installation, please refer the [INSTALL](INSTALL.md) file 

SDE is a SpringBoot Java Maven software project.

When running, the project requires a postgresql database to be available to connect.
You can find the standard require configuration keys as below:

### Configuration

Listed below are configuration keys needed to get the `sde-backend` up and running.

| Key  	                                               | Required  | Example                                     | Description                        |
|---	                                               |---	       |---	                                         |---                                 |
| keycloak.clientid                                    | X         | sdeclientId                                 | This is keycloak clienId/resource  |
| spring.security.oauth2.resourceserver.jwt.issuer-uri | X         | https://ids.issuer.com/auth/realms/master   | Url of Keycloak issuer uri         |
| management.endpoint.health.probes.enabled            | X         | true                                        | Default value, no need to change   |
| management.health.readinessstate.enabled             | X         | true                                        | Default value, no need to change   |
| management.health.livenessstate.enabled              | X         | true                                        | Default value, no need to change   |
| management.endpoints.web.exposure.include            | X         | *                                           | Default value, no need to change   |
| spring.lifecycle.timeout-per-shutdown-phase          | X         | 30s                                         | Default value, no need to change   | 
| logging.level.org.springframework.security.web.csrf  | X         | INFO                                        | Default value, no need to change   |
| logging.level.org.apache.http                        | X         | info                                        | Default value, no need to change   |
| logging.level.root                                   | X         | info                                        | Default value, no need to change   |
| file.upload-dir                                      | X         | ./temp/                                     | Default value, no need to change   |
| spring.servlet.multipart.enabled                     | X         | true                                        | Default value, no need to change   |
| spring.main.allow-bean-definition-overriding         | X         | true                                        | Default value, no need to change   |
| spring.servlet.multipart.file-size-threshold         | X         | 2KB                                         | Default value, no need to change   |
| spring.servlet.multipart.max-file-size               | X         | 200MB                                       | Default value, no need to change   |
| spring.servlet.multipart.max-request-size            | X         | 215MB                                       | Default value, no need to change   |
| server.servlet.context-path                          | X         | /api                                        | Default value, no need to change   |
| spring.flyway.baseline-on-migrate                    | X         | true                                        | Default value, no need to change   |
| spring.flyway.locations                              | X         | classpath:/flyway                           | Default value, no need to change   |
| spring.datasource.driver-class-name                  | X         | org.postgresql.Driver                       | Default value, no need to change   |
| spring.datasource.url                                | X         | jdbc:postgres//dbserver.com:5432/db         | Your database server details       |
| spring.datasource.username                           | X         |                                             | Your database password             |
| spring.datasource.password                           | X         |                                             | Your database password             |
| spring.jpa.hibernate.ddl-auto                        |           | update                                      | Default value, no need to change   |
| spring.jpa.open-in-view                              |           | false                                       | Default value, no need to change   |
| digital-twins.hostname                               | X         | https://example.digitaltwin.com             | Digital twin registry url          |
| digital-twins.authentication.url                     | X         | http://ex*.keycloak.com/auth/realms/default | Digital twin registry auth url     |
| digital-twins.authentication.clientId                | X         | your clientId                               | Digital twin registry clientId     |
| digital-twins.authentication.clientSecret            | X         | your secrete                                | Digital twin registry secrete      |
| digital-twins.authentication.grantType               | X         | client_credentials                          | Default value, no need to change   |
| edc.hostname                                         | X         | https://example.provider-connector.com      | Your EDC provider connector url    |
| edc.managementpath                                   | X         | default                                     | edc provider management path       |
| edc.apiKeyHeader                                     | X         | x-api-key                                   | Your connector api key             |
| edc.apiKey                                           | X         | yourpass                                    | Your connector apikey value        |
| edc.consumer.hostname                                | X         | https://example.consumer-connector.com      | Your EDC consumer connector        |
| edc.consumer.apikeyheader                            | X         | x-api-key                                   | Your connector api key             |
| edc.consumer.apikey                                  | X         | yourpass                                    | Your connector apikey value        |
| edc.consumer.datauri                                 | X         | /api/v1/ids/data                            | IDS endpoint path                  |
| edc.consumer.protocol.path                           | X         | default                                     | edc consumer protocol path         |
| edc.consumer.managementpath                          | X         | default                                     | edc consumer management path       |
| dft.hostname                                         | X         | https://example.sdehost.com                 | Your SDE hostname                  |
| dft.apiKeyHeader                                     | X         | API_KEY                                     | Your default key                   |
| dft.apiKey                                           | X         | yourpass                                    | Your default key password          |
| manufacturerId                                       | X         | default                                     | Your CX partner BPN number         |
| partner.pool.hostname                                | X         | default                                     | url to get legal-entity information|
| connector.discovery.token-url                        | X         | https://example.portal.backend.com          | Portal backend AuthURL             | 
| connector.discovery.clientId                         | X         | default                                     | client ID for connector discovery  |
| connector.discovery.clientSecret                     | X         | default                                     | password for connector discovery   |
| portal.backend.hostname                              | X         | default                                     | Portal backend svc URL based on BPN|
| springdoc.api-docs.path                              | X         | default                                     | swagger API path                   |
| bpndiscovery.hostname                                | X         | default                                     | bpn discovery hostname             |
| discovery.authentication.url                         | X         | default                                     | discovery authentication url       |
| discovery.clientId                                   | X         | default                                     | discovery clientId                 |
| discovery.clientSecret                               | X         | default                                     | discovery clientSecret             |
| discovery.grantType                                  | X         | default                                     | discovery grantType                |
| partner.pool.hostname                                | X         | default                                     | partner pool hostname              |
| partner.pool.authentication.url                      | X         | default                                     | partner pool authentication url    |
| partner.pool.clientId                                | X         | default                                     | partner pool clientId              |
| partner.pool.clientSecret                            | X         | default                                     | partner pool clientSecret          |
| partner.pool.grantType                               | X         | default                                     | partner pool grantType             |
| portal.backend.hostname                              | X         | default                                     | portal backend hostname            |
| portal.backend.authentication.url                    | X         | default                                     | portal authentication url          |
| portal.backend.clientId                              | X         | default                                     | portal clientId                    |
| portal.backend.clientSecret                          | X         | default                                     | portal clientSecret                |
| portal.backend.grantType                             | X         | default                                     | portal grantType                   |
| policy.hub.hostname	                               | X         | default                                     | policy hub hostname                |
| policy.hub.authentication.url	                       | X         | default                                     | policy hub authentication url      |
| policy.hub.clientId			                       | X         | default                                     | policy hub clientId                |
| policy.hub.clientSecret			                   | X         | default                                     | policy hub clientSecret            |
| policy.hub.grantType			              		   | X         | default                                     | policy hub grantType            |


#### Example Configuration/application.properties

```
keycloak.clientid=sdeclientId
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://ids.issuer.com/auth/realms/master
management.endpoint.health.probes.enabled=true
management.health.readinessstate.enabled=true
management.health.livenessstate.enabled=true
management.endpoints.web.exposure.include=*
spring.lifecycle.timeout-per-shutdown-phase=30s

#provider your logging level
logging.level.org.springframework.security.web.csrf=INFO
logging.level.org.apache.http=info
logging.level.root=info

#default spring boot configuration not need to change
file.upload-dir=./temp/
spring.servlet.multipart.enabled=true
spring.main.allow-bean-definition-overriding=true
spring.servlet.multipart.file-size-threshold=2KB
spring.servlet.multipart.max-file-size=200MB
spring.servlet.multipart.max-request-size=215MB

#API context path to access application apis
server.servlet.context-path=/api

#Database and flyway details, the database will 
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:/flyway
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgres//dbserver.com:5432/dftdb #your database server details
spring.datasource.username=your database password
spring.datasource.password=your database password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false

#Provide digital twin registry details which SDE should use to create twin for your, 
#The need technical user details depend on digital twin security configuration
digital-twins.hostname=https://example.digitaltwin.com
digital-twins.authentication.url=http://example.keycloak.com/auth/realms/default
digital-twins.authentication.clientId=your clientId
digital-twins.authentication.clientSecret=your secrete
digital-twins.authentication.grantType=client_credentials

#The EDC connector information which SDE should use As Data provider connector
edc.hostname=https://example.provider-connector.com
edc.apiKeyHeader=your connector api key
edc.apiKey=your connector apikey value 

#The EDC connector information which SDE should use As Data consumer connector
edc.consumer.hostname=https://example.consumer-connector.com
edc.consumer.apikeyheader=your connector api key
edc.consumer.apikey=your connector apikey value 
edc.consumer.datauri=/api/v1/ids/data

#Your Own SDE host url which will share with EDC connector as data address proxy
dft.hostname=https://example.sdehost.com
dft.apiKeyHeader=your default key
dft.apiKey=your default key password

#Your company BPN number
manufacturerId=default

#Portal pool hostname url to use discover legal company information in SDE
partner.pool.hostname=default

#Portal backend url for get connector list based on BPN number
connector.discovery.token-url=https://example.portal.backend.com
connector.discovery.clientId=default
connector.discovery.clientSecret=default
portal.backend.hostname=default
springdoc.api-docs.path=/api-docs
bpndiscovery.hostname=default
discovery.authentication.url=default
discovery.clientId=default
discovery.clientSecret=default
discovery.grantType=default
edc.consumer.protocol.path=default
edc.consumer.managementpath=default
edc.managementpath=default
partner.pool.hostname=default
partner.pool.authentication.url=default
partner.pool.clientId=default
partner.pool.clientSecret=default
partner.pool.grantType=default
portal.backend.hostname=default
portal.backend.authentication.url=default
portal.backend.clientId=default
portal.backend.clientSecret=default
portal.backend.grantType=default
bpndiscovery.hostname=default
discovery.authentication.url=default
discovery.clientId=default
discovery.clientSecret=default
discovery.grantType=default
edc.consumer.protocol.path=default
edc.consumer.managementpath=default
edc.managementpath=default
partner.pool.hostname=default
partner.pool.authentication.url=default
partner.pool.clientId=default
partner.pool.clientSecret=default
partner.pool.grantType=default
portal.backend.hostname=default
portal.backend.authentication.url=default
portal.backend.clientId=default
portal.backend.clientSecret=default
portal.backend.grantType=default
policy.hub.hostname=default
policy.hub.authentication.url=default
policy.hub.clientId=default
policy.hub.clientSecret=default
policy.hub.grantType=default
```

The above configuration we can use as for different deployment as specified here [InstallationGuide.md](InstallationGuide.md)

---
### Supported submodules
To find information about supported submodules and there version in SDE please visit [here](modules/sde-submodules/submodules.md) 

---

## DFT(Simple Data Exchanger) Compatible with :=
1. ***File Uploads***
    * SerialPart
    * SingleLevelBoMAsBuilt
    * Batch
    * PartAsPlanned
    * PartTypeInformation
    * SingleLevelBoMAsPlanned
    * PartSiteInformationAsPlanned
    * SingleLevelUsageAsBuilt
    * Product Carbon Footprint(PCF)
2. ***Json Update***
    * SerialPart
    * SingleLevelBoMAsBuilt
    * Batch
    * PartAsPlanned
    * PartTypeInformation
    * SingleLevelBoMAsPlanned
    * PartSiteInformationAsPlanned
 	* SingleLevelUsageAsBuilt
 	* Product Carbon Footprint(PCF)
3. ***Application UI***
    * SerialPart
    * SingleLevelBoMAsBuilt
    * Batch
    * PartAsPlanned
    * PartTypeInformation
    * SingleLevelBoMAsPlanned
    * PartSiteInformationAsPlanned
    * SingleLevelUsageAsBuilt
    * Product Carbon Footprint(PCF)


 ---   
##  RESTful APIs OF DFT (Simple Data Exchanger) 

###### ***Note: API_KEY, AUTHORIZATION TOKEN Required as Headers***
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
| **POST:- localhost:8080/api/connectors-discovery** |This API is used to fetch  connector's information | Refer Api Doc |Refer Api Doc|
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
| **GET:- localhost:8080/api/policy-attributes** |This API is used to fetch policy attributes | Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/policy-types** |This API is used to fetch type of policy attributes | Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/policy-content** |This API is used to fetch policy content | Refer Api Doc |Refer Api Doc|
| **POST:- localhost:8080/api/policy-content** |This API is used to create policy content | Refer Api Doc |Refer Api Doc|
| **POST:- localhost:8080/api/policy** |This API is used to save policy | Refer Api Doc |Refer Api Doc|
| **PUT:- localhost:8080/api/policy/{uuid}** |This API is used to update policy | Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/policy/{uuid}** |This API is used to get policy | Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/policy/is-policy-name-valid** |This API is used to check policy name valid or not | Refer Api Doc |Refer Api Doc|
| **GET:- localhost:8080/api/policy** |This API is used to all policy | Refer Api Doc |Refer Api Doc|
| **DELETE:- localhost:8080/api/policy/{uuid}** |This API is used to delete policy | Refer Api Doc |Refer Api Doc|

## Detailed API specs available under:

- [SDE-OPEN-API](src/main/resources/sde-open-api.yml)

## Backend API Swagger-ui : 
https://<host-url>/backend/api/swagger-ui/index.html

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
| **serialpart_v_300** | Table used to Store Date About Serialized Part |  **Primary Key**:UUID |
| **aspect_relationship** |Data about the relationship of parts to its child-components. | **Primary Key**:parent_catenax_id, child_catenax_id |
| **single_level_bom_asbuilt_v_300** |Data about the relationship of parts to its child-components. | **Primary Key**:parent_catenax_id, child_catenax_id |
| **batch** |Table used to Store Date about Serialized Part. |  **Primary Key**:UUID |
| **batch_v_300** |Table used to Store Date about Serialized Part. |  **Primary Key**:UUID |
| **part_as_planned** |Table used to Store Date about Part As Planned. |  **Primary Key**:UUID |
| **part_type_information** |Table used to Store Date about Part As Planned. |  **Primary Key**:UUID |
| **pcf_aspect** |Table used to Store Date about Part As Planned. |  **Primary Key**:UUID |
| **single_level_bom_as_planned** |Data about the relationship of part As Planned to its child-components. | **Primary Key**:parent_catenax_id, child_catenax_id |
| **single_level_bom_as_planned_v_300** |Data about the relationship of part As Planned to its child-components. | **Primary Key**:parent_catenax_id, child_catenax_id |
| **part_site_information_as_planned** |Table used to Store Date about Part Site Information As Planned. |  **Primary Key**:UUID |
| **contract_negotiation_info** |Tables Contains Contract Negotiation Info and offerid |  **Primary Key**: connector_id, offer_id |
| **failure_log** |Table Contains Data About Failure Entries |  **Primary Key**:UUID |
| **Flyway_Schema_History** |Table Contains data Migration History |  **Primary Key**:installed_rank |
| **Process_Report** |Table Contains status of Processing upload |  **Primary Key**:process_id |
| **sde_role** |Table Contains list of roles |  **Primary Key**:sde_role |
| **sde_permission** |Table Contains list of permissions |  **Primary Key**:sde_permission |
| **sde_role_permission_mapping** |Table Contains mapping of role with permissions |  **Primary Key**:sde_role, sde_permission |
| **single_level_usage_as_built** |Data about the relationship of parts to its child-components. | **Primary Key**:parent_catenax_id, child_catenax_id |
| **single_level_usage_as_built_v_300** |Data about the relationship of parts to its child-components. | **Primary Key**:parent_catenax_id, child_catenax_id |

---
## flyway
The scripts are in the folder: resources/flyway.<p>
File naming: <b>Vx__script_name.sql</b>, where x is the version number. <p>
When there is a need to change the last script, it is necessary to create a new script with the changes.

Link to flyway documentation: [Documentation](https://flywaydb.org/documentation/).

## API authentication
Authentication for the backend is handled via an API Key. This can be set in the configuration file.

### EDC
GitHub repository with correct version of the Eclipse DataSpace Connector Project: [repository](https://github.com/eclipse-tractusx/tractusx-edc).

## Licenses
For used licenses, please see the [NOTICE](https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/blob/main/NOTICE.md).

## Eclipse Dash Tool

The Eclipse Dash tool is used to analyze the dependencies used in the project and ensure all legal requirements are met. We're using the official maven plugin to resolve all project dependencies and then run the tool and update the summary in the DEPENDENCIES file.

## Notice for Docker image

Bellow you can find the information regarding Docker Notice for this application.

  - [Managed-simple-data-exchanger](DOCKER_NOTICE.md)


