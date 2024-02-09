/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.sde.common.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalDefaultExceptionHandler extends ResponseEntityExceptionHandler {

	public static final String DEFAULT_ERROR_VIEW = "error";

	@ExceptionHandler(NoDataFoundException.class)
	public ResponseEntity<Map<String, String>> handleNodataFoundException(NoDataFoundException ex, WebRequest request) {
		log.error("NoDataFoundException " + ex.getMessage());
		Map<String, String> errorResponse = prepareErrorResponse(ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handlePSQLException(Exception ex, WebRequest request) {
		log.error("Internal server error " + ex.getMessage());
		Map<String, String> errorResponse = prepareErrorResponse("Internal Server error");
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<Map<String, String>> handleValidationException(ValidationException ex, WebRequest request) {
		log.error("ValidationException " + ex.getMessage());
		Map<String, String> errorResponse = prepareErrorResponse(ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(FeignException.class)
	public ResponseEntity<Map<String, String>> handleFeignException(FeignException ex, WebRequest request) {
		log.error("FeignException: " + ex.getMessage());
		log.error("FeignException RequestBody: " + ex.request());
		log.error("FeignException ResponseBody: " + ex.contentUTF8());
		ObjectMapper objmap = new ObjectMapper();
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("msg", "Error in remote service execution");
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = objmap.readValue(ex.contentUTF8(), Map.class);
			Object object = map.get("errors");
			if (object != null)
				errorResponse = prepareErrorResponse(object.toString());
		} catch (JsonMappingException e) {
			log.error("FeignException JsonMappingException " + e.getMessage());
		} catch (JsonProcessingException e) {
			log.error("FeignException JsonProcessingException " + e.getMessage());
		}

		return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.status()));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, String>> handleConstraintValidationException(ConstraintViolationException ex,
			WebRequest request) {
		log.error("ConstraintViolationException " + ex.getMessage());
		Map<String, String> errorResponse = prepareErrorResponse(ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		log.error("MethodArgumentNotValidException " + errors);
		Map<String, String> errorResponse = prepareErrorResponse(errors.toString());
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		log.error("HandleHttpMessageNotReadable " + ex.getMessage());
		Map<String, String> errorResponse = prepareErrorResponse(ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public final ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex,
			WebRequest request) {
		String error = "You don't have access to this page or the page doesn't exist. Please contact your admin";
		log.error(error);
		Map<String, String> errorResponse = prepareErrorResponse(error);
		return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
	}

	private Map<String, String> prepareErrorResponse(String errormsg) {
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("msg", errormsg);
		return errorResponse;
	}
}
