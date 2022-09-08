package com.catenax.dft.entities.edc.request.policies.usagepolicy;

import com.catenax.dft.entities.edc.request.policies.ConstraintRequest;
import com.catenax.dft.enums.PolicyAccessEnum;
import com.catenax.dft.enums.UsagePolicyEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class UsagePolicyDTO {

    private UsagePolicyEnum type;
    private PolicyAccessEnum typeOfAccess;
    private String value;

    public abstract ConstraintRequest toConstraint();

}
