# our base build image
FROM maven:3.9.0-eclipse-temurin-19 as build

# copy the project files
COPY ../pom.xml ./pom.xml

# copy your other files
COPY ../modules ./modules

# build for release
RUN mvn clean install 

# our final base image

FROM eclipse-temurin:19-jdk-jammy

ARG USERNAME=dftuser
ARG USER_UID=1000
ARG USER_GID=$USER_UID

# Create the user
RUN apt-get update && apt-get install --no-install-recommends \
    && groupadd --gid $USER_GID $USERNAME \
    && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME \
    && apt-get update \
    && apt-get install -y sudo \
    && echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME \
    && chmod 0440 /etc/sudoers.d/$USERNAME \
    && rm -rf /var/lib/apt/lists/*

USER $USERNAME

# set deployment directory
WORKDIR /home/user/dftuser/dftapp

# copy over the built artifact from the maven image

COPY --from=build modules/sde-core/target/*.jar ./app.jar

EXPOSE 8080
# set the startup command to run your binary

CMD ["java", "-jar", "./app.jar","--spring.config.location=file:/app/configuration.properties"]

