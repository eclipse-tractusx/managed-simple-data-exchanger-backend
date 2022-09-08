package com.catenax.dft.entities;

import com.catenax.dft.enums.DurationEnum;
import com.catenax.dft.enums.PolicyAccessEnum;
import com.catenax.dft.enums.UsagePolicyEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsagePolicyRequest implements Serializable {

    @JsonProperty(value = "type")
    UsagePolicyEnum type;
    @JsonProperty(value = "typeOfAccess")
    PolicyAccessEnum typeOfAccess;
    @JsonProperty(value = "value")
    String value;
    @JsonProperty(value = "durationUnit")
    DurationEnum durationUnit;
    @JsonProperty(value = "customName")
    String customName;
}
