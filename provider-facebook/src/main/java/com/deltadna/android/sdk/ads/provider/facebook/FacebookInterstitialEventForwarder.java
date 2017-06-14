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

package com.deltadna.android.sdk.ads.provider.facebook;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;

final class FacebookInterstitialEventForwarder implements InterstitialAdListener {
    
    private final MediationAdapter adapter;
    private final MediationListener listener;
    
    FacebookInterstitialEventForwarder(
            MediationAdapter adapter,
            MediationListener listener) {
        
        this.adapter = adapter;
        this.listener = listener;
    }
    
    @Override
    public void onAdLoaded(Ad ad) {
        Log.d(BuildConfig.LOG_TAG, "Ad loaded");
        listener.onAdLoaded(adapter);
    }
    
    @Override
    public void onError(Ad ad, AdError error) {
        Log.w(BuildConfig.LOG_TAG, "Error: " + error);
        
        final AdRequestResult result;
        switch (error.getErrorCode()) {
            case AdError.NO_FILL_ERROR_CODE:
                result = AdRequestResult.NoFill;
                break;
            
            case AdError.NETWORK_ERROR_CODE:
                result = AdRequestResult.Network;
                break;
            
            default:
                result = AdRequestResult.Error;
        }
        
        listener.onAdFailedToLoad(adapter, result, error.getErrorMessage());
    }
    
    @Override
    public void onInterstitialDisplayed(Ad ad) {
        Log.d(BuildConfig.LOG_TAG, "Interstitial displayed");
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void onAdClicked(Ad ad) {
        Log.d(BuildConfig.LOG_TAG, "Ad clicked");
        listener.onAdClicked(adapter);
    }
    
    @Override
    public void onInterstitialDismissed(Ad ad) {
        Log.d(BuildConfig.LOG_TAG, "Interstitial dismissed");
        listener.onAdClosed(adapter, true);
    }
    
    @Override
    public void onLoggingImpression(Ad ad) {
        Log.d(BuildConfig.LOG_TAG, "Logging impression");
    }
}
