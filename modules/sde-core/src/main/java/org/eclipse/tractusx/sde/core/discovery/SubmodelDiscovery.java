package org.eclipse.tractusx.sde.core.discovery;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.eclipse.tractusx.sde.common.extensions.SubmodelExtension;
import org.eclipse.tractusx.sde.core.registry.SubmodelRegistration;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SubmodelDiscovery {

	private final SubmodelRegistration submoduleRegistration;

	@Autowired
	private ListableBeanFactory beanFactory;

	@PostConstruct
	private void submodels() {
		Collection<SubmodelExtension> interfaces = beanFactory.getBeansOfType(SubmodelExtension.class).values();
		interfaces.forEach(subomdelService -> submoduleRegistration.register(subomdelService));
	}

}
