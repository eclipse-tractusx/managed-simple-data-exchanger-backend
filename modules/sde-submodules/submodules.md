# SDE Submodules
---
## Description
This is maven package mainly build for supporting different semantic submodels like SerialPartTypization, Batch, AssemblyPartRelationship etc. 
Each Maven module handled dedicated Submodel and responsible for validation, parsing and transforming of the data.

Currently SDE supports below submodels.
 
### Supported Models

#### [serial-part-typization in Version 1.1.0]
#### [batch in Version 1.0.0]
#### [assembly-part-relationship in Version 1.1.0]

### How we can add Submodels?

To add new semantic submodel we have to follow below steps:

- Create new maven module project under the /modules/sde-submodules folder.
  
  product-dft-backend -> modules -> sde-submodules -> New_maven_module_project

- Add dependency of external services like EDC and DigitalTwins into the created submodule project pom.xml file.
- Provide the schema JSON of new submodel in location of your newly created module at /src/main/resources/submodel-name.json
- Create package structure org.eclipse.tractusx.sde.submodels.newsubmodel 
- Under created package create java class enxtends SubmodelExtension abstarct class. Provide the method of logic for load json schema file at the run time and override the method of abstarct class submodel.
- Create another class extends with SubmodelExecutor and override the all abstarct methods as per your need.
- Create packages for entity, model, mapper, repository, service and steps which we have to implement for the submodel process.

Once your maven module ready just do the clean build and install so submodel will automatically get register and you chan check  api /submodels where you can see the registered submodel entry. 




[serial-part-typization in Version 1.1.0]: https://github.com/catenax-ng/product-dft-backend/blob/modules-branch/modules/sde-submodules/serial-part-typization/README.md
[batch in Version 1.0.0]: https://github.com/catenax-ng/product-dft-backend/blob/modules-branch/modules/sde-submodules/batch/README.md
[assembly-part-relationship in Version 1.1.0]: https://github.com/catenax-ng/product-dft-backend/blob/modules-branch/modules/sde-submodules/assembly-part-relationship/README.md
