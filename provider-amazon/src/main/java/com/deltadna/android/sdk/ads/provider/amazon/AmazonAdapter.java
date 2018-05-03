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
import android.support.annotation.Nullable;
import android.util.Log;

import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.InterstitialAd;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.AdShowResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;

import org.json.JSONObject;

public final class AmazonAdapter extends MediationAdapter {
    
    private static boolean initialised;
    
    private final String appKey;
    private final boolean testMode;
    
    @Nullable
    private MediationListener listener;
    @Nullable
    private AmazonEventForwarder forwarder;
    @Nullable
    private InterstitialAd interstitial;
    
    public AmazonAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex,
            String appKey,
            boolean testMode) {
        
        super(  eCPM,
                demoteOnCode,
                privacy,
                waterfallIndex);
        
        this.appKey = appKey;
        this.testMode = testMode;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        synchronized (AmazonAdapter.class) {
            if (!initialised) {
                Log.d(BuildConfig.LOG_TAG, "Initialising");
                
                try {
                    AdRegistration.enableTesting(testMode);
                    AdRegistration.setAppKey(appKey);
                } catch (Exception e) {
                    Log.w(BuildConfig.LOG_TAG, "Failed to initialise", e);
                    listener.onAdFailedToLoad(
                            this,
                            AdRequestResult.Configuration,
                            "Failed to initialise: " + e);
                    return;
                }
                
                initialised = true;
            }
        }
        
        this.listener = listener;
        
        forwarder = new AmazonEventForwarder(listener, this);
        try {
            interstitial = new InterstitialAd(activity);
            interstitial.setListener(forwarder);
        } catch (Exception e) {
            Log.w(BuildConfig.LOG_TAG, "Failed to create ad", e);
            listener.onAdFailedToLoad(
                    this,
                    AdRequestResult.Error,
                    "Failed to create ad: " + e);
            return;
        }
        
        if (!interstitial.isLoading() && !interstitial.isShowing()) {
            try {
                interstitial.loadAd();
            } catch (Exception e) {
                Log.w(BuildConfig.LOG_TAG, "Failed to load ad", e);
                listener.onAdFailedToLoad(
                        this,
                        AdRequestResult.Error,
                        "Failed to load ad: " + e);
            }
        }
    }
    
    @Override
    public void showAd() {
        if (listener != null) {
            if (interstitial == null) {
                Log.w(BuildConfig.LOG_TAG, "Ad is null");
                listener.onAdFailedToShow(this, AdShowResult.ERROR);
            } else if (forwarder != null && forwarder.isExpired()) {
                Log.w(BuildConfig.LOG_TAG, "Forwarded expired ad");
                listener.onAdFailedToShow(this, AdShowResult.EXPIRED);
            } else if (!interstitial.isReady()) {
                Log.w(BuildConfig.LOG_TAG, "Ad is not ready");
                listener.onAdFailedToShow(this, AdShowResult.EXPIRED);
            } else {
                if (!interstitial.showAd()) {
                    Log.w(BuildConfig.LOG_TAG, "Showing ad failed");
                    listener.onAdFailedToShow(this, AdShowResult.ERROR);
                } else {
                    listener.onAdShowing(this);
                }
            }
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
        listener = null;
        forwarder = null;
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
