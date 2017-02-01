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

import com.applovin.adview.AppLovinIncentivizedInterstitial;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;

import org.json.JSONObject;

public final class AppLovinRewardedAdapter extends MediationAdapter {
    
    private static final String TAG = BuildConfig.LOG_TAG
            + ' '
            + AppLovinRewardedAdapter.class.getSimpleName();
    
    private final String key;
    private final String placement;
    private final boolean verboseLogging;
    private final long adRefreshSeconds;
    
    @Nullable
    private AppLovinSdk sdk;
    
    @Nullable
    private Activity activity;
    @Nullable
    private AppLovinEventForwarder forwarder;
    @Nullable
    private AppLovinIncentivizedInterstitial rewarded;
    
    public AppLovinRewardedAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String key,
            String placement,
            boolean verboseLogging,
            long adRefreshSeconds) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.key = key;
        this.placement = placement;
        this.verboseLogging = verboseLogging;
        this.adRefreshSeconds = adRefreshSeconds;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        if (sdk == null) {
            Log.d(BuildConfig.LOG_TAG, "Initialising AppLovin SDK");
            
            final AppLovinSdkSettings settings = new AppLovinSdkSettings();
            settings.setVerboseLogging(verboseLogging);
            settings.setBannerAdRefreshSeconds(adRefreshSeconds);
            
            try {
                sdk = AppLovinSdk.getInstance(
                        key,
                        settings,
                        activity.getApplicationContext());
            } catch (Exception e) {
                Log.e(TAG, "Failed initialisation", e);
                listener.onAdFailedToLoad(
                        this,
                        AdRequestResult.Configuration,
                        "Failed initialisation " + e);
                return;
            }
        }
        
        this.activity = activity;
        forwarder = new AppLovinEventForwarder(listener, this);
        
        //noinspection ConstantConditions
        rewarded = AppLovinIncentivizedInterstitial.create(sdk);
        rewarded.preload(forwarder);
    }
    
    @Override
    public void showAd() {
        if (rewarded != null && rewarded.isAdReadyToDisplay()) {
            rewarded.show(
                    activity,
                    placement,
                    null,
                    forwarder,
                    forwarder,
                    forwarder);
        }
    }
    
    @Override
    public String getProviderString() {
        return "APPLOVIN";
    }
    
    @Override
    public String getProviderVersionString() {
        return AppLovinSdk.VERSION;
    }
    
    @Override
    public void onDestroy() {
        if (rewarded != null) {
            rewarded.dismiss();
            rewarded = null;
        }
        
        activity = null;
        forwarder = null;
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
