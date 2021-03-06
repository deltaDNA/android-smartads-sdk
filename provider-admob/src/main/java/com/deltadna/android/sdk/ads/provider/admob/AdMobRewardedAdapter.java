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

package com.deltadna.android.sdk.ads.provider.admob;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdShowResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;

import org.json.JSONObject;

public final class AdMobRewardedAdapter extends MediationAdapter {
    
    private final String appId;
    private final String adUnitId;
    private final boolean testMode;
    
    @Nullable
    private Activity activity;
    @Nullable
    private MediationListener listener;
    @Nullable
    private RewardedVideoAd ad;
    
    public AdMobRewardedAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex,
            String appId,
            String adUnitId,
            boolean testMode) {
        
        super(  eCPM,
                demoteOnCode,
                privacy,
                waterfallIndex);
        
        this.appId = appId;
        this.adUnitId = testMode
                ? "ca-app-pub-3940256099942544/5224354917"
                : adUnitId;
        this.testMode = testMode;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        this.activity = activity;
        this.listener = listener;
        
        InitialisationHelper.initialise(activity, appId);
        
        ad = MobileAds.getRewardedVideoAdInstance(activity);
        ad.setRewardedVideoAdListener(new RewardedEventForwarder(this, listener));
        
        final AdRequest.Builder request = new AdRequest.Builder();
        if (testMode) request.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        
        final Bundle extras = new Bundle(1);
        extras.putString(
                InitialisationHelper.NON_PERSONALISED_ADS,
                privacy.userConsent ? "0" : "1");
        request.addNetworkExtrasBundle(AdMobAdapter.class, extras);
        request.tagForChildDirectedTreatment(privacy.ageRestricted);
        
        ad.loadAd(adUnitId, request.build());
    }
    
    @Override
    public void showAd() {
        if (ad != null) {
            if (ad.isLoaded()) {
                ad.show();
                
                activity = null;
                listener = null;
                ad = null;
            } else if (listener != null) {
                Log.w(BuildConfig.LOG_TAG, "Ad is not loaded");
                listener.onAdFailedToShow(this, AdShowResult.EXPIRED);
            }
        } else if (listener != null) {
            Log.w(BuildConfig.LOG_TAG, "Ad is null");
            listener.onAdFailedToShow(this, AdShowResult.ERROR);
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
    public void onResume() {
        if (ad != null && activity != null) ad.resume(activity);
    }
    
    @Override
    public void onPause() {
        if (ad != null && activity != null) ad.pause(activity);
    }
    
    @Override
    public void onDestroy() {
        if (ad != null && activity != null) ad.destroy(activity);
    }
    
    @Override
    public boolean isGdprCompliant() {
        return true;
    }
}
