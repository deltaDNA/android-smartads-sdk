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

package com.deltadna.android.sdk.ads.provider.adcolony;

import android.app.Activity;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.jirbo.adcolony.AdColony;
import com.jirbo.adcolony.AdColonyVideoAd;

import org.json.JSONObject;

public final class AdColonyAdapter extends MediationAdapter {
    
    private final String appId;
    private final String clientOptions;
    private final String zoneIds;
    
    private Activity activity;
    
    private boolean initialised;
    private AdColonyVideoAd videoAd;
    private AdColonyAvailabilityMonitor availabilityMonitor;
    
    public AdColonyAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String appId,
            String clientOptions,
            String zoneIds) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.appId = appId;
        this.clientOptions = clientOptions;
        this.zoneIds = zoneIds;
    }
    
    @Override
    public void requestAd(Activity activity, MediationListener listener, JSONObject configuration) {
        if(!initialised) {
            this.activity = activity;
            availabilityMonitor = new AdColonyAvailabilityMonitor(listener, this);
            try {
                AdColony.configure(activity, clientOptions, appId, zoneIds);
                AdColony.addAdAvailabilityListener(availabilityMonitor);
            } catch(Exception e) {
                Log.e(BuildConfig.LOG_TAG, "Failed to initialise", e);
                listener.onAdFailedToLoad(
                        this,
                        AdRequestResult.Configuration,
                        "Invalid AdColony configuration: " + e);
                return;
            }

            initialised = true;

            try {
                videoAd = new AdColonyVideoAd();
                AdColonyEventForwarder forwarder = new AdColonyEventForwarder(listener, this);
                availabilityMonitor.setForwarder(forwarder);
                videoAd.withListener(forwarder);
            } catch (Exception e) {
                Log.e(BuildConfig.LOG_TAG, "Failed to load ad", e);
                listener.onAdFailedToLoad(
                        this,
                        AdRequestResult.Error,
                        "Failed to load AdColony ad: " + e);
            }
        } else {
            // Ad Colony doesn't trigger changes on it's ad availability interface when the next ad has loaded (seemingly)
            // therefore we need to just take the last value on every attempt subsequent to initialisation.
            try {
                videoAd = new AdColonyVideoAd();
                AdColonyEventForwarder forwarder = new AdColonyEventForwarder(listener, this);
                availabilityMonitor.setForwarder(forwarder);
                videoAd.withListener(forwarder);
                if(availabilityMonitor.isAvailable()) {
                    listener.onAdLoaded(this);
                } else {
                    Log.w(BuildConfig.LOG_TAG, "No fill");
                    listener.onAdFailedToLoad(
                            this,
                            AdRequestResult.NoFill,
                            availabilityMonitor.getReason());
                }
            } catch (Exception e) {
                Log.e(BuildConfig.LOG_TAG, "Failed to load ad", e);
                listener.onAdFailedToLoad(
                        this,
                        AdRequestResult.Error,
                        "Failed to load AdColony ad: " + e);
            }
        }
    }

    @Override
    public void showAd() {
        if(initialised && videoAd != null && videoAd.isReady()) {
            videoAd.show();
        }
    }

    @Override
    public String getProviderString() {
        return "ADCOLONY";
    }

    @Override
    public String getProviderVersionString() {
        return "2.3.0";
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onPause() {
        if(initialised) {
            AdColony.pause();
        }
    }

    @Override
    public void onResume() {
        if(initialised) {
            AdColony.resume(activity);
        }
    }
}
