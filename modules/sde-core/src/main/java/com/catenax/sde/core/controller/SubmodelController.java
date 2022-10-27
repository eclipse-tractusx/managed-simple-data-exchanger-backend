package com.catenax.sde.core.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.catenax.sde.core.service.SubmodelService;

import lombok.RequiredArgsConstructor;

@RestController
@ControllerAdvice
@RequiredArgsConstructor
public class SubmodelController {

	private final SubmodelService submodelService;

	@GetMapping("/submodels")
	public Set<String> getAllSubmodels() {
		return submodelService.findAllSubmodels();
	}

	@GetMapping("/submodels/{submodelName}")
	public Map<String, Object> getSubmodelByName(@PathVariable String submodelName) {
		return submodelService.findSubmodelByName(submodelName).toMap();
	}

}
