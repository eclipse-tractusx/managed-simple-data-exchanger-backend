package com.catenax.dft.entities.edc.request.policies;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Expression {
	
	@JsonProperty("edctype")
    private String edcType;
    
    @JsonProperty("value")
    private Object value;

}
