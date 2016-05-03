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

package com.deltadna.android.sdk.ads.provider.mopub;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;

final class MoPubEventForwarder implements
        MoPubInterstitial.InterstitialAdListener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    MoPubEventForwarder(MediationListener listener, MediationAdapter adapter) {
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        Log.d(BuildConfig.LOG_TAG, "Interstitial loaded");
        listener.onAdLoaded(adapter);
    }
    
    @Override
    public void onInterstitialFailed(
            MoPubInterstitial interstitial,
            MoPubErrorCode errorCode) {
        
        Log.w(BuildConfig.LOG_TAG, "Interstitial failed: " + errorCode);
        
        final AdRequestResult adStatus;
        switch (errorCode) {
            case NO_FILL:
            case NETWORK_NO_FILL:
                adStatus = AdRequestResult.NoFill;
                break;
            
            case WARMUP:
            case SERVER_ERROR:
            case INTERNAL_ERROR:
            case CANCELLED:
                adStatus = AdRequestResult.Error;
                break;
            
            case NO_CONNECTION:
                adStatus = AdRequestResult.Network;
                break;
            
            case ADAPTER_NOT_FOUND:
            case ADAPTER_CONFIGURATION_ERROR:
                adStatus = AdRequestResult.Configuration;
                break;
            
            case NETWORK_TIMEOUT:
                adStatus = AdRequestResult.Network;
                break;
            
            case NETWORK_INVALID_STATE:
            case MRAID_LOAD_ERROR:
            case VIDEO_CACHE_ERROR:
            case VIDEO_DOWNLOAD_ERROR:
            case VIDEO_NOT_AVAILABLE:
            case VIDEO_PLAYBACK_ERROR:
            case UNSPECIFIED:
                adStatus = AdRequestResult.Error;
                break;
            
            default:
                Log.w(BuildConfig.LOG_TAG, "Unknown case: " + errorCode);
                adStatus = AdRequestResult.Error;
        }
        
        listener.onAdFailedToLoad(
                adapter,
                adStatus,
                "MoPub interstitial failed: " + errorCode);
    }
    
    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        Log.d(BuildConfig.LOG_TAG, "Interstitial shown");
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        Log.d(BuildConfig.LOG_TAG, "Interstitial clicked");
        listener.onAdClicked(adapter);
    }
    
    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        Log.d(BuildConfig.LOG_TAG, "Interstitial dismissed");
        listener.onAdClosed(adapter, true);
    }
}
