# Installation Guide
## Product DFT
Install from the command line:

`docker pull ghcr.io/catenax-ng/product-dft-backend:main`


It is necessary to inject the environment variables, credentials and URLs that can be found on application.properties file.
#### CatenaX variables
| Property       | Value          | Description        | Example |
|----------------|----------------|--------------------|---------|
| manufacturerId | MANUFACTURERID | Id of manufacturer | CatenaX |


#### Digital Twins variables and endpoints to run locally:
| Property                                  | Value                                     | Description                                    | Example                          |
|-------------------------------------------|-------------------------------------------|------------------------------------------------|----------------------------------|
| digital-twins.hostname                    | DIGITAL-TWINS_HOSTNAME                    | hostname for Digital Twins                     | https://                         |
| digital-twins.authentication.url          | DIGITAL-TWINS_AUTHENTICATION_URL          | authentication url for Digital Twins           | https://                         |
| digital-twins.authentication.clientId     | DIGITAL-TWINS_AUTHENTICATION_CLIENTID     | client ID authentication for Digital Twins     | sa-cl6-cx-4                      |
| digital-twins.authentication.clientSecret | DIGITAL-TWINS_AUTHENTICATION_CLIENTSECRET | client secret authentication for Digital Twins | VrL8uSG5Tn3NrFiY39vs0klTmlvsRRmo |

The values are on the [Vault](https://vault.vault.demo.catena-x.net/).
*<i><b>Must create a GitHub token to access</b></i>

#### EDC variables and endpoints to run locally:
| Property                        | Value                           | Description                                   | EXAMPLE      |
|---------------------------------|---------------------------------|-----------------------------------------------|--------------|
| edc.aspect.hostname             | EDC_ASPECT_HOSTNAME             | hostname for aspect edc                       | https://     |
| edc.apiKeyHeader                | EDC_APIKEYHEADER                | API KEY header for edc                        |              |
| edc.apiKey                      | EDC_APIKEY                      | API KEY for edc                               | apiKey       |
| dft.hostname                    | DFT_HOSTNAME                    | hostname for DFT                              | apiValue     |
| edc.asset.payload.url.auth.key  | EDC_ASSET_PAYLOAD_URL_AUTH_KEY  | url authentication key for edc asset payload  | someKey      |
| edc.asset.payload.url.auth.code | EDC_ASSET_PAYLOAD_URL_AUTH_CODE | url authentication code for edc asset payload | someCode     |
| edc.enable                      | EDC_ENABLE                      | enable / disable edc                          | true / false |

The values are on the [Vault](https://vault.vault.demo.catena-x.net/).
*<i><b>Must create a GitHub token to access</b></i> 

## Upload a file:
When a file .csv is uploaded, the program checks whether the file is a SerialPartTypization or an AssemblyPartRelationship and there is a pipeline for each one.

<b>For Serial Part Typization:</b>

1. Maps the content of the line with an Aspect.
2. Generates the UUID if it does not contain a UUID.
3. Registers in DigitalTwins.
4. Stores the line in the database.

<b>For Assembly Part Relationship:</b>

1. Maps the content of the line with an Aspect Relationship.
2. checks if an Aspect exists so it can be related to that Aspect.
3. Registers in DigitalTwins.
4. Stores the line in the database.

The file .cvs is loaded in memory, the content is saved and then, the file is removed from memory.


If the file is not .csv, it is read, processed and is considered as FAILED



