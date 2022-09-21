/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the CatenaX (ng) GitHub Organisation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package com.catenax.dft.util;

import com.catenax.dft.entities.UsagePolicy;
import com.catenax.dft.enums.DurationEnum;
import com.catenax.dft.enums.PolicyAccessEnum;
import com.catenax.dft.enums.UsagePolicyEnum;
import org.apache.commons.lang3.StringUtils;


public class UtilityFunctions {

    public static String removeLastSlashOfUrl(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.lastIndexOf("/"));
        } else {
            return url;
        }
    }

    public static UsagePolicy getDurationPolicy(String durationValue) {
        DurationEnum durationUnit;
        //Sample value - P0Y0M3DT0H0M0S - Output - 3 Day(s)
        String value = StringUtils.substringBetween(durationValue, "P", "Y");
        if (!value.equals("0")) {
            durationUnit = DurationEnum.YEAR;
            return getResponse(value, durationUnit);
        }
        value = StringUtils.substringBetween(durationValue, "Y", "M");
        if (!value.equals("0")) {
            durationUnit = DurationEnum.MONTH;
            return getResponse(value, durationUnit);
        }
        value = StringUtils.substringBetween(durationValue, "M", "D");
        if (!value.equals("0")) {
            durationUnit = DurationEnum.DAY;
            return getResponse(value, durationUnit);
        }
        value = StringUtils.substringBetween(durationValue, "T", "H");
        if (!value.equals("0")) {
            durationUnit = DurationEnum.HOUR;
            return getResponse(value, durationUnit);
        }
        value = StringUtils.substringBetween(durationValue, "H", "M");
        if (!value.equals("0")) {
            durationUnit = DurationEnum.MINUTE;
            return getResponse(value, durationUnit);
        }
        value = durationValue.substring(durationValue.lastIndexOf("M") + 1, durationValue.indexOf("S"));
        if (!value.equals("0")) {
            durationUnit = DurationEnum.SECOND;
            return getResponse(value, durationUnit);
        }
        return null;
    }

    private static UsagePolicy getResponse(String value, DurationEnum durationUnit) {
        UsagePolicy policyResponse;
        policyResponse = UsagePolicy.builder().type(UsagePolicyEnum.DURATION)
                .typeOfAccess(PolicyAccessEnum.RESTRICTED)
                .value(value)
                .durationUnit(durationUnit)
                .build();
        return policyResponse;
    }

}
