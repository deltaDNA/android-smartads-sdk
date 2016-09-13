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

package com.deltadna.android.sdk.ads.provider.supersonic;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.supersonic.mediationsdk.logger.SupersonicError;
import com.supersonic.mediationsdk.sdk.InterstitialListener;

import java.util.Locale;

final class SupersonicInterstitialForwarder implements InterstitialListener {
    
    private final SupersonicInterstitialAdapter adapter;
    private final MediationListener listener;
    
    SupersonicInterstitialForwarder(
            SupersonicInterstitialAdapter adapter,
            MediationListener listener) {
        
        this.adapter = adapter;
        this.listener = listener;
    }
    
    @Override
    public void onInterstitialInitSuccess() {
        // nothing to do
    }
    
    @Override
    public void onInterstitialInitFailed(SupersonicError error) {
        Log.w(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Interstitial init fail with code %d and message %s",
                error.getErrorCode(),
                error.getErrorMessage()));
        
        final AdRequestResult result;
        switch (error.getErrorCode()) {
            case SupersonicError.ERROR_CODE_NO_ADS_TO_SHOW:
            case SupersonicError.ERROR_REACHED_CAP_LIMIT:
                result = AdRequestResult.NoFill;
                break;
            
            case SupersonicError.ERROR_CODE_NO_CONFIGURATION_AVAILABLE:
            case SupersonicError.ERROR_CODE_USING_CACHED_CONFIGURATION:
            case SupersonicError.ERROR_CODE_KEY_NOT_SET:
            case SupersonicError.ERROR_CODE_INVALID_KEY_VALUE:
                result = AdRequestResult.Configuration;
                break;
            
            default:
                result = AdRequestResult.Error;
                Log.w(  BuildConfig.LOG_TAG,
                        "Unknown case: " + error.getErrorCode());
        }
        
        listener.onAdFailedToLoad(adapter, result, error.getErrorMessage());
    }
    
    @Override
    public void onInterstitialReady() {
        listener.onAdLoaded(adapter);
    }
    
    @Override
    public void onInterstitialLoadFailed(SupersonicError error) {
        // the errors are the same, so use the same logic
        onInterstitialInitFailed(error);
    }
    
    @Override
    public void onInterstitialOpen() {}
    
    @Override
    public void onInterstitialShowSuccess() {
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void onInterstitialShowFailed(SupersonicError error) {
        Log.w(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Interstitial show fail with code %d and message %s",
                error.getErrorCode(),
                error.getErrorMessage()));
        
        listener.onAdFailedToShow(adapter, AdClosedResult.ERROR);
    }
    
    @Override
    public void onInterstitialClick() {
        listener.onAdClicked(adapter);
    }
    
    @Override
    public void onInterstitialClose() {
        listener.onAdClosed(adapter, true);
    }
}
