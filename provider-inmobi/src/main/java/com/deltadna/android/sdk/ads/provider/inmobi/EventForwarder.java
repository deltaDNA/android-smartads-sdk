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

package com.deltadna.android.sdk.ads.provider.inmobi;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.AdShowResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiInterstitial;

import java.util.Locale;
import java.util.Map;

abstract class EventForwarder implements
        InMobiInterstitial.InterstitialAdListener2 {
    
    protected final MediationListener listener;
    protected final MediationAdapter adapter;
    
    EventForwarder(MediationListener listener, MediationAdapter adapter) {
        this.listener = listener;
        this.adapter = adapter;
    }
    
    protected abstract boolean hasCompleted();
    
    @Override
    public void onAdReceived(InMobiInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Ad received: " + ad);
    }
    
    @Override
    public void onAdLoadFailed(
            InMobiInterstitial ad,
            InMobiAdRequestStatus status) {
        
        Log.w(BuildConfig.LOG_TAG, String.format(
                Locale.US, 
                "Ad load failed: %s / %s / %s",
                ad,
                status.getStatusCode(),
                status.getMessage()));
        
        final AdRequestResult adStatus;
        switch (status.getStatusCode()) {
            case NETWORK_UNREACHABLE:
                adStatus = AdRequestResult.Network;
                break;
            
            case NO_FILL:
                adStatus = AdRequestResult.NoFill;
                break;
            
            default:
                adStatus = AdRequestResult.Error;
        }
        
        listener.onAdFailedToLoad(
                adapter,
                adStatus,
                String.format(
                        Locale.US,
                        "InMobi ad load failed: %s / %s",
                        status.getStatusCode(),
                        status.getMessage()));
    }
    
    @Override
    public void onAdLoadSucceeded(InMobiInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Ad load succeeded: " + ad);
        listener.onAdLoaded(adapter);
    }
    
    @Override
    public void onAdWillDisplay(InMobiInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Ad will display: " + ad);
    }
    
    @Override
    public void onAdDisplayed(InMobiInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Ad displayed: " + ad);
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void onAdDisplayFailed(InMobiInterstitial ad) {
        Log.w(BuildConfig.LOG_TAG, "Ad display failed: " + ad);
        listener.onAdFailedToShow(adapter, AdShowResult.ERROR);
    }
    
    @Override
    public void onAdInteraction(InMobiInterstitial ad, Map<Object, Object> map) {
        Log.d(BuildConfig.LOG_TAG, "Ad interaction: " + ad);
        listener.onAdClicked(adapter);
    }
    
    @Override
    public void onAdDismissed(InMobiInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Ad dismissed: " + ad);
        listener.onAdClosed(adapter, hasCompleted());
    }
    
    @Override
    public void onAdRewardActionCompleted(
            InMobiInterstitial ad,
            Map<Object, Object> map) {
        Log.d(BuildConfig.LOG_TAG, "Ad reward action completed: " + ad);
    }
    
    @Override
    public void onUserLeftApplication(InMobiInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "User left application: " + ad);
        listener.onAdLeftApplication(adapter);
    }
}
