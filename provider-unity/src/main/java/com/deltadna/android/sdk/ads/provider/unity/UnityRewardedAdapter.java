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
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.unity3d.ads.android.UnityAds;

import org.json.JSONObject;

public final class UnityRewardedAdapter extends MediationAdapter {
    
    private final String gameId;
    private final String zoneId;
    private final boolean testMode;
    
    private Activity activity;
    private boolean initialised;
    
    public UnityRewardedAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String gameId,
            String zoneId,
            boolean testMode) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.gameId = gameId;
        this.zoneId = zoneId;
        this.testMode = testMode;
    }
    
    @Override
    public void requestAd(Activity activity, MediationListener listener, JSONObject configuration) {
        this.activity = activity;
        
        if (!initialised) {
            try {
                UnityAds.init(
                        activity,
                        gameId,
                        new UnityRewardedEventForwarder(listener, this));
                if (zoneId != null) {
                    UnityAds.setZone(zoneId);
                }
                UnityAds.setTestMode(testMode);
                
                initialised = true;
            } catch (Exception e) {
                Log.w(BuildConfig.LOG_TAG, "Failed to initialise", e);
                listener.onAdFailedToLoad(
                        this,
                        AdRequestResult.Configuration,
                        "Invalid Unity configuration: " + e);
            }
        }
        
        if (UnityAds.canShow()) {
            listener.onAdLoaded(this);
        } else {
            Log.w(BuildConfig.LOG_TAG, "No fill");
            listener.onAdFailedToLoad(
                    this,
                    AdRequestResult.NoFill,
                    "Unity no fill");
        }
    }

    @Override
    public void showAd() {
        if(UnityAds.canShow()) {
            UnityAds.show();
        }
    }

    @Override
    public String getProviderString() {
        return "UNITY";
    }

    @Override
    public String getProviderVersionString() {
        return UnityAds.getSDKVersion();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {
        if(activity != null) {
            UnityAds.changeActivity(activity);
        }
    }
}
