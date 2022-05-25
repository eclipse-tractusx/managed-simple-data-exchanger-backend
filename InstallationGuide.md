#Product DFT
Install from the command line: <p>

`docker pull ghcr.io/catenax-ng/product-dft-backend:main`


It is necessary to inject the environment variables, credentials and URLs that can be found on application.properties file.
####CatenaX variables
    manufacturerId=catenaX

####Digital Twins variables and endpoints:
    digital-twins.url=
    digital-twins.authentication.url=
    digital-twins.authentication.clientId=
    digital-twins.authentication.clientSecret=

####EDC variables and endpoints:
    edc.aspect.url=
    edc.child.aspect.url=
    edc.asset.payload.url=
    edc.asset.payload.url.auth.key=
    edc.asset.payload.url.auth.code=
    edc.aspect.apiKey=
    edc.aspect.apiValue=
    edc.child.aspect.apiKey=
    edc.child.aspect.apiValue=
    edc.asset.relationship.payload.url=


##Upload a file:
When a file .csv is uploaded, the program checks whether the file is a SerialPartTypization or an AssemblyPartRelationship and there is a pipeline for each one.
<p>
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



