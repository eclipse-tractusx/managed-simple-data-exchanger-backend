# Helm Charts for DFT Backend

This chart bootstraps a DFT deployment on a Kubernetes cluster using the Helm package manager.


## Dependency Charts

This helm chart is an umbrella chart that pulls together engine specific charts. The engine charts are included as dependencies in Chart.yaml.
We have added PostgresSQL bitnami image as a Dependency.



## Repository Structure

This GitHub repository contains the source for the packaged and versioned charts released using GitHub pages (the Chart Repository).

The Charts in the charts/ directory in the master branch of this repository match the latest packaged Chart in the Chart Repository. 

## Helm Release
 
Provides simple semantic versioning based from previous git tags. You can run the chart-release-fe.yml workflow to create new release. 

## Helm Chart Templates

The templates require your application to built into a Docker image. The Docker Templates project provides assistance in creating an image for your application.

This project provides the following files:

| File                                               | Description                                                             |
|--------------------------------------------------- |-----------------------------------------------------------------------  |  
| `/charts/dftbackend/Chart.yaml`                    | The definition file for your application                                | 
| `/charts/dftbackend/values.yaml`                   | Configurable values that are inserted into the following template files |   
| `/charts/dftbackend/values-int.yaml`               | Configurable values for int env                                         | 
| `/charts/dftbackend/templates/deployment.yaml`     | Template to configure your application deployment.                      |
| `/charts/dftbackend/templates/ingress.yaml`        | Template to configure your application deployment.                      | 
| `/charts/dftbackend/templates/service.yaml`        | Template to configure your application deployment.                      | 
| `/charts/dftbackend/templates/hpa.yaml`            | Template to configure your application deployment.                      | 
| `/charts/dftbackend/templates/NOTES.txt`           | Helper to enable locating your application IP and PORT                  | 

## Helm Commands
$ helm repo add eclipse-tractusx-dft-backend https://github.com/eclipse-tractusx/dft-backend/tree/main/charts

$ helm install my-release eclipse-tractusx/dftbackend --version 2.0.0

