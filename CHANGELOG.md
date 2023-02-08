# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),

## [Unreleased]
- Dynamic generation of CSV (Sample,template) file for implemented submodule. 
- New api for transaction history download function for each process id.
- Create api to support dynamic submodule help page.

## [1.8.0] - 2022-12-12
### Added
- Uploaded submodel data update/delete.
- SDE Backend code Stabilization for easy to add new submodels. 
- Added new submodel PartAsPlanned aspect.
- Added new submodel SingleLevelBoMAsPlanned aspect.
- Added new submodel PartSiteInformationAsPlanned aspect.
- Added new functionality update and Delete with exists aspect. 
- Added new api for user role permission access managament.
- Enabled api level permissions.

### Fixed
- Bug fixes.

## [1.7.0] - 2022-11-07
### Added
- Enabled XSS protection for all API's.
- Added changes to Enable sawgger API Documentation.

### Fixed
- Restrict Log-in for C-X users only to valid SDE instance for the correct organization.

## [1.6.0] - 2022-10-31
### Added
- All API's secured with Keycloak token security to access API's.
- Supported new "Batch" submodel for tracability and create twins into DigitalTwins registry with EDC data offer.
- User can upload csv/json with usage and access policy restriction with duration, role, purpose and restricted perticuler BPM	while uploading data.
- Provided new API's to support consumer panel of SDE frontend.
- Added new api calls to access Portal service API's.
- Added new properties into application properties files.

### Changed
- Moved public api's uder /public uri.
- Public API's protected with existing security to access.
- Changes in Date validation for all submodel.
- Changes for improve the exception handling.

### Removed
- Removed existing api security to access API's(Provided Keycloak Security based Authentication).

### Fixed
- Fix for duplicate data upload handling. 
- Fix for Sonar issues.

## [1.5.0] - 2022-10-11
### Added
- Created user guidliance and installation documentation 
- Created umbrella helm charts.
- Integrated trivy, kicks. 

### Changed 
- Updated helm charts secrets. 
- Updated jar versions. 

## [1.3.0] - 2022-09-21
### Fixed
- Fix for edc api call.

### Changed 
- keycloack properties file changed.

## [1.2.0] - 2022-08-29
### Added
- Integrated EDC connector to create assest, policies as a data offer.

### Changed 
- Moved helm charts from helm/ to charts.
- Created workflows. 
- Updated digital twin url in vault. 

## [1.1.0] - 2022-08-24
### Added
- Data is uploaded via two CSV-files and Parsing of CSV file for Parts and Relationships
- The DFT registers the data in the Digital Twin Registry and makes it accessible via an EDC
- Compliance with Catena-X Guidelines
- Integration with Digital Twin registry service.


[Unreleased]: https://github.com/catenax-ng/tx-dft-backend/compare/dft-backend-1.8.0...main
[1.8.0]: https://github.com/catenax-ng/tx-dft-backend/compare/dft-backend-1.7.0...dft-backend-1.8.0
[1.7.0]: https://github.com/catenax-ng/product-dft-backend/compare/dft-backend-1.6.0...dft-backend-1.7.0
[1.6.0]: https://github.com/catenax-ng/product-dft-backend/compare/dft-backend-1.5.0...dft-backend-1.6.0
[1.5.0]: https://github.com/catenax-ng/product-dft-backend/compare/dftbackend-1.3.0...dft-backend-1.5.0
[1.3.0]: https://github.com/catenax-ng/product-dft-backend/compare/dftbackend-1.2.0...dftbackend-1.3.0
[1.2.0]: https://github.com/catenax-ng/product-dft-backend/compare/dftbackend-1.1.0...dftbackend-1.2.0
