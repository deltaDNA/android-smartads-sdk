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

package com.deltadna.android.sdk.ads.provider.flurry;

import android.app.Activity;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.flurry.android.FlurryAgent;
import com.flurry.android.ads.FlurryAdInterstitial;
import com.flurry.android.ads.FlurryAdTargeting;

import org.json.JSONObject;

public final class FlurryRewardedAdapter extends MediationAdapter {
    
    private final String apiKey;
    private final String adSpace;
    private final boolean testMode;
    
    private FlurryAdInterstitial interstitial;
    
    public FlurryRewardedAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String apiKey,
            String adSpace,
            boolean testMode) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.apiKey = apiKey;
        this.adSpace = adSpace;
        this.testMode = testMode;
        if(testMode) {
            Log.i(BuildConfig.LOG_TAG, "Flurry set to have test ads enabled");
        }
    }
    
    @Override
    public void requestAd(Activity activity, MediationListener listener, JSONObject configuration) {
        if(!FlurryHelper.isInitialised()) {
            try {
                FlurryHelper.initialise(activity, apiKey);
            } catch (Exception e) {
                Log.e(BuildConfig.LOG_TAG, "Failed to initialise", e);
                listener.onAdFailedToLoad(
                        this,
                        AdRequestResult.Configuration,
                        "Invalid Flurry configuration: " + e);
                return;
            }
        }

        try {
            FlurryAdTargeting adTargeting = new FlurryAdTargeting();
            adTargeting.setEnableTestAds(testMode);
            interstitial = new FlurryAdInterstitial(activity, adSpace);
            interstitial.setTargeting(adTargeting);
            interstitial.setListener(new FlurryRewardedEventForwarder(listener, this));
            interstitial.fetchAd();
        } catch (Exception e) {
            Log.e(BuildConfig.LOG_TAG, "Failed to fetch ad", e);
            listener.onAdFailedToLoad(
                    this,
                    AdRequestResult.Error,
                    "Failed to fetch Flurry ad: " + e);
        }
    }

    @Override
    public void showAd() {
        if(interstitial != null && interstitial.isReady()) {
            interstitial.displayAd();
        }
    }

    @Override
    public String getProviderString() {
        return "FLURRY";
    }

    @Override
    public String getProviderVersionString() {
        return FlurryAgent.getReleaseVersion();
    }
    
    @Override
    public void onDestroy() {
        if (interstitial != null) {
            interstitial.destroy();
            interstitial = null;
        }
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
