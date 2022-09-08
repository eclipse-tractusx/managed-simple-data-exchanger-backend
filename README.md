# Data Format Transformer

## Description

This repository is part of the overarching Catena-X project. It contains the Backend for the DFT.
DFT is short for Data Format Transformer.

It is a standalone service which can be self-hosted. 
It enables companies to provide their data in the Catena-X network via an EDC.
Data is uploaded via two CSV-files. The DFT registers the data in the Digital Twin Registry and makes it accessible via an EDC.
The DFT project has two dependencies: Digital Twins and EDC.

##### For installation guide: see [InstallationGuide.md](InstallationGuide.md)

#### How to run

DFT is a SpringBoot Java software project managed by Maven.

When running, the project requires a postgresql database to be available to connect to. Per default configuration the application expects postgres to run on localhost on port 5432.

You can find the standard credentials as well as further database configurations int the application.properties file in the resource folder.


### Prerequisites
- JDK18
- Postgres 13.2
- Docker

### Steps
1. Clone the GitHub Repository - https://github.com/catenax-ng/product-dft-backend
2. Get your instance of postgres running.
3. Setup your project environment to JDK 18
4. Start the application from your IDE.

## Database
## Wildfly
The scripts are in the folder: resources/flyway.<p>
File naming: <b>Vx__script_name.sql</b>, where x is the version number. <p>
When there is a need to change the last script, it is necessary to create a new script with the changes.

Link to flyway documentation: [Documentation](https://flywaydb.org/documentation/) 

## API authentication
Authentication for the backend is handled via an API Key. This can be set in the configuration file.

## ArgoCD
The latest version on main is automatically picked up by ArgoCD and deployed to the DEV environment. See https://catenax-ng.github.io/.

https://dft-api.int.demo.catena-x.net/

To see how to deploy an application on 'Hotel Budapest': 
[How to deploy](https://catenax-ng.github.io/docs/guides/how-to-deploy-an-application)

### EDC
GitHub repository with correct version of the Eclipse DataSpace Connector Project: [repository](https://github.com/catenax-ng/product-edc)

### Licenses
Apache 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
