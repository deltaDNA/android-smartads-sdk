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

package com.deltadna.android.sdk.ads.provider.unity;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.unity3d.ads.UnityAds;

import org.json.JSONObject;

public final class UnityRewardedAdapter extends MediationAdapter {
    
    private final String gameId;
    @Nullable
    private final String zoneId;
    private final boolean testMode;
    
    private boolean initialised;
    
    @Nullable
    private Activity activity;
    
    public UnityRewardedAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String gameId,
            @Nullable String zoneId,
            boolean testMode) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.gameId = gameId;
        this.zoneId = zoneId;
        this.testMode = testMode;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            final MediationListener listener,
            JSONObject configuration) {
        
        if (!initialised) {
            try {
                UnityAds.initialize(
                        activity,
                        gameId,
                        new UnityRewardedEventForwarder(listener, this),
                        testMode);
                
                initialised = true;
            } catch (Exception e) {
                Log.w(BuildConfig.LOG_TAG, "Failed to initialise", e);
                listener.onAdFailedToLoad(
                        this,
                        AdRequestResult.Configuration,
                        "Invalid Unity configuration: " + e);
            }
        } else {
            if (zoneId != null && UnityAds.isReady(zoneId)) {
                listener.onAdLoaded(this);
            } else if (UnityAds.isReady()) {
                listener.onAdLoaded(this);
            } else {
                Log.w(BuildConfig.LOG_TAG, "No fill");
                listener.onAdFailedToLoad(
                        UnityRewardedAdapter.this,
                        AdRequestResult.NoFill,
                        "Unity no fill");
            }
        }
        
        this.activity = activity;
    }
    
    @Override
    public void showAd() {
        if (zoneId != null) {
            if (UnityAds.isReady(zoneId) && activity != null) {
                UnityAds.show(activity, zoneId);
            }
        } else {
            if (UnityAds.isReady() && activity != null) {
                UnityAds.show(activity);
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
        activity = null;
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
