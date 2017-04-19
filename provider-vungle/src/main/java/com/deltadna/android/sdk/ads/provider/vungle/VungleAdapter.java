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

package com.deltadna.android.sdk.ads.provider.vungle;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MainThread;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.vungle.publisher.EventListener;
import com.vungle.publisher.VunglePub;

import org.json.JSONObject;

public final class VungleAdapter extends MediationAdapter {
    
    private final String appId;
    
    private EventForwarder forwarder;
    private boolean initialised;
    
    @Nullable
    private VunglePub vunglePub;
    
    public VungleAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String appId) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.appId = appId;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        if (!initialised) {
            Log.d(BuildConfig.LOG_TAG, "Initialising");
            
            try {
                forwarder = new EventForwarder(this, listener);
                
                vunglePub = VunglePub.getInstance();
                vunglePub.init(activity, appId);
                vunglePub.setEventListeners(MainThread.redirect(
                        forwarder,
                        EventListener.class));
                
                initialised = true;
                Log.d(BuildConfig.LOG_TAG, "Initialised");
            } catch (Exception e) {
                Log.w(BuildConfig.LOG_TAG, "Failed to initialise", e);
                listener.onAdFailedToLoad(
                        this,
                        AdRequestResult.Configuration,
                        "Invalid Vungle configuration: " + e);
                return;
            }
        }
        
        forwarder.requestPerformed(listener);
    }
    
    @Override
    public void showAd() {
        if (vunglePub != null && vunglePub.isAdPlayable()) {
            vunglePub.playAd();
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
        vunglePub = null;
    }
    
    @Override
    public void onPause() {
        if (vunglePub != null) {
            vunglePub.onPause();
        }
    }
    
    @Override
    public void onResume() {
        if (vunglePub != null) {
            vunglePub.onResume();
        }
    }
}
