#################################################################################
# Copyright (c) 2022,2024 T-Systems International GmbH
# Copyright (c) 2022,2024 Contributors to the Eclipse Foundation
#
# See the NOTICE file(s) distributed with this work for additional
# information regarding copyright ownership.
#
# This program and the accompanying materials are made available under the
# terms of the Apache License, Version 2.0 which is available at
# https://www.apache.org/licenses/LICENSE-2.0.
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.
#
# SPDX-License-Identifier: Apache-2.0
################################################################################
# our base build image
FROM maven:3.9.5-eclipse-temurin-17 AS build

# copy the project files
COPY ../pom.xml ./pom.xml

# copy your other files
COPY ../modules ./modules

# build for release
RUN mvn clean install 

# our final base image

FROM eclipse-temurin:17.0.10_7-jdk-focal

ARG USERNAME=sdeuser
ARG USER_UID=1001
ARG USER_GID=$USER_UID

# Create the user

RUN useradd -m -d /home/sdeuser -s /bin/bash -u $USER_UID -U $USERNAME 
USER $USERNAME

# set deployment directory
WORKDIR /home/sdeuser/sdeapp
COPY LICENSE NOTICE.md DEPENDENCIES SECURITY.md  ./

# copy over the built artifact from the maven image
COPY --from=build modules/sde-core/target/*.jar ./app.jar
EXPOSE 8080

# set the startup command to run your binary
CMD ["java", "-jar", "./app.jar","--spring.config.location=file:/app/configuration.properties"]