# Installation Guide

It is necessary to inject the environment variables, credentials and URLs that can be found on application.properties file.
For more details, please refer configuration section from [README.md](README.md)

### RUN SDE backend in ArgoCD 
 We have helm chart available for ArgoCD deployment. In deployment, if don't specified specific version, the app version/chart version is automatically picked up by ArgoCD and deployed to the environment using Helm charts.

 In values.yaml you can find `default` as value for all required configuration. You need to change all those values as per your need. For reference, please refer configuration example section.
 
 As part of argo CD deployment using helm chart the postgres database dependency will get provide automatic but for EDC, DigitalTwin and Portal you need to provide valid details as per configuration requirement other wise SDE service will get started with default configuration but will not work as expected.

### RUN SDE Backend in k8s cluster
#### Prerequisites
- k8s cluster/ minikube
- helm
- Docker

 In values.yaml you can find `default` as value for all required configuration. You need to change all those values as per your need. For reference, please refer configuration example section.

 ```shell
  helm repo add sdebackend https://eclipse-tractusx.github.io/charts/dev 

  helm search repo sdebackend/sde

  helm install -f your-values.yaml release-name sdebackend/sde-app 
 ``` 
### RUN SDE Backend Locally
#### Prerequisites
- JDK17
- Postgres 12.12.10

#### Steps
1. Clone the GitHub Repository - https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend.
2. Get your instance of postgres running (Create **sdedb** new database).
3. Setup your project environment to JDK 17.
4. Provide require application configuration in application.properties as specified in step configuration.properties.
5. Start the SDE spring boot application from your IDE using main class or use spring CLI.

#### Building 
SDE-backend use Maven for building process. To build a service from sources one need to go to corresponding directory and trigger building process:

 ``` shell
  cd managed-simple-data-exchanger-backend 

  ./mvnw clean install 
 ```

Then fat jar file can be found in modules/sde-core/target folder as well as in local Maven repository. it can be run with this command: 

 ``` shell
  java -jar target/sde-core-0.0.1.jar 
 ```

## Upload a file:
When a file .csv is uploaded, the program checks whether the file is a SerialPartTypization or an AssemblyPartRelationship and there is a pipeline for each one.
Apart from both upload Batch upload is additional feature were added into DFT.

<b>For Serial Part:</b>

1. Maps the content of the line with an Serial Part Typization Aspect.
2. Generates the UUID if it does not contain a UUID.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

<b>For Single Level BoM As Built:</b>

1. Maps the content of the line with an Assembly Part Relationship Aspect Relationship.
2. checks if an Serial Part Typization Aspect exists so it can be related to that Aspect.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

<b>For Single Level Usage As Built:</b>

1. Maps the content of the line with an Single Level Usage As Built Aspect Relationship.
2. checks if an Serial Part Typization Aspect exists so it can be related to that Aspect.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

<b>For Batch Upload:</b>

1. Maps the content of the line with an Batch.
2. Generates the UUID if it does not contain a UUID.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

<b>For PartAsPlanned Upload:</b>

1. Maps the content of the line with an PartAsPlanned.
2. Generates the UUID if it does not contain a UUID.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

<b>For PartTypeInformation Upload:</b>

1. Maps the content of the line with an PartTypeInformation.
2. Generates the UUID if it does not contain a UUID.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

<b>For SingleLevelBoMAsPlanned Upload:</b>

1. Maps the content of the line with an SingleLevelBoMAsPlanned.
2. checks if an PartAsPlanned Aspect exists so it can be related to that Aspect.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

<b>For PartSiteInformationAsPlanned Upload:</b>

1. Maps the content of the line with an PartSiteInformationAsPlanned.
2. checks if an PartAsPlanned Aspect exists so it can be related to that Aspect.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

<b>For Pcf Upload:</b>

1. Maps the content of the line with an Pcf.
2. checks if an PartAsPlanned Aspect exists so it can be related to that Aspect.
3. Registers in DigitalTwins.
4. Create Asset in EDC Connector.
5. Stores the line in the database.

The file .csv is loaded in memory, the content is saved and then, the file is removed from memory.


If the file is not .csv, it is read, processed and is considered as FAILED.

