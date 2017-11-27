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

package com.deltadna.android.sdk.ads.provider.inmobi;

import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.inmobi.ads.InMobiInterstitial;

import java.util.Map;

final class RewardedEventForwarder extends EventForwarder {
    
    private boolean completed;
    
    RewardedEventForwarder(
            MediationListener listener,
            MediationAdapter adapter) {
        
        super(listener, adapter);
    }
    
    @Override
    protected boolean hasCompleted() {
        return completed;
    }
    
    @Override
    public void onAdRewardActionCompleted(
            InMobiInterstitial ad,
            Map<Object, Object> map) {
        
        super.onAdRewardActionCompleted(ad, map);
        completed = true;
    }
}
