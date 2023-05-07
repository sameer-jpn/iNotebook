package com.bajaj.markets.insurance.business.controller;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.core.instrument.util.StringUtils;

import com.bajaj.bfsd.common.BFLLoggerComponent;
import com.bajaj.bfsd.common.BFLLoggerUtilExt;
import com.bajaj.bfsd.security.beans.CustomDefaultHeaders;
import com.bajaj.markets.authentication.principal.Role;
import com.bajaj.markets.insurance.business.beans.BusinessException;
import com.bajaj.markets.insurance.business.beans.ErrorBean;
import com.bajaj.markets.insurance.business.beans.InputBean;
import com.bajaj.markets.insurance.business.beans.ResponseBean;
import com.bajaj.markets.insurance.business.helper.BusinessConstants;
import com.bajaj.markets.insurance.business.service.CreateApplicationBusinessService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class CreateApplicationBusinessController {

	@RestController
	private CreateApplicationBusinessService createAppInsuranceBusinessService;

	@Autowired
	private BFLLoggerUtilExt logger;

	@Autowired
	private Environment env;

	@Autowired
	private CustomDefaultHeaders customDefaultHeaders;
	
	@Value("${spring.session.enabled:false}")
	private boolean sessionEnabled;

	private static final String CLASS_NAME = CreateApplicationBusinessController.class.getSimpleName();

	@ApiOperation(value = "create Application and Start workflow", notes = "create Application and Start workflow", httpMethod = "POST", response = ResponseBean.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authtoken", required = true, dataType = "string", paramType = "header"),
			@ApiImplicitParam(name = "guardtoken", required = false, dataType = "string", paramType = "header") })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully Created the Application and started the Workflow or resumed the application", response = ResponseBean.class),
			@ApiResponse(code = 400, message = "Bad input parameter", response = ErrorBean.class),
			@ApiResponse(code = 500, message = "Some Internal Eror Occured", response = ErrorBean.class),
			@ApiResponse(code = 401, message = "Some Internal Eror Occured", response = ErrorBean.class),
			@ApiResponse(code = 403, message = "Some Internal Eror Occured", response = ErrorBean.class) })
	@CrossOrigin
	@PostMapping(path = "${api.insurance.business.application.create.POST.uri}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured({ Role.SYSTEM, Role.PSEUDO_CUSTOMER, Role.EMPLOYEE, Role.PSEUDO_VERIFIED_CUSTOMER, Role.PSEUDO_VERIFIED_CUSTOMER, Role.CUSTOMER })
	public ResponseEntity<ResponseBean> createApplication(@RequestBody InputBean inputRequest,
			HttpServletRequest request,
			@RequestParam(value = "offer", required = false) String offerId) {

		logger.debug(CLASS_NAME, BFLLoggerComponent.CONTROLLER,
				"Inside createApplication with Request : " + inputRequest);

		if (Objects.isNull(inputRequest.getPayload())) {
			throw new BusinessException("OMIB-0002", env.getProperty("OMIB-0002"), HttpStatus.BAD_REQUEST);
		}
		

		 if(!StringUtils.isEmpty(offerId)) {
	            logger.debug(CLASS_NAME, BFLLoggerComponent.CONTROLLER,
	                    "Inside createApplication with offerId : " + offerId);
	        }
	    ResponseBean response = createAppInsuranceBusinessService.createApplication(inputRequest, request, offerId);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, CustomDefaultHeaders.AUTH_TOKEN);
		if (sessionEnabled) {
			httpHeaders.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, BusinessConstants.SESSION_TOKEN);
		}
		httpHeaders.add(CustomDefaultHeaders.AUTH_TOKEN, customDefaultHeaders.getAuthtoken());
		logger.debug(CLASS_NAME, BFLLoggerComponent.CONTROLLER, "Exit from createApplication Response : " + response);
		return new ResponseEntity<>(response, httpHeaders, HttpStatus.CREATED);
	}

	@ApiOperation(value = "Get application details", notes = "Get application details", httpMethod = "GET", response = Object.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "processId", required = true, dataType = "string", paramType = "header"),
			@ApiImplicitParam(name = "authtoken", required = true, dataType = "string", paramType = "header"),
			@ApiImplicitParam(name = "guardtoken", required = false, dataType = "string", paramType = "header") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully Created the Application and started the Workflow or resumed the application", response = Object.class),
			@ApiResponse(code = 400, message = "Bad input parameter", response = ErrorBean.class),
			@ApiResponse(code = 500, message = "Some Internal Eror Occured", response = ErrorBean.class),
			@ApiResponse(code = 401, message = "Some Internal Eror Occured", response = ErrorBean.class),
			@ApiResponse(code = 403, message = "Some Internal Eror Occured", response = ErrorBean.class) })
	@CrossOrigin
	@GetMapping(path = "${api.insurance.business.application.create.GET.uri}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured({ Role.PSEUDO_CUSTOMER, Role.EMPLOYEE, Role.PSEUDO_VERIFIED_CUSTOMER, Role.CUSTOMER })
	public ResponseEntity<Object> getApplicationDetails(HttpServletRequest request) {

		logger.debug(CLASS_NAME, BFLLoggerComponent.CONTROLLER, "Inside getApplicationDetails");

		String processId = request.getHeader(BusinessConstants.PROCESS_ID);
		if (Objects.isNull(processId)) {
			throw new BusinessException("OMIB-0003", env.getProperty("OMIB-0003"), HttpStatus.BAD_REQUEST);
		}
		ResponseBean response = createAppInsuranceBusinessService.getApplicationData(processId);
		logger.debug(CLASS_NAME, BFLLoggerComponent.CONTROLLER,
				"Exit from getApplicationDetails Response : " + response);
		return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
	}

}