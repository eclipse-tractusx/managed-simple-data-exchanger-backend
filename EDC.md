# Running EDC locally

As of the time of the writing of this document, we are using the commit revision #`24956ec24d68fc04539f55c2b16d0e17bf197565`
from [here](https://github.com/eclipse-dataspaceconnector/DataSpaceConnector.git) to run the EDC basic connector locally.

In a shell from the project root run:

```shell
./gradlew :launchers:basic:shadowJar
java -jar launchers/basic/build/libs/dataspaceconnector-basic.jar
```



