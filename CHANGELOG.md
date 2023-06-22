# Changelog
All notable changes to this project will be documented in this file.

## [Unreleased]
- Support EDC version 4.1.
- Renamed SerialPartTypization aspect submodel to SerialPart.
- Renamed AssemblyPartRelationship aspect submodel to SingleLevelBoMAsBuilt.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
## [2.0.2] - 2023-06-21
### Added
- BPN Discovery service integration for DDTR.

### Fixed
- Upgradation of springboot(3.0.5-3.1.0) for security.

## [2.0.1] - 2023-05-23
- Add automatic identification of submodel for processing.

## [2.0.0] - 2023-05-05
- Removed token log statement from logs.
- EDC version 0.3.0 changes for multiple BPN.
- Error handling & input validation Messages for exceptions during upload / creation.
- Manufacturer country code list.
- Cancel contract agreement on provider side.
- BPN validation in SDE.
- The field "VAN" is still case sensitive, there is no need and it causes errors if you do not know it.
- Mix of CSV-formats: When you export the data from the contract panel you get a "comma"-separated file, when you download it from history it is separated by "semicolon". Files with "comma" are shown incorrect at least on German systems.
- User not able to copy values from the contract panel as the keys are longer as the displayed field.
- To find out which contract belongs to which dataset User have to download the history file.

### Fixed
-  Change in spring boot framework version.

## [1.9.1] - 2023-03-24

### Fixed
- Helm charts fixed with default values.
- Database dependency updated in charts.

## [1.9.0] - 2023-03-15
### Added
- Added new submodel SingleLevelUsageAsBuilt 1.0.1 aspect.
- Updated Batch submodel to support 1.0.2 version.
- Updated SerialPartTypization in Version 1.1.0 submodel support.
- Updated AssemblyPartRelationship in Version 1.1.1 submodel support.
- Added Look-Up process for AssemblyPartRelationship submodel.

### Fixed
- Removed manufactureId (BPN) from DT EDC URL creation.
- Correct submodel id for delete while assembly update.
- ENUM fix after changing variable of DT.
- Exception log for look up failure request.
- Look up feature for AssemblyPartRelationship: The Batch, we connected a Batch item with a Serial Part Typization Item
- Look up feature for AssemblyPartRelationship: The items (Serial Part Typization) had the fields VAN and Value filled was not work

### Changed
- Added/Updated Header copyrights for 2023.

## [1.8.1] - 2023-01-23
### Added
- Added use case selection.
- Enable filtering displayed submodels by selected use case.
- Added new API for transaction history download function for each processId.
- Added new API to Dynamic generation of CSV sample and CSV template per submodel.
- Added new API to support dynamic submodel help page.
- Added list contract agreements on Provider side

### Fixed
- Bug fixes.

## [1.8.0] - 2022-12-12
### Added
- Uploaded submodel data update/delete.
- SDE Backend code Stabilization for easy to add new submodels. 
- Added new submodel PartAsPlanned aspect.
- Added new submodel SingleLevelBoMAsPlanned aspect.
- Added new submodel PartSiteInformationAsPlanned aspect.
- Added new functionality update and Delete with exists aspect. 
- Added new api for user role permission access management.
- Enabled api level permissions.

### Fixed
- Bug fixes.

## [1.7.0] - 2022-11-07
### Added
- Enabled XSS protection for all API's.
- Added changes to Enable swagger API Documentation.

### Fixed
- Restrict Log-in for C-X users only to valid SDE instance for the correct organization.

## [1.6.0] - 2022-10-31
### Added
- All API's secured with Keycloak token security to access API's.
- Supported new "Batch" submodel for traceability and create twins into DigitalTwins registry with EDC data offer.
- User can upload csv/json with usage and access policy restriction with duration, role, purpose and restricted particular BPM	while uploading data.
- Provided new API's to support consumer panel of SDE frontend.
- Added new api calls to access Portal service API's.
- Added new properties into application properties files.

### Changed
- Moved public api's under /public uri.
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
- Created user guideline and installation documentation.
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
- Integrated EDC connector to create assets, policies as a data offer.

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

[unreleased]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/dftbackend-2.0.2...main
[2.0.2]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/dftbackend-2.0.1...dftbackend-2.0.2
[2.0.1]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/dftbackend-2.0.0...dftbackend-2.0.1
[2.0.0]: https://github.com/eclipse-tractusx/dft-backend/compare/1.9.1...2.0.0
[1.9.1]: https://github.com/eclipse-tractusx/dft-backend/compare/1.9.0...1.9.1
[1.9.0]: https://github.com/eclipse-tractusx/dft-backend/compare/dft-backend-1.8.1...1.9.0
[1.8.1]: https://github.com/eclipse-tractusx/dft-backend/compare/dft-backend-1.8.0...dft-backend-1.8.1
[1.8.0]: https://github.com/eclipse-tractusx/dft-backend/compare/dft-backend-1.7.0...dft-backend-1.8.0
[1.7.0]: https://github.com/eclipse-tractusx/dft-backend/releases/tag/dft-backend-1.7.0

