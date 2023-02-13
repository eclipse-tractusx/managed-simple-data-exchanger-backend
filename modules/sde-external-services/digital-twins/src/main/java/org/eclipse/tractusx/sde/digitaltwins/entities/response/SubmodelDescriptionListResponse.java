package org.eclipse.tractusx.sde.digitaltwins.entities.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmodelDescriptionListResponse {
	
	private List<ShellDescriptorResponse> items;

}
