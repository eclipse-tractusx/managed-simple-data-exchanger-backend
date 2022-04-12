# our base build image
FROM maven:3.8.5-openjdk-18-slim as build

# copy the project files
COPY ./pom.xml /pom.xml

# build all dependencies
RUN mvn dependency:go-offline -B

# copy your other files
COPY ./src ./src

# build for release
RUN mvn package

# our final base image
FROM openjdk:18-alpine

# set deployment directory
WORKDIR /dft

# copy over the built artifact from the maven image
COPY --from=build target/*.jar ./app.jar

# set the startup command to run your binary
CMD ["java", "-jar", "./app.jar"]
