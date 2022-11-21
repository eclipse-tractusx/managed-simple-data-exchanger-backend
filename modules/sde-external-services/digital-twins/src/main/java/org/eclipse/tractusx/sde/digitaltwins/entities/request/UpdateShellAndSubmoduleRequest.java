package org.eclipse.tractusx.sde.digitaltwins.entities.request;

import java.util.List;

import org.eclipse.tractusx.sde.digitaltwins.entities.common.Description;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.GlobalAssetId;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.KeyValuePair;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateShellAndSubmoduleRequest {
	
    private String idShort;
    private String identification;
    private List<Description> description;
    private GlobalAssetId globalAssetId;
    private List<KeyValuePair> specificAssetIds;
    private List<CreateSubModelRequest> submodelDescriptors;
    
	 @SneakyThrows
	    public String toJsonString() {
	        final ObjectMapper mapper = new ObjectMapper();
	        return mapper.writeValueAsString(this);
	    }
}
