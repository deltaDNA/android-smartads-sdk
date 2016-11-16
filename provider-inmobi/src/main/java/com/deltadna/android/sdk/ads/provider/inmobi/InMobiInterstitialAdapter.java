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

import android.app.Activity;
import android.support.annotation.Nullable;

import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.sdk.InMobiSdk;

import org.json.JSONObject;

public final class InMobiInterstitialAdapter extends MediationAdapter {
    
    private final String accountId;
    private final Long placementId;
    
    @Nullable
    private InMobiInterstitial interstitial;
    
    public InMobiInterstitialAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String accountId,
            Long placementId) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.accountId = accountId;
        this.placementId = placementId;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject mediationParams) {
        
        synchronized (InMobiHelper.class) {
            if (!InMobiHelper.isInitialised()) {
                InMobiHelper.initialise(activity, accountId);
            }
        }
        
        interstitial = new InMobiInterstitial(
                activity,
                placementId,
                new InMobiInterstitialEventForwarder(listener, this));
        interstitial.load();
    }
    
    @Override
    public void showAd() {
        if (interstitial != null && interstitial.isReady()) {
            interstitial.show();
            interstitial = null;
        }
    }
    
    @Override
    public String getProviderString() {
        return "INMOBI";
    }
    
    @Override
    public String getProviderVersionString() {
        return InMobiSdk.getVersion();
    }
    
    @Override
    public void onDestroy() {
        interstitial = null;
    }
    
    @Override
    public void onPause() {
        // cannot forward
    }
    
    @Override
    public void onResume() {
        // cannot forward
    }
}
