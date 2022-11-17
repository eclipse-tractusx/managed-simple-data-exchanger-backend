package org.eclipse.tractusx.sde.submodels.slbap.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class SingleLevelBoMAsPlannedConstants {
	
	@Value(value = "${manufacturerId}")
	public String manufacturerId;
	
	@Value(value = "${edc.hostname}")
	public String edcEndpoint;
	
	public static final String MANUFACTURER_PART_ID = "ManufacturerPartID";
	public static final String MANUFACTURER_ID = "ManufacturerID";
	public static final String HTTP = "HTTP";
	public static final String HTTPS = "HTTPS";
	public static final String ENDPOINT_PROTOCOL_VERSION = "0.0.1-SNAPSHOT";
	public static final String PREFIX = "urn:uuid:";
	public static final String DELETED_Y = "Y";

}
