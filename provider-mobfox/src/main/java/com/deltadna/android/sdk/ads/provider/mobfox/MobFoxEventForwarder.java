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

package com.deltadna.android.sdk.ads.provider.mobfox;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.mobfox.sdk.interstitial.Interstitial;
import com.mobfox.sdk.interstitial.InterstitialListener;

final class MobFoxEventForwarder implements InterstitialListener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    private boolean completed;
    
    MobFoxEventForwarder(MediationListener listener, MediationAdapter adapter) {
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onInterstitialLoaded(Interstitial ad) {
        Log.d(BuildConfig.LOG_TAG, "Interstitial loaded: " + ad);
        listener.onAdLoaded(adapter);
    }
    
    @Override
    public void onInterstitialFailed(String error) {
        Log.w(BuildConfig.LOG_TAG, "Interstitial failed: " + error);
        
        // best guess at figuring out what the error could be...
        final AdRequestResult result;
        final String e = error.toLowerCase();
        if (e.contains("fill") || e.contains("no ad")) {
            result = AdRequestResult.NoFill;
        } else if (e.contains("inventory") || e.contains("hash") || e.contains("invh")) {
            result = AdRequestResult.Configuration;
        } else {
            result = AdRequestResult.Error;
        }
        
        listener.onAdFailedToLoad(adapter, result, error);
    }
    
    @Override
    public void onInterstitialShown() {
        Log.d(BuildConfig.LOG_TAG, "Interstitial shown");
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void onInterstitialClicked() {
        Log.d(BuildConfig.LOG_TAG, "Interstitial clicked");
        listener.onAdClicked(adapter);
    }
    
    @Override
    public void onInterstitialFinished() {
        Log.d(BuildConfig.LOG_TAG, "Interstitial finished");
        completed = true;
    }
    
    @Override
    public void onInterstitialClosed() {
        Log.d(BuildConfig.LOG_TAG, "Interstitial closed");
        listener.onAdClosed(adapter, completed);
    }
}
