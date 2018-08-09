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

package com.deltadna.android.sdk.ads.provider.admob;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

final class RewardedEventForwarder implements RewardedVideoAdListener {
    
    private final MediationAdapter adapter;
    private final MediationListener listener;
    
    private boolean complete;
    
    RewardedEventForwarder(
            MediationAdapter adapter,
            MediationListener listener) {
        
        this.adapter = adapter;
        this.listener = listener;
    }
    
    @Override
    public void onRewardedVideoAdLoaded() {
        Log.d(BuildConfig.LOG_TAG, "Rewarded video ad loaded");
        listener.onAdLoaded(adapter);
    }
    
    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        Log.d(BuildConfig.LOG_TAG, "Rewarded video ad failed to load: " + i);
        
        final AdRequestResult result;
        switch (i) {
            case AdRequest.ERROR_CODE_NO_FILL:
                result = AdRequestResult.NoFill;
                break;
            
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                result = AdRequestResult.Network;
                break;
            
            default:
                result = AdRequestResult.Error;
        }
        
        listener.onAdFailedToLoad(adapter, result, "Error code " + i);
    }
    
    @Override
    public void onRewardedVideoAdOpened() {
        Log.d(BuildConfig.LOG_TAG, "Rewarded video ad opened");
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void onRewardedVideoStarted() {
        Log.d(BuildConfig.LOG_TAG, "Rewarded video started");
    }
    
    @Override
    public void onRewardedVideoCompleted() {
        Log.d(BuildConfig.LOG_TAG, "Rewarded video completed");
    }
    
    @Override
    public void onRewarded(RewardItem rewardItem) {
        Log.d(BuildConfig.LOG_TAG, "Rewarded: " + rewardItem);
        complete = true;
    }
    
    @Override
    public void onRewardedVideoAdClosed() {
        Log.d(BuildConfig.LOG_TAG, "Rewarded video ad closed");
        listener.onAdClosed(adapter, complete);
    }
    
    @Override
    public void onRewardedVideoAdLeftApplication() {
        Log.d(BuildConfig.LOG_TAG, "Rewarded video ad left application");
        listener.onAdLeftApplication(adapter);
    }
}
