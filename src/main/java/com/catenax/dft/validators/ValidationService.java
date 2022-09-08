package com.catenax.dft.validators;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.catenax.dft.entities.UsagePolicyRequest;
import com.catenax.dft.enums.PolicyAccessEnum;

@Service
public class ValidationService {

    public boolean isValid(List<UsagePolicyRequest> usagePolicyRequests) {
        if(!CollectionUtils.isEmpty(usagePolicyRequests))
        {
            boolean validateFlag = false;
            for(UsagePolicyRequest usagePolicyRequest : usagePolicyRequests) {
                switch (usagePolicyRequest.getType()) {
                    case DURATION:
                        validateFlag = validateDuration(usagePolicyRequest);
                        break;
                    default:
                        validateFlag = validatePolicy(usagePolicyRequest);
                        break;
                }
                if(!validateFlag){
                    break;
                }
            }
            return validateFlag;
        }
       return true;
    }
    private boolean validatePolicy(UsagePolicyRequest usagePolicyRequest) {

        boolean isValid = false;
        if(usagePolicyRequest.getTypeOfAccess() != null && usagePolicyRequest.getTypeOfAccess().equals(PolicyAccessEnum.UNRESTRICTED)) {
            isValid = true;
        }
        else if(usagePolicyRequest.getTypeOfAccess() != null && usagePolicyRequest.getTypeOfAccess().equals(PolicyAccessEnum.RESTRICTED) &&
                StringUtils.isNotBlank(usagePolicyRequest.getValue())) {
            isValid= true;
        }
        return isValid;
    }

    private boolean validateDuration(UsagePolicyRequest usagePolicyRequest) {

        boolean isValid = false;
        if(usagePolicyRequest.getTypeOfAccess() != null && usagePolicyRequest.getTypeOfAccess().equals(PolicyAccessEnum.UNRESTRICTED)) {
            isValid = true;
        }
        else if(usagePolicyRequest.getTypeOfAccess() != null && usagePolicyRequest.getTypeOfAccess().equals(PolicyAccessEnum.RESTRICTED) &&
                StringUtils.isNotBlank(usagePolicyRequest.getValue()) &&
                usagePolicyRequest.getDurationUnit() != null) {
            isValid = true;
        }
        return isValid;
    }
}
