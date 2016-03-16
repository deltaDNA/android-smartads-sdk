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

package com.deltadna.android.sdk.ads.provider.amazon;

import android.app.Activity;
import android.util.Log;

import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.InterstitialAd;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;

import org.json.JSONObject;

public final class AmazonAdapter extends MediationAdapter {
    
    private final String appKey;
    private final boolean testMode;
    
    private InterstitialAd interstitial;
    
    public AmazonAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String appKey,
            boolean testMode) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.appKey = appKey;
        this.testMode = testMode;
        if(testMode) {
            Log.i(BuildConfig.LOG_TAG, "Amazon set to have test ads enabled");
        }
    }
    
    @Override
    public void requestAd(Activity activity, MediationListener listener, JSONObject configuration) {
        if(interstitial == null) {
            try {
                Log.d(BuildConfig.LOG_TAG, "Initialising");
                
                AdRegistration.enableTesting(testMode);
                AdRegistration.setAppKey(appKey);
                
                interstitial = new InterstitialAd(activity);
                interstitial.setListener(new AmazonEventForwarder(listener, this));
            } catch (Exception e) {
                Log.e(BuildConfig.LOG_TAG, "Failed to initialise", e);
                listener.onAdFailedToLoad(
                        this,
                        AdRequestResult.Configuration,
                        "Invalid AdMob configuration: " + e);
                return;
            }
        }
        
        if(!interstitial.isLoading() && !interstitial.isShowing()) {
            try {
                interstitial.loadAd();
            } catch (Exception e) {
                Log.w(BuildConfig.LOG_TAG, "Failed to request ad", e);
                listener.onAdFailedToLoad(
                        this,
                        AdRequestResult.Error,
                        "Failed to request AdMob ad: " + e);
            }
        }
    }
    
    @Override
    public void showAd() {
        if(interstitial != null && interstitial.isReady()){
            interstitial.showAd();
        }
    }
    
    @Override
    public String getProviderString() {
        return "AMAZON";
    }

    @Override
    public String getProviderVersionString() {
        return AdRegistration.getVersion();
    }
    
    @Override
    public void onDestroy() {}
    
    @Override
    public void onPause() {}
    
    @Override
    public void onResume() {}
}
