/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.catenax.dft.usecases.csvHandler.aspects;

import com.catenax.dft.entities.digitalTwins.AssetAdministrationShellDescriptor;
import com.catenax.dft.entities.digitalTwins.LocalIdentifier;
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.gateways.external.DigitalTwinGateway;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LookUpShellsUseCase extends AbstractCsvHandlerUseCase<Aspect, Aspect> {

    private final DigitalTwinGateway gateway;

    public LookUpShellsUseCase(DigitalTwinGateway gateway, StoreAspectCsvHandlerUseCase nextUseCase) {
        super(nextUseCase);
        this.gateway = gateway;
    }


    @Override
    @SneakyThrows
    protected Aspect executeUseCase(Aspect aspect) {
        List<LocalIdentifier> localIdentifiers = new ArrayList<>();
        localIdentifiers.add(LocalIdentifier.builder()
                .key(aspect.getLocalIdentifiersKey())
                .value(aspect.getLocalIdentifiersValue())
                .build()
        );

        List<String> shellIds = gateway.getDigitalTwins(localIdentifiers);

        if (shellIds.isEmpty()) {
            AssetAdministrationShellDescriptor aasDescriptor = AssetAdministrationShellDescriptor
                    .builder()
                    .build();
            gateway.createDigitalTwin(aasDescriptor);
        } else if (shellIds.size() == 1) {
            aspect.setUuid(shellIds.stream().findFirst().get());
        } else {
            throw new Exception(String.format("Multiple ids found on aspect %s - %s", aspect.getLocalIdentifiersKey(), aspect.getLocalIdentifiersValue()));
        }

        return aspect;
    }
}