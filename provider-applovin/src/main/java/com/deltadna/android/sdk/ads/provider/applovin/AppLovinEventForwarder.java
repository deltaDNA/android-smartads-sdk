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

import android.support.annotation.Nullable;
import android.util.Log;

import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinErrorCodes;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;

final class AppLovinEventForwarder implements
        AppLovinAdLoadListener,
        AppLovinAdDisplayListener,
        AppLovinAdVideoPlaybackListener,
        AppLovinAdClickListener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    @Nullable
    private PollingLoadChecker checker;
    
    private boolean notified;
    private boolean complete;
    
    AppLovinEventForwarder(MediationListener listener, MediationAdapter adapter) {
        this.listener = listener;
        this.adapter = adapter;
    }
    
    void setChecker(@Nullable PollingLoadChecker checker) {
        this.checker = checker;
    }
    
    @Override
    public void adReceived(AppLovinAd appLovinAd) {
        Log.d(BuildConfig.LOG_TAG, "Ad received");
        
        if (checker != null) {
            checker.stop();
        }
        if (!notified) {
            listener.onAdLoaded(adapter);
            notified = true;
        }
    }
    
    @Override
    public void failedToReceiveAd(int i) {
        Log.w(BuildConfig.LOG_TAG, "Failed to receive ad: " + i);
        
        final AdRequestResult result;
        switch (i) {
            case AppLovinErrorCodes.FETCH_AD_TIMEOUT:
            case AppLovinErrorCodes.NO_NETWORK:
            case AppLovinErrorCodes.INCENTIVIZED_SERVER_TIMEOUT:
                result = AdRequestResult.Network;
                break;
            
            case AppLovinErrorCodes.NO_FILL:
                result = AdRequestResult.NoFill;
                break;
            
            case AppLovinErrorCodes.UNABLE_TO_RENDER_AD:
            case AppLovinErrorCodes.UNSPECIFIED_ERROR:
            case AppLovinErrorCodes.INCENTIVIZED_NO_AD_PRELOADED:
            case AppLovinErrorCodes.INCENTIVIZED_UNKNOWN_SERVER_ERROR:
            case AppLovinErrorCodes.INCENTIVIZED_USER_CLOSED_VIDEO:
            case AppLovinErrorCodes.INVALID_URL:
            case AppLovinErrorCodes.UNABLE_TO_PRECACHE_RESOURCES:
            case AppLovinErrorCodes.UNABLE_TO_PRECACHE_IMAGE_RESOURCES:
            case AppLovinErrorCodes.UNABLE_TO_PRECACHE_VIDEO_RESOURCES:
                result = AdRequestResult.Error;
                break;
            
            default:
                Log.w(BuildConfig.LOG_TAG, "Unknown case: " + i);
                result = AdRequestResult.Error;
        }
        
        if (checker != null) {
            checker.stop();
        }
        
        listener.onAdFailedToLoad(
                adapter,
                result,
                "Failed to receive AppLovin ad: " + i);
    }
    
    @Override
    public void adDisplayed(AppLovinAd appLovinAd) {
        Log.d(BuildConfig.LOG_TAG, "Ad displayed");
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void adHidden(AppLovinAd appLovinAd) {
        Log.d(BuildConfig.LOG_TAG, "Ad hidden");
        listener.onAdClosed(adapter, complete);
    }
    
    @Override
    public void videoPlaybackBegan(AppLovinAd appLovinAd) {
        Log.d(BuildConfig.LOG_TAG, "Video playback began");
        complete = false;
    }
    
    @Override
    public void videoPlaybackEnded(AppLovinAd appLovinAd, double v, boolean b) {
        Log.d(BuildConfig.LOG_TAG, "Video playback ended");
        complete = b;
    }
    
    @Override
    public void adClicked(AppLovinAd appLovinAd) {
        Log.d(BuildConfig.LOG_TAG, "Ad clicked");
        listener.onAdClicked(adapter);
    }
}
