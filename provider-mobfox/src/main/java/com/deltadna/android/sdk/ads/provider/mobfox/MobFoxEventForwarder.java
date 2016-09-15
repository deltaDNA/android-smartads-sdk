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

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.mobfox.sdk.interstitialads.InterstitialAd;
import com.mobfox.sdk.interstitialads.InterstitialAdListener;

final class MobFoxEventForwarder implements InterstitialAdListener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    MobFoxEventForwarder(MediationListener listener, MediationAdapter adapter) {
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onInterstitialLoaded(InterstitialAd ad) {
        listener.onAdLoaded(adapter);
    }
    
    @Override
    public void onInterstitialFailed(InterstitialAd ad, Exception e) {
        // messages taken from decompiled InterstitialAd class
        final AdRequestResult result;
        switch (e.getMessage()) {
            case "no fill":
            case "no ads in response":
                result = AdRequestResult.NoFill;
                break;
            
            case "please set inventory hash before load()":
                result = AdRequestResult.Configuration;
                break;
            
            default:
                result = AdRequestResult.Error;
        }
        
        listener.onAdFailedToLoad(adapter, result, e.getMessage());
    }
    
    @Override
    public void onInterstitialShown(InterstitialAd ad) {
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void onInterstitialClicked(InterstitialAd ad) {
        listener.onAdClicked(adapter);
    }
    
    @Override
    public void onInterstitialClosed(InterstitialAd ad) {
        listener.onAdClosed(adapter, false); // TODO check
    }
    
    @Override
    public void onInterstitialFinished() {
        listener.onAdClosed(adapter, true);
    }
}
