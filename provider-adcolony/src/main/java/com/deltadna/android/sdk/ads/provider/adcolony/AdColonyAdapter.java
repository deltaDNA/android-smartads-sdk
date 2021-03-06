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
import android.support.annotation.Nullable;
import android.util.Log;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAppOptions;
import com.adcolony.sdk.AdColonyInterstitial;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;

import org.json.JSONObject;

public final class AdColonyAdapter extends MediationAdapter {
    
    private static boolean initialised;
    
    private final String appId;
    private final String[] zoneIds;
    private final boolean testMode;
    
    @Nullable
    private AdColonyEventForwarder forwarder;
    
    public AdColonyAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex,
            String appId,
            String zoneIds,
            boolean testMode) {
        
        super(  eCPM,
                demoteOnCode,
                privacy,
                waterfallIndex);
        
        this.appId = appId;
        this.zoneIds = zoneIds.split(",");
        this.testMode = testMode;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        if (!initialised) {
            initialised = AdColony.configure(
                    activity,
                    new AdColonyAppOptions()
                            .setGDPRRequired(true)
                            .setGDPRConsentString(privacy.userConsent ? "1" : "0")
                            .setTestModeEnabled(testMode)
                            .setMediationNetwork("DeltaDNA", BuildConfig.VERSION_NAME),
                    appId,
                    zoneIds);
        } else {
            final AdColonyAppOptions options = AdColony.getAppOptions();
            if (options.getGDPRConsentString().equals("1") && !privacy.userConsent) {
                AdColony.setAppOptions(options.setGDPRConsentString("0"));
            } else if (options.getGDPRConsentString().equals("0") && privacy.userConsent) {
                AdColony.setAppOptions(options.setGDPRConsentString("1"));
            }
        }
        
        if (!initialised) {
            Log.w(BuildConfig.LOG_TAG, "Not initialised");
            listener.onAdFailedToLoad(
                    this,
                    AdRequestResult.Configuration,
                    "Failed to initialise AdColony");
        } else {
            forwarder = new AdColonyEventForwarder(listener, this);
            if (!AdColony.requestInterstitial(zoneIds[0], forwarder)) {
                Log.w(BuildConfig.LOG_TAG, "Requesting interstitial failed");
                listener.onAdFailedToLoad(
                        this,
                        AdRequestResult.Error,
                        "Failed to request interstitial from AdColony");
            }
        }
    }
    
    @Override
    public void showAd() {
        if (forwarder != null && forwarder.getAd() != null) {
            final AdColonyInterstitial ad = forwarder.getAd();
            
            if (ad.isExpired()) {
                forwarder.onExpired();
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
        if (forwarder != null && forwarder.getAd() != null) {
            forwarder.getAd().destroy();
        }
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
