package com.catenax.dft.entities.edc.request.policies;

import com.catenax.dft.entities.UsagePolicyRequest;
import com.catenax.dft.entities.edc.request.policies.accesspolicy.AccessPolicyDTO;
import com.catenax.dft.entities.edc.request.policies.usagepolicy.DurationPolicyDTO;
import com.catenax.dft.entities.edc.request.policies.usagepolicy.PurposePolicyDTO;
import com.catenax.dft.entities.edc.request.policies.usagepolicy.RolePolicyDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PolicyConstraintBuilderService {

    public List<ConstraintRequest> getPolicyConstraints(List<String> bpnNumbers, List<UsagePolicyRequest> usagePolicies) {
        List<ConstraintRequest> constraints = new ArrayList<>();

        if (bpnNumbers != null && !bpnNumbers.isEmpty()) {
            AccessPolicyDTO accessPolicy = AccessPolicyDTO.builder().bpnNumbers(bpnNumbers).build();
            constraints.add(accessPolicy.toConstraint());
        }
        if (usagePolicies != null && !usagePolicies.isEmpty()) {
            constraints.addAll(getUsagePolicyConstraints(usagePolicies));
        }

        return constraints;
    }

    private List<ConstraintRequest> getUsagePolicyConstraints(List<UsagePolicyRequest> usagePolicies) {
        List<ConstraintRequest> usageConstraintList = new ArrayList<>();
        usagePolicies.stream().forEach(policy ->
        {
            switch (policy.getType()) {
                case DURATION:
                    ConstraintRequest request =DurationPolicyDTO.fromUsagePolicy(policy).toConstraint();
                    if (request != null) {
                        usageConstraintList.add(request);
                    }
                    break;
                case PURPOSE:
                    ConstraintRequest purposeRequest = PurposePolicyDTO.fromUsagePolicy(policy).toConstraint();
                    if (purposeRequest != null) {
                        usageConstraintList.add(purposeRequest);
                    }
                    break;
                case ROLE:
                    ConstraintRequest roleRequest = RolePolicyDTO.fromUsagePolicy(policy).toConstraint();
                    if (roleRequest != null) {
                        usageConstraintList.add(roleRequest);
                    }
                    break;
                default:
                    break;
            }
        });
        return usageConstraintList;
    }
}
