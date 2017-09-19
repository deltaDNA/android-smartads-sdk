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

package com.deltadna.android.sdk.ads.provider.facebook;

import android.app.Activity;

import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.facebook.ads.RewardedVideoAd;

import org.json.JSONObject;

public final class FacebookRewardedAdapter extends FacebookAdapter {
    
    private RewardedVideoAd ad;
    
    public FacebookRewardedAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String placementId) {
        
        super(eCPM, demoteOnCode, waterfallIndex, placementId);
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        ad = new RewardedVideoAd(activity, placementId);
        ad.setAdListener(new RewardedEventForwarder(this, listener));
        ad.loadAd();
    }
    
    @Override
    public void showAd() {
        if (ad != null && ad.isAdLoaded()) {
            ad.show();
        }
    }
    
    @Override
    public void onDestroy() {
        if (ad != null) {
            ad.destroy();
            ad = null;
        }
    }
}
