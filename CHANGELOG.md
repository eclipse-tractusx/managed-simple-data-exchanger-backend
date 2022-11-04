# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),

## [Unreleased]

### Changed
- Moved helm charts from `helm/` to `charts`

## [1.0.4] - 2022-09-09
Added helm release, versioning & tagging

### Added
- Added Consumer panel for SDE application
- Added xss protection
- Added Swagger API docs enable
- Added new Batch submodule

### Changed
- Improve Exception handling
- Improvement audit tail for process tracking

### Known knowns
- csrf is disable shall be mitigated (low risk)
