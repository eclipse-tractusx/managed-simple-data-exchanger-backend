package com.catenax.dft.entities.edc.request.policies.usagepolicy;

import com.catenax.dft.entities.UsagePolicyRequest;
import com.catenax.dft.entities.edc.request.policies.ConstraintRequest;
import com.catenax.dft.entities.edc.request.policies.Expression;
import com.catenax.dft.enums.DurationEnum;
import com.catenax.dft.enums.PolicyAccessEnum;
import com.catenax.dft.enums.UsagePolicyEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DurationPolicyDTO extends UsagePolicyDTO{
    private static final String DATASPACECONNECTOR_LITERALEXPRESSION = "dataspaceconnector:literalexpression";
    private DurationEnum durationUnit;

    public static DurationPolicyDTO fromUsagePolicy(UsagePolicyRequest usagePolicyRequest)
    {
        return DurationPolicyDTO.builder().type(usagePolicyRequest.getType()).typeOfAccess(usagePolicyRequest.getTypeOfAccess())
                .value(usagePolicyRequest.getValue()).durationUnit(usagePolicyRequest.getDurationUnit()).build();

    }
    @Override
    public ConstraintRequest toConstraint() {
        if (getTypeOfAccess().equals(PolicyAccessEnum.RESTRICTED)) {
            Expression lExpression = Expression.builder()
                    .edcType(DATASPACECONNECTOR_LITERALEXPRESSION)
                    .value("idsc:ELAPSED_TIME")
                    .build();

            String operator = "LEQ";
            Expression rExpression = null;
            String value = "P";
            switch (this.durationUnit) {
                case YEAR:
                    value = value + getValue()+ "Y0M0DT0H0M0S";
                    break;
                case MONTH:
                    value = value + "0Y" + getValue() + "M0DT0H0M0S";
                    break;
                case DAY:
                    value = value + "0Y0M" + getValue() + "DT0H0M0S";
                    break;
                case HOUR:
                    value = value + "0Y0M0DT" + getValue() + "H0M0S";
                    break;
                case MINUTE:
                    value = value + "0Y0M0DT0H" + getValue() + "M0S";
                    break;
                case SECOND:
                    value = value + "0Y0M0DT0H0M" + getValue() + "S";
                    break;
                default:
                    break;
            }
            rExpression = Expression.builder()
                    .edcType(DATASPACECONNECTOR_LITERALEXPRESSION)
                    .value(value)
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
