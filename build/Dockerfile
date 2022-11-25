# our base build image
FROM maven:3.8.5-openjdk-18-slim as build

# copy the project files
COPY ../pom.xml ./pom.xml

# build all dependencies
#RUN mvn dependency:go-offline -B

# copy your other files
COPY ../modules ./modules

# build for release
RUN mvn clean install 

# our final base image
#FROM eclipse-temurin:18.0.1_10-jre
FROM eclipse-temurin:19.0.1_10-jre

ARG USERNAME=dftuser
ARG USER_UID=1000
ARG USER_GID=$USER_UID

# Create the user
RUN groupadd --gid $USER_GID $USERNAME \
    && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME \
    && apt-get update \
    && apt-get install -y sudo \
    && echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME \
    && chmod 0440 /etc/sudoers.d/$USERNAME

USER $USERNAME

ARG UID=7000
ARG GID=7000

# set deployment directory
WORKDIR /dft

# copy over the built artifact from the maven image

COPY  --from=build modules/sde-core/target/*.jar ./app.jar

#RUN chown ${UID}:${GID} /dft

USER ${UID}:${GID}

EXPOSE 8080
# set the startup command to run your binary
CMD ["java", "-jar", "./app.jar"]

#CMD ["java", "-jar", "./app.jar","--spring.config.location=file:/app/configuration.properties"]

