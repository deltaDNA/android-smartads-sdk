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

package com.deltadna.android.sdk.ads.provider.admob;

import android.app.Activity;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONObject;

final public class AdMobAdapter extends MediationAdapter {
    
    private final String adUnitId;
    
    private InterstitialAd interstitial;
    
    public AdMobAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String adUnitId) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.adUnitId = adUnitId;
    }
    
    @Override
    public void requestAd(final Activity activity, final MediationListener listener, JSONObject configuration) {
        if(interstitial == null) {
            try {
                Log.d(BuildConfig.LOG_TAG, "Initialising");
                
                interstitial = new InterstitialAd(activity);
                interstitial.setAdUnitId(adUnitId);
                interstitial.setAdListener(new AdMobEventForwarder(listener, this));
            } catch (Exception e) {
                Log.e(BuildConfig.LOG_TAG, "Failed to initialise", e);
                listener.onAdFailedToLoad(
                        this,
                        AdRequestResult.Configuration,
                        "Invalid AdMob configuration: " + e);
                return;
            }
        }
        
        try {
            interstitial.loadAd(new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build());
        } catch (Exception e) {
            Log.w(BuildConfig.LOG_TAG, "Failed to request ad", e);
            listener.onAdFailedToLoad(
                    this,
                    AdRequestResult.Error,
                    "SDK exception on request: " + e.getMessage());
        }
    }
    
    @Override
    public void showAd() {
        if (interstitial != null && interstitial.isLoaded()) {
            interstitial.show();
        }
    }
    
    @Override
    public String getProviderString() {
        return "ADMOB";
    }
    
    @Override
    public String getProviderVersionString() {
        return "7.8.0";
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
