package org.eclipse.tractusx.sde.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ConfigurableFactoryProvider {
    @Autowired
    private List<ConfigurableFactory<?>> factories;

    @SuppressWarnings("unchecked")
    public <T> Optional<ConfigurableFactory<T>> getFactory(Class<T> tClass) {
        return factories.stream().filter(f -> tClass.isAssignableFrom(f.getCreatedClass())).map(o -> (ConfigurableFactory<T>)o).findFirst();
    }
}
