package com.catenax.sde.core.discovery;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catenax.sde.common.extensions.SubmodelExtension;
import com.catenax.sde.core.registry.SubmodelRegistration;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SubmodelDiscovery {

	private final SubmodelRegistration submoduleRegistration;

	@Autowired
	private ListableBeanFactory beanFactory;

	@PostConstruct
	private void Submodels() {
		Collection<SubmodelExtension> interfaces = beanFactory.getBeansOfType(SubmodelExtension.class).values();
		interfaces.forEach(subomdelService -> {
			submoduleRegistration.register(subomdelService);
		});
	}

}
