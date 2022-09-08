package com.catenax.dft.entities.edc.request.policies.usagepolicy;

import com.catenax.dft.entities.UsagePolicyRequest;
import com.catenax.dft.entities.edc.request.policies.ConstraintRequest;
import com.catenax.dft.entities.edc.request.policies.Expression;
import com.catenax.dft.enums.PolicyAccessEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@SuperBuilder
public class RolePolicyDTO extends UsagePolicyDTO{
    private static final String DATASPACECONNECTOR_LITERALEXPRESSION = "dataspaceconnector:literalexpression";

    public static RolePolicyDTO fromUsagePolicy(UsagePolicyRequest usagePolicyRequest)
    {
    	return RolePolicyDTO.builder().type(usagePolicyRequest.getType()).typeOfAccess(usagePolicyRequest.getTypeOfAccess())
                .value(usagePolicyRequest.getValue()).build();

    }
    @Override
    public ConstraintRequest toConstraint() {
        if (getTypeOfAccess().equals(PolicyAccessEnum.RESTRICTED)) {
            Expression lExpression = Expression.builder()
                    .edcType(DATASPACECONNECTOR_LITERALEXPRESSION)
                    .value("idsc:ROLE")
                    .build();

            String operator = "EQ";
            Expression rExpression = null;
            rExpression = Expression.builder()
                    .edcType(DATASPACECONNECTOR_LITERALEXPRESSION)
                    .value(getValue())
                    .build();

            return ConstraintRequest.builder().edcType("AtomicConstraint")
                    .leftExpression(lExpression)
                    .rightExpression(rExpression)
                    .operator(operator)
                    .build();

        }
        return null;
    }
}
