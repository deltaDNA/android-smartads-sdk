/*
 * Copyright (c) 2016 deltaDNA Ltd. All rights reserved.
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
 */

package com.deltadna.android.sdk.ads.core;

public enum AdShowResult {

    FULFILLED("Fulfilled"),
    NO_AD_AVAILABLE("No ad was available"),
    AD_SHOW_POINT("adShowPoint was false"),
    SESSION_LIMIT_REACHED("Session limit reached"),
    MIN_TIME_NOT_ELAPSED("adMinimumInterval not elapsed"),
    NOT_READY("Not ready"),
    AD_SHOW_ENGAGE_FAILED("Engage hit failed, showing ad anyway"),
    INTERNAL_ERROR("Internal error"),
    NOT_REGISTERED("Not registered");

    private String status;

    AdShowResult(String status) {
        this.status = status;
    }

    String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "AdStatus{" +
                "status='" + status + '\'' +
                '}';
    }
}
