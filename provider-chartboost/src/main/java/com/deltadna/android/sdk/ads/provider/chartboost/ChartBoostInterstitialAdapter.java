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

package com.deltadna.android.sdk.ads.provider.chartboost;

import android.app.Activity;

import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;

import org.json.JSONObject;

public final class ChartBoostInterstitialAdapter extends ChartBoostAdapter {
    
    public ChartBoostInterstitialAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex,
            String appId,
            String appSignature,
            String location) {
        
        super(  eCPM,
                demoteOnCode,
                privacy,
                waterfallIndex,
                appId,
                appSignature,
                location);
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        super.requestAd(activity, listener, configuration);
        
        Helper.requestInterstitial(
                location,
                listener,
                this);
    }
    
    @Override
    public void showAd() {
        Helper.showInterstitial(location);
    }
}
