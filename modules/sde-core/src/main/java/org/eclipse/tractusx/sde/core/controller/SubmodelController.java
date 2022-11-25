package org.eclipse.tractusx.sde.core.controller;

import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.core.service.SubmodelService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@ControllerAdvice
@RequiredArgsConstructor
public class SubmodelController {

	private final SubmodelService submodelService;

	@GetMapping("/submodels")
	public List<Map<String, String>> getAllSubmodels() {
		return submodelService.findAllSubmodels();
	}

	@GetMapping("/submodels/{submodelName}")
	public Map<Object, Object> getSubmodelByName(@PathVariable String submodelName) {
		return submodelService.findSubmodelByName(submodelName);
	}

}
