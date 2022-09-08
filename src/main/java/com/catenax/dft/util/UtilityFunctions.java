/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.catenax.dft.util;

import org.apache.commons.lang3.StringUtils;


public class UtilityFunctions {

    public static String removeLastSlashOfUrl(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.lastIndexOf("/"));
        } else {
            return url;
        }
    }

    public static String getDurationValue(String durationValue) {
        //Sample value - P0Y0M3DT0H0M0S - Output - 3 Day(s)
        String value = StringUtils.substringBetween(durationValue, "P", "Y");
        if (!value.equals("0")) {
            return value + " Year(s)";
        }
        value = StringUtils.substringBetween(durationValue, "Y", "M");
        if (!value.equals("0")) {
            return value + " Month(s)";
        }
        value = StringUtils.substringBetween(durationValue, "M", "D");
        if (!value.equals("0")) {
            return value + " Day(s)";
        }
        value = StringUtils.substringBetween(durationValue, "T", "H");
        if (!value.equals("0")) {
            return value + " Hour(s)";
        }
        value = StringUtils.substringBetween(durationValue, "H", "M");
        if (!value.equals("0")) {
            return value + " Minute(s)";
        }
        value = durationValue.substring(durationValue.lastIndexOf("M") + 1, durationValue.indexOf("S"));
        if (!value.equals("0")) {
            return value + " Second(s)";
        }
        return null;
    }

}
