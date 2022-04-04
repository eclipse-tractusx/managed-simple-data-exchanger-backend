# our base build image
FROM 3.8.4-openjdk-17 as build

# copy the project files
COPY ./rest-api/pom.xml /pom.xml

# build all dependencies
RUN mvn dependency:go-offline -B

# copy your other files
COPY ./rest-api/src ./src

# build for release
RUN mvn package

# our final base image
FROM openjdk:17

# set deployment directory
WORKDIR /dft

# copy over the built artifact from the maven image
COPY --from=build target/*.jar ./app.jar

# set the startup command to run your binary
CMD ["java", "-jar", "./app.jar"]
