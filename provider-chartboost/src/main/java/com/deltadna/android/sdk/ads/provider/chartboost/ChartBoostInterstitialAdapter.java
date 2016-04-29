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

package com.deltadna.android.sdk.ads.provider.chartboost;

import android.app.Activity;
import android.util.Log;

import com.chartboost.sdk.CBLocation;
import com.chartboost.sdk.Chartboost;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;

import org.json.JSONObject;

public final class ChartBoostInterstitialAdapter extends MediationAdapter {
    
    public static final String LOCATION = CBLocation.LOCATION_DEFAULT;
    
    private final String appId;
    private final String appSignature;
    private final String location;
    
    private Activity activity;
    
    public ChartBoostInterstitialAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String appId,
            String appSignature,
            String location) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.appId = appId;
        this.appSignature = appSignature;
        this.location = location;
    }
    
    @Override
    public void requestAd(Activity activity, MediationListener listener, JSONObject configuration) {
        this.activity = activity;

        try {
            ChartBoostHelper.initialise(activity, appId, appSignature);
        } catch (Exception e) {
            Log.w(BuildConfig.LOG_TAG, "Failed to initialise", e);
            listener.onAdFailedToLoad(
                    this,
                    AdRequestResult.Configuration,
                    "Invalid Configuration: " + e);
            return;
        }

        ChartBoostHelper.setInterstitialListeners(listener, this);

        try {
            Chartboost.cacheInterstitial(location);
        } catch(Exception e){
            Log.e(BuildConfig.LOG_TAG, "Failed to request ad", e);
            listener.onAdFailedToLoad(
                    this,
                    AdRequestResult.Error,
                    "Failed to request Chartboost ad: " + e);
        }
    }

    @Override
    public void showAd() {
        if(ChartBoostHelper.isInitialised() && Chartboost.hasInterstitial(location)) {
            Chartboost.showInterstitial(location);
        }
    }

    @Override
    public String getProviderString() {
        return "CHARTBOOST";
    }

    @Override
    public String getProviderVersionString() {
        return "6.0.2";
    }
    
    @Override
    public void onDestroy() {
        if (activity != null && ChartBoostHelper.isInitialised()) {
            if (ChartBoostHelper.isInitialised()) {
                Chartboost.onDestroy(activity);
            }
            
            activity = null;
        }
    }
    
    @Override
    public void onPause() {
        if (activity != null && ChartBoostHelper.isInitialised()) {
            Chartboost.onPause(activity);
        }
    }
    
    @Override
    public void onResume() {
        if (activity != null && ChartBoostHelper.isInitialised()) {
            Chartboost.onResume(activity);
        }
    }
}
