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

package com.deltadna.android.sdk.ads.provider.applovin;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;

import org.json.JSONObject;

public final class AppLovinRewardedAdapter extends MediationAdapter {
    
    private final String key;
    @Nullable
    private final String placement;
    private final boolean verboseLogging;
    
    @Nullable
    private AppLovinSdk sdk;
    
    @Nullable
    private AppLovinInterstitialAdDialog interstitial;
    
    public AppLovinRewardedAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex,
            String key,
            @Nullable String placement,
            boolean verboseLogging) {
        
        super(  eCPM,
                demoteOnCode,
                privacy,
                waterfallIndex);
        
        this.key = key;
        this.placement = placement;
        this.verboseLogging = verboseLogging;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        if (sdk == null) {
            Log.d(BuildConfig.LOG_TAG, "Initialising AppLovin SDK");
            
            final AppLovinSdkSettings settings = new AppLovinSdkSettings();
            settings.setVerboseLogging(verboseLogging);
            
            sdk = AppLovinSdk.getInstance(key, settings, activity);
            sdk.initializeSdk();
            
            AppLovinPrivacySettings.setHasUserConsent(
                    privacy.userConsent, activity);
            AppLovinPrivacySettings.setIsAgeRestrictedUser(
                    privacy.ageRestricted, activity);
            
        } else {
            if (    AppLovinPrivacySettings.hasUserConsent(activity)
                    != privacy.userConsent) {
                AppLovinPrivacySettings.setHasUserConsent(
                        privacy.userConsent, activity);
            }
            if (    AppLovinPrivacySettings.isAgeRestrictedUser(activity)
                    != privacy.ageRestricted) {
                AppLovinPrivacySettings.setIsAgeRestrictedUser(
                        privacy.userConsent, activity);
            }
        }
        
        interstitial = AppLovinInterstitialAd.create(sdk, activity);
        
        if (interstitial != null) {
            final AppLovinEventForwarder forwarder =
                    new AppLovinEventForwarder(listener, this);
            
            interstitial.setAdDisplayListener(forwarder);
            interstitial.setAdVideoPlaybackListener(forwarder);
            interstitial.setAdClickListener(forwarder);
            
            sdk.getAdService().loadNextAd(
                    AppLovinAdSize.INTERSTITIAL,
                    new AppLovinEventForwarder(listener,this));
        }
    }
    
    @Override
    public void showAd() {
        if (interstitial != null && interstitial.isAdReadyToDisplay()) {
            if (TextUtils.isEmpty(placement)) {
                interstitial.show();
            } else {
                interstitial.show(placement);
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
        if (interstitial != null) {
            if (interstitial.isShowing()) {
                interstitial.dismiss();
            }
            
            interstitial = null;
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
