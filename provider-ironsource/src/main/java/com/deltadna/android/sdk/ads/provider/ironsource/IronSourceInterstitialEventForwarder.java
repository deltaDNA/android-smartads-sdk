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

package com.deltadna.android.sdk.ads.provider.ironsource;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.AdShowResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.InterstitialListener;

final class IronSourceInterstitialEventForwarder implements InterstitialListener {
    
    private final MediationAdapter adapter;
    private final MediationListener listener;
    
    IronSourceInterstitialEventForwarder(
            MediationAdapter adapter,
            MediationListener listener) {
        
        this.adapter = adapter;
        this.listener = listener;
    }
    
    @Override
    public void onInterstitialAdReady() {
        Log.d(BuildConfig.LOG_TAG, "Interstitial ad ready");
        listener.onAdLoaded(adapter);
    }
    
    @Override
    public void onInterstitialAdLoadFailed(IronSourceError error) {
        Log.w(BuildConfig.LOG_TAG, "Interstitial ad load failed: " + error);
        
        final AdRequestResult result;
        switch (error.getErrorCode()) {
            case IronSourceError.ERROR_CODE_NO_ADS_TO_SHOW:
                result = AdRequestResult.NoFill;
                break;
            
            case IronSourceError.ERROR_NO_INTERNET_CONNECTION:
                result = AdRequestResult.Network;
                break;
            
            case IronSourceError.ERROR_CODE_INIT_FAILED:
                result = AdRequestResult.Loaded;
                break;
                
            case IronSourceError.ERROR_CODE_INVALID_KEY_VALUE:
            case IronSourceError.ERROR_CODE_NO_CONFIGURATION_AVAILABLE:
                result = AdRequestResult.Configuration;
                break;
            
            default:
                result = AdRequestResult.Error;
        }
        
        listener.onAdFailedToLoad(adapter, result, error.getErrorMessage());
    }
    
    @Override
    public void onInterstitialAdOpened() {
        Log.d(BuildConfig.LOG_TAG, "Interstitial ad opened");
    }
    
    @Override
    public void onInterstitialAdShowFailed(IronSourceError error) {
        Log.w(BuildConfig.LOG_TAG, "Interstitial ad show failed: " + error);
        listener.onAdFailedToShow(adapter, AdShowResult.ERROR);
    }
    
    @Override
    public void onInterstitialAdShowSucceeded() {
        Log.d(BuildConfig.LOG_TAG, "Interstitial ad show succeeded");
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void onInterstitialAdClicked() {
        Log.d(BuildConfig.LOG_TAG, "Interstitial ad clicked");
        listener.onAdClicked(adapter);
    }
    
    @Override
    public void onInterstitialAdClosed() {
        Log.d(BuildConfig.LOG_TAG, "Interstitial ad closed");
        listener.onAdClosed(adapter, true);
    }
}
