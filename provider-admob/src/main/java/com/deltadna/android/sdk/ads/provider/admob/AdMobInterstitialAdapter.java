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
import com.deltadna.android.sdk.ads.bindings.Privacy;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONObject;

final public class AdMobInterstitialAdapter extends MediationAdapter {
    
    private final String appId;
    private final String adUnitId;
    private final boolean testMode;
    
    private InterstitialAd interstitial;
    
    public AdMobInterstitialAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex,
            String appId,
            String adUnitId,
            boolean testMode) {
        
        super(  eCPM,
                demoteOnCode,
                privacy,
                waterfallIndex);
        
        this.appId = appId;
        this.adUnitId = testMode
                ? "ca-app-pub-3940256099942544/1033173712"
                : adUnitId;
        this.testMode = testMode;
    }
    
    @Override
    public void requestAd(final Activity activity, final MediationListener listener, JSONObject configuration) {
        
        InitialisationHelper.initialise(activity, appId);
        
        if(interstitial == null) {
            try {
                Log.d(BuildConfig.LOG_TAG, "Initialising");
                
                interstitial = new InterstitialAd(activity);
                interstitial.setAdUnitId(adUnitId);
                interstitial.setAdListener(new InterstitialEventForwarder(listener, this));
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
            final AdRequest.Builder request = new AdRequest.Builder();
            if (testMode) request.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            
            interstitial.loadAd(request.build());
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
        return BuildConfig.PROVIDER_NAME;
    }
    
    @Override
    public String getProviderVersionString() {
        return BuildConfig.PROVIDER_VERSION;
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
