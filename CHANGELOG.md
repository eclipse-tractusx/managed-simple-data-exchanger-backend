# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),

## [Unreleased]

### Added
- Updated readme.md and open api yml file.
- Added controller interface api's for Policy management.
- External EDC service interface api updated.
- Updated supported sub-model implementation classes.
- EDC asset update refactored in supported submodels.
- Support for pcf v6.0.0 submodel.
- Added controller interface api's for PCF Exchange.
- Added new classes for multiple submodel version support.
- Added refactor code changes for external services.
- Added different usecase Handler for multi version support.
- Added usecase handle implementation for multiple submodel support.
- Supported new serial part submodel with multi version support. 
- Supported new single level bom as built submodel with multi version support.
- Removed maven modules  serial-part-typization and assembly-part-relationship for update. 
- New maven module for part type information submodel support.
- Refactored/Updated batch maven module to support multi version.
- Refactored/Updated PartAsPlanned and SingleLevelBoMAsPlanned maven module to support multi version.
- Refactored/Updated PartSiteInformationAsPlanned maven module to support multi version.
- Refactored/Updated SingleLevelUsageAsBuilt maven module to support multi version.
- Refactored/Updated PCF maven module to support multi version.
- Added new flyway files.
- Supporting new submodel Singlelevelbomasplanned.
- Support EDC 7.
- Added new files for digital twin access rule support.
- Refactor code for pcf, dt access API, EDC 7. 
- Dt access api use in digital twin processing.  
- Added test cases for PCF and policy controller.
- Refactor code to make stable release

### Fixed
- Remove garbage character from 'edc_request_template' path. Fixed [#147](https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/issues/147).
- Dependabot bump version fix in pom.xml and DEPENDENCIES file update.
- Dockerfile image update. [#117](https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/pull/117)
- Dependabot bump version fixes for 24/05

## [2.3.7] - 2024-05-09 
### Fixed
- Rename edc_request_template directory.
- This is fixed for issue #147.

## [2.3.6] - 2024-03-06
### Fixed
- open api fix in sde-open-api.yml.
- Fixed Postgres vulnerability CVE-2024-1597.
- Fixed spring security Vulnerability CVE-2024-22234.

## [2.3.5] - 2024-02-20

### Added
- Policy-Hub service api integration.
- Added New Policy Entities and pojo classes.
- Draft code of Policies changes.
- Added new classes for file download history.
- Added new classes for files for cache ddtr and bpndiscovery twin search.
- Added DT asset provider configuration on start up.
- Use common DTO in all submodels.
- Use policy hub model in all submodels.
- Add controller interface for download data.
- Add test cases for consumer interface.
- update open api docs.
 
### Fixed
- Fix new dDTR support changes.
- Fixed Vulnerability logback issue of CVE-2023-6481.
- Docker image updated to fix vulnerability.

## [2.3.4] - 2023-12-21
### Fixed
- Removed time duration from policy.

## [2.3.3] - 2023-12-06
### Fixed
- fixed veracode security in app CVE-2023-46589 and CVE-2023-34053 .
- build base image version.

## [2.3.2] - 2023-12-01
### Fixed
- fixed veracode security CVE-2023-6378(logback-classic Denial Of Service)

## [2.3.1] - 2023-11-29
### Fixed
- docker cmd updated,removed r from run command.
- fixed veracode security CVE-2023-33202(Bouncy Castle Denial of Service). 
- Updated assembly-part-relationship csv and .md file.

## [2.3.0] - 2023-11-29
### Added
- DT use refactor for look up twin.
 
## [2.2.2] - non-released
### Added
- Added oauth security for sde public api.
- BPN url add API path.

### Fixed
- Correct dataplane endpoint for digital twin.
- DSP endpoint path for digital-twin edc url.
- Trivy workflow update. 

## [2.2.1] - non-released
### Fixed
- Update PCF schema fields for SDE.

## [2.2.0] - 2023-09-20
### Added
- Pcf model schema and model registration.
- Add entity mapping and model for pcf model execution.
- Support pcf submodel in sde.

## [2.1.1] - 2023-09-06
- bumped version

## [2.1.0] - 2023-08-30
### Fixed
- Added external subject in specific asset Ids.
- Update AAS submodel endpoints subprotocolBody.
- Supported Batch submodel version 2.0.0.
- DDTR update with latest version.
- Feign Client changes updated for child aspect relationship.

## [2.0.11] - 2023-08-29
### Changed 
- Docker image name changed.

## [2.0.10] - 2023-08-23
### Fixed
- Updated openAPI file for kics issue.
- Documentation updated.

## [2.0.9] - 2023-08-08
### Fixed
- Access policy for BPN check should be or not and constraint.
- Filter contract as Provider and Consumer.
- Fix for look up functionality, dtURL use from cache.
- fix for Offer subscription issue.

## [2.0.8] - 2023-08-07
### Added
- Support EDC version 0.5.1
- Integration with BPDM api interface.
- add Traceability use case as access policy.
- refactor token utilty api.
- update varible name for uniqueness.
- refactor dt look up api for new version.
- Adapt DTR API new changes.
- Support AAS version 0.3
- Renamed SerialPartTypization aspect submodel to SerialPart.
- Renamed AssemblyPartRelationship aspect submodel to SingleLevelBoMAsBuilt.
- Support EDC version 4.1
- added umbrella charts for sde frontend and backend.
- removed the older charts.

### Fixed
- Sonar lint issues fixed.
- Spring security trivy issue fixed.
- refactor EDC and de-centralized digital-twins api calls.
- BPN discovery update.
- remove unwanted maven module.
- fix to disable traceability use case policy.

## [2.0.7] - non-released
### Added
- Support AAS version 0.3

## [2.0.6] - non-released
### Added
- Renamed SerialPartTypization aspect submodel to SerialPart.
- Renamed AssemblyPartRelationship aspect submodel to SingleLevelBoMAsBuilt. 

## [2.0.5] - non-released
### Added
- Support EDC version 4.1.

## [2.0.4] -non-released
### Fixed
- refactor EDC and de-centralized digital-twins api calls.

## [2.0.3] - non-released
### Added
- added umbrella charts for sde frontend and backend.
- removed the older charts.

### Fixed
- BPN discovery update.
- remove unwanted maven module.

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

[unreleased]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/v2.3.6...main
[2.3.6]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/v2.3.5...v2.3.6
[2.3.5]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/v2.3.4...v2.3.5
[2.3.4]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/v2.3.3...v2.3.4
[2.3.3]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/v2.3.2...v2.3.3
[2.3.2]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/v2.3.1...v2.3.2
[2.3.1]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/v2.3.0...v2.3.1
[2.3.0]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/v2.2.0...v2.3.0
[2.2.0]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/v2.1.1...v2.2.0
[2.1.1]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/v2.1.0...v2.1.1
[2.1.0]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/v2.0.11...v2.1.0
[2.0.11]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/v2.0.10...v2.0.11
[2.0.10]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/sdebackend-2.0.9...v2.0.10
[2.0.9]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/sdebackend-2.0.8...sdebackend-2.0.9
[2.0.8]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/dftbackend-2.0.2...dftbackend-2.0.8
[2.0.2]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/dftbackend-2.0.1...dftbackend-2.0.2
[2.0.1]: https://github.com/eclipse-tractusx/managed-simple-data-exchanger-backend/compare/dftbackend-2.0.0...dftbackend-2.0.1
[2.0.0]: https://github.com/eclipse-tractusx/dft-backend/compare/1.9.1...2.0.0
[1.9.1]: https://github.com/eclipse-tractusx/dft-backend/compare/1.9.0...1.9.1
[1.9.0]: https://github.com/eclipse-tractusx/dft-backend/compare/dft-backend-1.8.1...1.9.0
[1.8.1]: https://github.com/eclipse-tractusx/dft-backend/compare/dft-backend-1.8.0...dft-backend-1.8.1
[1.8.0]: https://github.com/eclipse-tractusx/dft-backend/compare/dft-backend-1.7.0...dft-backend-1.8.0
[1.7.0]: https://github.com/eclipse-tractusx/dft-backend/releases/tag/dft-backend-1.7.0

