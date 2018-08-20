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

package com.deltadna.android.sdk.ads.provider.loopme;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.loopme.LoopMeInterstitial;
import com.loopme.common.LoopMeError;

import java.util.Locale;

final class EventForwarder implements LoopMeInterstitial.Listener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    private boolean complete;
    private boolean expired;
    
    EventForwarder(
            MediationListener listener,
            MediationAdapter adapter) {
        
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onLoopMeInterstitialLoadSuccess(LoopMeInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Interstitial load success: " + ad);
        listener.onAdLoaded(adapter);
    }
    
    @Override
    public void onLoopMeInterstitialLoadFail(
            LoopMeInterstitial ad,
            LoopMeError error) {
        
        Log.d(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Interstitial load fail: %s/%s",
                ad,
                error.getMessage()));
        listener.onAdFailedToLoad(
                adapter,
                AdRequestResult.NoFill,
                error.getMessage());
    }
    
    @Override
    public void onLoopMeInterstitialShow(LoopMeInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Interstitial show: " + ad);
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void onLoopMeInterstitialVideoDidReachEnd(LoopMeInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Interstitial video did reach end: " + ad);
        complete = true;
    }
    
    @Override
    public void onLoopMeInterstitialHide(LoopMeInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Interstitial hide: " + ad);
        listener.onAdClosed(adapter, complete);
    }
    
    @Override
    public void onLoopMeInterstitialClicked(LoopMeInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Interstitial clicked: " + ad);
        listener.onAdClicked(adapter);
    }
    
    @Override
    public void onLoopMeInterstitialLeaveApp(LoopMeInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Interstitial leave app: " + ad);
        listener.onAdLeftApplication(adapter);
    }
    
    @Override
    public void onLoopMeInterstitialExpired(LoopMeInterstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Interstitial expired: " + ad);
        expired = true;
    }
    
    boolean hasExpired() {
        return expired;
    }
}
