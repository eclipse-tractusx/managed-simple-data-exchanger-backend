package org.eclipse.tractusx.sde.core.controller;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.core.service.SubmodelCsvService;
import org.eclipse.tractusx.sde.core.utils.CsvUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@ControllerAdvice
@RequiredArgsConstructor
public class SubmodelCsvController {

	private final SubmodelCsvService submodelCsvService;

	private final CsvUtil csvUtil;

	@SneakyThrows
	@GetMapping(value = "/submodels/csvfile/{submodelName}")
	public void getSubmodelCSV(@PathVariable String submodelName, @RequestParam("type") String type,
			HttpServletResponse response) {

		String filename = submodelName + CommonConstants.CSV_FILE_EXTENSION;
		csvUtil.generateCSV(response, filename, submodelCsvService.findSubmodelCsv(submodelName, type));
	}

	@GetMapping(value = "/{submodel}/download/{processId}/csv")
	//@PreAuthorize("hasPermission('','provider_download_own_data')")
	public void getDownloadFileByProcessId(@PathVariable("processId") String processId,
			@PathVariable("submodel") String submodel, HttpServletResponse response) {

		String filename = submodel + "_" + processId + CommonConstants.CSV_FILE_EXTENSION;
		csvUtil.generateCSV(response, filename, submodelCsvService.findByProcessedIdSubmodelCsv(submodel, processId));
	}

}
