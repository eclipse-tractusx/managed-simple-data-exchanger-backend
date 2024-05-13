#### Simple Data Exchanger (previously known as Data Format Transformer - DFT) 
---
## Description
It is a standalone service which can be self-hosted. It enables companies to provide their data in the Catena-X network via an EDC. Data is uploaded via CSV-files/Json/Manual Entry. The SDE registers the data in the Digital Twin Registry and makes it accessible via an EDC. The SDE project has currently three dependencies: Digital Twins, Portal and EDC.

### Modules

SDE backend splited into multiple services loosely-coupled Maven modules.

### product-dft-backend (Parent POM)

- modules/sde-external-services/edc
- modules/sde-external-services/digital-twins
- modules/sde-external-services/portal
- modules/sde-external-services/bpn-discovery
- modules/sde-external-services/policy-hub

- modules/sde-common
- modules/sde-core

- modules/sde-submodules/serial-part
- modules/sde-submodules/single-level-bom-as-built
- modules/sde-submodules/batch
- modules/sde-submodules/part-as-planned
- modules/sde-submodules/part-type-information
- modules/sde-submodules/part-site-information-as-planned
- modules/sde-submodules/single-level-usage-as-built
- modules/sde-submodules/single-level-bom-as-planned
- modules/sde-submodules/pcf
- modules/pcf-exchange