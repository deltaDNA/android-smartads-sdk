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
import android.text.TextUtils;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.metadata.MetaData;

import org.json.JSONObject;

public final class UnityRewardedAdapter extends MediationAdapter {
    
    private static final String GDPR_CONSENT = "gdpr.consent";
    
    private final String gameId;
    private final String placementId;
    private final boolean testMode;
    
    private boolean initialised;
    private boolean committedConsent;
    
    @Nullable
    private Activity activity;
    @Nullable
    private EventForwarder forwarder;
    
    public UnityRewardedAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex,
            String gameId,
            String placementId,
            boolean testMode) {
        
        super(  eCPM,
                demoteOnCode,
                privacy,
                waterfallIndex);
        
        this.gameId = gameId;
        this.placementId = placementId;
        this.testMode = testMode;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            final MediationListener listener,
            JSONObject configuration) {
        
        if (!initialised) {
            Log.d(BuildConfig.LOG_TAG, "Initialising");
            try {
                forwarder = new EventForwarder(this, placementId, listener);
                
                UnityAds.initialize(
                        activity,
                        gameId,
                        forwarder,
                        testMode);
                
                final MetaData metaData = new MetaData(activity);
                metaData.set(GDPR_CONSENT, privacy.userConsent);
                metaData.commit();
                committedConsent = privacy.userConsent;
                
                initialised = true;
                Log.d(BuildConfig.LOG_TAG, "Initialised");
            } catch (Exception e) {
                Log.w(BuildConfig.LOG_TAG, "Failed to initialise", e);
                listener.onAdFailedToLoad(
                        this,
                        AdRequestResult.Configuration,
                        "Invalid Unity configuration: " + e);
                return;
            }
        } else if (committedConsent != privacy.userConsent) {
            final MetaData metaData = new MetaData(activity);
            metaData.set(GDPR_CONSENT, privacy.userConsent);
            metaData.commit();
            committedConsent = privacy.userConsent;
        }
        
        this.activity = activity;
        
        if (forwarder != null && !TextUtils.isEmpty(placementId)) {
            forwarder.requestPerformed(listener);
        }
    }
    
    @Override
    public void showAd() {
        if (UnityAds.isReady(placementId) && activity != null) {
            UnityAds.show(activity, placementId);
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
