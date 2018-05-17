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
import android.text.TextUtils;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MainThread;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;
import com.vungle.publisher.VungleAdEventListener;
import com.vungle.publisher.VungleInitListener;
import com.vungle.publisher.VunglePub;

import org.json.JSONObject;

public final class VungleAdapter extends MediationAdapter {
    
    private final String appId;
    private final String placementId;
    
    private EventForwarder forwarder;
    private boolean initialised;
    
    private VunglePub vunglePub;
    
    public VungleAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex,
            String appId,
            String placementId) {
        
        super(  eCPM,
                demoteOnCode,
                privacy,
                waterfallIndex);
        
        this.appId = appId;
        this.placementId = placementId;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            final MediationListener listener,
            JSONObject configuration) {
        
        if (TextUtils.isEmpty(placementId)) {
            listener.onAdFailedToLoad(
                    this,
                    AdRequestResult.Configuration,
                    "placementId is null or empty");
            return;
        }
        
        if (!initialised) {
            Log.d(BuildConfig.LOG_TAG, "Initialising");
            
            vunglePub = VunglePub.getInstance();
            vunglePub.init(
                    activity,
                    appId,
                    new String[] { placementId },
                    MainThread.redirect(
                            new VungleInitListener() {
                                @Override
                                public void onSuccess() {
                                    initialised = true;
                                    Log.d(BuildConfig.LOG_TAG, "Initialised");
                                    
                                    forwarder = new EventForwarder(
                                            placementId,
                                            VungleAdapter.this,
                                            listener);
                                    vunglePub.clearAndSetEventListeners(
                                            MainThread.redirect(
                                                    forwarder,
                                                    VungleAdEventListener.class));
                                    
                                    vunglePub.loadAd(placementId);
                                }
                                
                                @Override
                                public void onFailure(Throwable throwable) {
                                    Log.w(  BuildConfig.LOG_TAG,
                                            "Failed to initialise",
                                            throwable);
                                    listener.onAdFailedToLoad(
                                            VungleAdapter.this,
                                            AdRequestResult.Configuration,
                                            throwable.getMessage());
                                }
                            },
                            VungleInitListener.class));
        } else {
            forwarder.requestPerformed(listener);
        }
    }
    
    @Override
    public void showAd() {
        if (vunglePub != null && vunglePub.isAdPlayable(placementId)) {
            vunglePub.playAd(placementId, null);
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
