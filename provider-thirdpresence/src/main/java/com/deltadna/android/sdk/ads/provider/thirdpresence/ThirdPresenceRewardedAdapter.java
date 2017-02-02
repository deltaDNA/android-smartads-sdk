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

package com.deltadna.android.sdk.ads.provider.thirdpresence;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.thirdpresence.adsdk.sdk.VideoAd;
import com.thirdpresence.adsdk.sdk.VideoAdManager;
import com.thirdpresence.adsdk.sdk.internal.TLog;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public final class ThirdPresenceRewardedAdapter extends MediationAdapter {
    
    private final String placementId;
    
    private final Map<String, String> environment;
    private final Map<String, String> parameters;
    
    @Nullable
    private Activity activity;
    @Nullable
    private MediationListener listener;
    @Nullable
    ThirdPresenceRewardedEventForwarder forwarder;
    @Nullable
    private VideoAd ad;
    
    public ThirdPresenceRewardedAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String accountName,
            String placementId,
            boolean testMode) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.placementId = (testMode) ? "sa7nvltbrn" : placementId;
        
        environment = new HashMap<>();
        environment.put(
                VideoAd.Environment.KEY_ACCOUNT,
                (testMode) ? "sdk-demo" : accountName);
        environment.put(
                VideoAd.Environment.KEY_PLACEMENT_ID,
                placementId);
        parameters = new HashMap<>();
        
        TLog.enabled = testMode;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        this.activity = activity;
        this.listener = listener;
        forwarder = new ThirdPresenceRewardedEventForwarder(listener, this);
        
        try {
            ad = VideoAdManager.getInstance().create(
                    VideoAd.PLACEMENT_TYPE_INTERSTITIAL,
                    placementId);
            ad.setListener(forwarder);
            ad.init(activity,
                    environment,
                    parameters,
                    VideoAd.DEFAULT_TIMEOUT);
            ad.loadAd();
        } catch (Exception e) {
            final String msg = "Failed to initialise ThirdPresence: " + e.getMessage();
            Log.w(BuildConfig.LOG_TAG, msg);
            listener.onAdFailedToLoad(this, AdRequestResult.Configuration, msg);
        }
    }
    
    @Override
    public void showAd() {
        if (    listener != null
                && forwarder != null
                && ad != null
                && ad.isAdLoaded()) {
            
            ad.displayAd(activity, new Runnable() {
                @Override
                public void run() {
                    Log.d(  BuildConfig.LOG_TAG,
                            "Completed: " + forwarder.hasCompleted());
                    
                    listener.onAdClosed(
                            ThirdPresenceRewardedAdapter.this,
                            forwarder.hasCompleted());
                    
                    activity = null;
                    listener = null;
                    forwarder = null;
                    
                    ad.reset();
                    ad = null;
                }
            });
        }
    }
    
    @Override
    public String getProviderString() {
        return "THIRDPRESENCE";
    }
    
    @Override
    public String getProviderVersionString() {
        return "1.5.1";
    }
    
    @Override
    public void onDestroy() {
        activity = null;
        listener = null;
        forwarder = null;
        ad = null;
    }
    
    @Override
    public void onPause() {}
    
    @Override
    public void onResume() {}
}
