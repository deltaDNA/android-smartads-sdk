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
import com.deltadna.android.sdk.ads.bindings.AdShowResult;
import com.deltadna.android.sdk.ads.bindings.MainThread;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;
import com.vungle.warren.InitCallback;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.Vungle;

import org.json.JSONObject;

public final class VungleAdapter extends MediationAdapter {
    
    private static final String CONSENT_VERSION = "1";
    
    private final String appId;
    private final String placementId;
    
    @Nullable
    private MediationListener listener;
    
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
        
        if (!Vungle.isInitialized()) {
            Vungle.init(
                    appId,
                    activity.getApplicationContext(),
                    new Callback(listener));
        } else {
            switch (Vungle.getConsentStatus()) {
                case OPTED_IN:
                    if (!privacy.userConsent) Vungle.updateConsentStatus(
                            Vungle.Consent.OPTED_OUT,
                            CONSENT_VERSION);
                    break;
                    
                case OPTED_OUT:
                    if (privacy.userConsent) Vungle.updateConsentStatus(
                            Vungle.Consent.OPTED_IN,
                            CONSENT_VERSION);
                    break;
            }
            
            requestAd(listener);
        }
    }
    
    @Override
    public void showAd() {
        if (Vungle.isInitialized()) {
            if (Vungle.canPlayAd(placementId)) {
                Vungle.playAd(
                        placementId,
                        null,
                        MainThread.redirect(
                                new PlayEventForwarder(placementId, this, listener),
                                PlayAdCallback.class));
            } else if (listener != null) {
                listener.onAdFailedToShow(this, AdShowResult.NOT_LOADED);
            } else {
                Log.w(BuildConfig.LOG_TAG, "Can't play ad and listener is null");
            }
        } else if (listener != null) {
            listener.onAdFailedToShow(this, AdShowResult.ERROR);
        } else {
            Log.w(BuildConfig.LOG_TAG, "SDK not initialised and listener is null");
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
        listener = null;
    }
    
    @Override
    public void onPause() {}
    
    @Override
    public void onResume() {}
    
    @Override
    public boolean isGdprCompliant() {
        return true;
    }
    
    private void requestAd(MediationListener listener) {
        this.listener = listener;
        
        if (Vungle.isInitialized()) {
            Vungle.loadAd(
                    placementId,
                    MainThread.redirect(
                            new LoadEventForwarder(placementId, this, listener),
                            LoadAdCallback.class));
        } else {
            listener.onAdFailedToLoad(
                    this,
                    AdRequestResult.Error,
                    "SDK not initialised");
        }
    }
    
    private final class Callback implements InitCallback {
        
        private final MediationListener listener;
        
        Callback(MediationListener listener) {
            this.listener = listener;
        }
        
        @Override
        public void onSuccess() {
            Log.v(BuildConfig.LOG_TAG, "SDK initialised");
            
            Vungle.updateConsentStatus(
                    privacy.userConsent
                            ? Vungle.Consent.OPTED_IN
                            : Vungle.Consent.OPTED_OUT,
                    CONSENT_VERSION);
            
            requestAd(listener);
        }
        
        @Override
        public void onError(Throwable throwable) {
            Log.w(BuildConfig.LOG_TAG, "Failed to initialise SDK", throwable);
            listener.onAdFailedToLoad(
                    VungleAdapter.this,
                    AdRequestResult.Configuration,
                    throwable.getMessage());
        }
        
        @Override
        public void onAutoCacheAdAvailable(String s) {}
    }
}
