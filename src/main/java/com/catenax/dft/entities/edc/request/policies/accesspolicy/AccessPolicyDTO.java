package com.catenax.dft.entities.edc.request.policies.accesspolicy;

import com.catenax.dft.entities.edc.request.policies.ConstraintRequest;
import com.catenax.dft.entities.edc.request.policies.Expression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccessPolicyDTO {
    private static final String DATASPACECONNECTOR_LITERALEXPRESSION = "dataspaceconnector:literalexpression";

    List<String> bpnNumbers;

    public ConstraintRequest toConstraint() {
        Expression lExpression = Expression.builder()
                .edcType(DATASPACECONNECTOR_LITERALEXPRESSION)
                .value("BusinessPartnerNumber")
                .build();

        String operator = "IN";
        Expression rExpression = null;
        rExpression = Expression.builder()
                    .edcType(DATASPACECONNECTOR_LITERALEXPRESSION)
                    .value(bpnNumbers)
                    .build();

        return ConstraintRequest.builder().edcType("AtomicConstraint")
                .leftExpression(lExpression)
                .rightExpression(rExpression)
                .operator(operator)
                .build();
    }
}
