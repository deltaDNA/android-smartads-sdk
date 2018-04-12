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

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.AdShowResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;

public interface AdAgentListener {

    void onAdLoaded(AdAgent adAgent, MediationAdapter mediationAdapter, long requestTime);

    void onAdFailedToLoad(AdAgent adAgent, MediationAdapter mediationAdapter, String reason, long requestTime, AdRequestResult adLoadResult);

    void onAdOpened(AdAgent adAgent, MediationAdapter mediationAdapter);

    void onAdFailedToOpen(AdAgent agent, MediationAdapter adapter, String reason, AdShowResult result);

    void onAdClosed(AdAgent adAgent, MediationAdapter mediationAdapter, boolean complete);
}
