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

import android.support.annotation.Nullable;
import android.util.Log;

import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyZone;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.AdShowResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;

class AdColonyEventForwarder extends AdColonyInterstitialListener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    @Nullable
    private AdColonyInterstitial ad;
    
    AdColonyEventForwarder(
            MediationListener listener,
            MediationAdapter adapter) {
        
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onRequestNotFilled(AdColonyZone zone) {
        Log.w(BuildConfig.LOG_TAG, "Request not filled");
        listener.onAdFailedToLoad(
                adapter,
                AdRequestResult.NoFill,
                "AdColony no fill");
    }
    
    @Override
    public void onRequestFilled(AdColonyInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Request filled");
        
        this.ad = ad;
        listener.onAdLoaded(adapter);
    }
    
    @Override
    public void onOpened(AdColonyInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Opened");
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void onClicked(AdColonyInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Clicked");
        listener.onAdClicked(adapter);
    }
    
    @Override
    public void onClosed(AdColonyInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Closed");
        
        if (this.ad != null) {
            this.ad.destroy();
            this.ad = null;
        }
        
        listener.onAdClosed(adapter, true);
    }
    
    @Override
    public void onLeftApplication(AdColonyInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Left application");
        listener.onAdLeftApplication(adapter);
    }
    
    @Nullable
    AdColonyInterstitial getAd() {
        return ad;
    }
    
    void onExpired() {
        Log.d(BuildConfig.LOG_TAG, "Expired");
        listener.onAdFailedToShow(adapter, AdShowResult.EXPIRED);
    }
}
