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

package com.deltadna.android.sdk.ads.bindings;

/**
 * Created by davidwhite on 29/05/15.
 */
public interface MediationListener {

    void onAdLoaded(MediationAdapter mediationAdapter);

    void onAdFailedToLoad(MediationAdapter mediationAdapter, AdRequestResult adLoadResult, String reason);

    void onAdShowing(MediationAdapter mediationAdapter);

    void onAdFailedToShow(MediationAdapter mediationAdapter, AdClosedResult adClosedResult);

    void onAdClicked(MediationAdapter mediationAdapter);

    void onAdLeftApplication(MediationAdapter mediationAdapter);

    void onAdClosed(MediationAdapter mediationAdapter, boolean complete);
}
