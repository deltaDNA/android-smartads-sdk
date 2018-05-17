/*
 * Copyright (c) 2017 deltaDNA Ltd. All rights reserved.
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

package com.deltadna.android.sdk.ads.provider.machinezone;

import android.content.Context;

import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;
import com.fractionalmedia.sdk.AdZone;

public final class MachineZoneRewardedAdapter extends MachineZoneAdapter {
    
    public MachineZoneRewardedAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex,
            String adUnitId) {
        
        super(  eCPM,
                demoteOnCode,
                privacy,
                waterfallIndex,
                adUnitId);
    }
    
    @Override
    protected void requestAd(Context context, MediationListener listener) {
        AdZone.Rewarded(
                context,
                adUnitId,
                new RewardedEventForwarder(
                        new EventForwarder(this, listener)));
    }
}
