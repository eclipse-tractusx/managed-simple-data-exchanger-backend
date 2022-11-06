# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),

## [Unreleased]
- Uploaded submodel data update/delete.
- SDE Backend code Stabilization for easy to add new submodels. 

## [1.6.0] - 2022-10-31
### Added
- All API's secured with Keycloak token security to access API's.
- Supported new "Batch" submodel for tracability and create twins into DigitalTwins registry with EDC data offer.
- Enabled XSS protection for all API's.
- User can upload csv/json with usage and access policy restriction with duration, role, purpose and restricted perticuler BPM	while uploading data.
- Added changes to Enable sawgger API Documentation.
- Provided new API's to support consumer panel of SDE frontend.
- Added new api calls to access Portal service API's.
- Added new properties into application properties files.

### Changed
- Moved helm charts from helm/ to charts.
- Moved public api's uder /public uri.
- Public API's protected with existing security to access.
- Changes in Date validation for all submodel.
- Changes for improve the exception handling.

### Removed
- Removed existing api security to access API's(Provided Keycloak Security based Authentication).


### Fixed
- Fix for duplicate data upload handling. 
- Fix for Sonar issues.


[Unreleased]: https://github.com/catenax-ng/product-dft-backend/compare/dft-backend-1.6.0...main
[1.6.0]: https://github.com/catenax-ng/product-dft-backend/compare/dft-backend-1.5.0...dft-backend-1.6.0
[1.5.0]: https://github.com/catenax-ng/product-dft-backend/compare/dftbackend-1.3.0...dft-backend-1.5.0
[1.3.0]: https://github.com/catenax-ng/product-dft-backend/compare/dftbackend-1.2.0...dftbackend-1.3.0
[1.2.0]: https://github.com/catenax-ng/product-dft-backend/compare/dftbackend-1.1.1...dftbackend-1.2.0
