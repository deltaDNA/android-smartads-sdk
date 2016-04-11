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

package com.deltadna.android.sdk.ads.provider.applovin;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;

import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;

import org.json.JSONObject;

public final class AppLovinInterstitialAdapter extends MediationAdapter {
    
    private final String key;
    private final boolean verboseLogging;
    private final long adRefreshSeconds;
    
    @Nullable
    private AppLovinSdk sdk;
    @Nullable
    private AppLovinInterstitialAdDialog interstitial;
    
    public AppLovinInterstitialAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String key,
            boolean verboseLogging,
            long adRefreshSeconds) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.key = key;
        this.verboseLogging = verboseLogging;
        this.adRefreshSeconds = adRefreshSeconds;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        if (sdk == null) {
            final AppLovinSdkSettings settings = new AppLovinSdkSettings();
            settings.setVerboseLogging(verboseLogging);
            settings.setBannerAdRefreshSeconds(adRefreshSeconds);
            
            sdk = AppLovinSdk.getInstance(
                    key,
                    new AppLovinSdkSettings(),
                    activity.getApplicationContext());
        } else {
            Log.w(BuildConfig.LOG_TAG, "SDK has already been initialised");
        }
        
        final AppLovinEventForwarder forwarder =
                new AppLovinEventForwarder(listener, this);
        
        //noinspection ConstantConditions
        interstitial = AppLovinInterstitialAd.create(sdk, activity);
        interstitial.setAdLoadListener(forwarder);
        interstitial.setAdDisplayListener(forwarder);
        interstitial.setAdVideoPlaybackListener(forwarder);
        interstitial.setAdClickListener(forwarder);
    }
    
    @Override
    public void showAd() {
        if (interstitial != null && interstitial.isAdReadyToDisplay()) {
            interstitial.show();
        }
    }
    
    @Override
    public String getProviderString() {
        return "APPLOVIN";
    }
    
    @Override
    public String getProviderVersionString() {
        return Version.VALUE;
    }
    
    @Override
    public void onDestroy() {
        if (interstitial != null && interstitial.isShowing()) {
            interstitial.dismiss();
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
