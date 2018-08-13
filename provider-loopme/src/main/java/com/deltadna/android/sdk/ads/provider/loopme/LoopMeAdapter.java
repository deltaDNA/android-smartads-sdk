/*
 * Copyright (c) 2017 deltaDNA Ltd. All rights reserved.
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

package com.deltadna.android.sdk.ads.provider.loopme;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdShowResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;
import com.loopme.LoopMeInterstitial;

import org.json.JSONObject;

public final class LoopMeAdapter extends MediationAdapter {
    
    private final String appKey;
    
    @Nullable
    private LoopMeInterstitial ad;
    @Nullable
    private MediationListener listener;
    @Nullable
    private EventForwarder forwarder;
    
    public LoopMeAdapter(
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
        
        this.appKey = (!testMode)
                ? appKey
                : LoopMeInterstitial.TEST_PORT_INTERSTITIAL;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        this.listener = listener;
        
        if (ad == null) {
            forwarder = new EventForwarder(this).setListener(listener);
            
            ad = LoopMeInterstitial.getInstance(appKey, activity);
            ad.setAutoLoading(false);
            ad.setListener(forwarder);
            ad.load();
        } else if (forwarder != null) {
            forwarder.setListener(listener);
            ad.load();
        }
    }
    
    @Override
    public void showAd() {
        if (ad != null) {
            if (forwarder != null && forwarder.hasExpired() && listener != null) {
                Log.w(BuildConfig.LOG_TAG, "Forwarded expired ad");
                listener.onAdFailedToShow(this, AdShowResult.EXPIRED);
            } else if (!ad.isReady() && listener != null) {
                Log.w(BuildConfig.LOG_TAG, "Ad is not ready");
                listener.onAdFailedToShow(this, AdShowResult.EXPIRED);
            } else if (!ad.isReady() && listener != null) {
                Log.w(BuildConfig.LOG_TAG, "Ad is not loaded");
                listener.onAdFailedToShow(this, AdShowResult.NOT_LOADED);
            } else {
                ad.show();
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
        if (ad != null)  {
            ad.destroy();
            ad = null;
        }
        
        listener = null;
        forwarder = null;
    }
    
    @Override
    public void onPause() {}
    
    @Override
    public void onResume() {}
    
    @Override
    public boolean isGdprCompliant() {
        return true;
    }
}
