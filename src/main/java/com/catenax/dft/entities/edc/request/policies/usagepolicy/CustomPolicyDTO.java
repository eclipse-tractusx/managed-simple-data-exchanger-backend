package com.catenax.dft.entities.edc.request.policies.usagepolicy;

import com.catenax.dft.entities.UsagePolicyRequest;
import com.catenax.dft.entities.edc.request.policies.ConstraintRequest;
import com.catenax.dft.exceptions.DftException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@SuperBuilder
public class CustomPolicyDTO extends UsagePolicyDTO{
    private static final String DATASPACECONNECTOR_LITERALEXPRESSION = "dataspaceconnector:literalexpression";

    public static CustomPolicyDTO fromUsagePolicy(UsagePolicyRequest usagePolicyRequest)
    {
        return CustomPolicyDTO.builder().type(usagePolicyRequest.getType()).typeOfAccess(usagePolicyRequest.getTypeOfAccess())
                .value(usagePolicyRequest.getValue()).build();

    }
    @Override
    public ConstraintRequest toConstraint() {
        throw new DftException("Constraint not supported for custom policy");
    }
}
