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

package com.deltadna.android.sdk.ads.provider.unity;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

final class UnityRewardedEventForwarder implements IUnityAdsListener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    UnityRewardedEventForwarder(
            MediationListener listener,
            MediationAdapter adapter) {
        
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onUnityAdsReady(String placementId) {
        Log.d(BuildConfig.LOG_TAG, "Ad ready");
        listener.onAdLoaded(adapter);
    }
    
    @Override
    public void onUnityAdsError(UnityAds.UnityAdsError error, String message) {
        Log.w(BuildConfig.LOG_TAG, "Ad error");
        
        final AdRequestResult reason;
        switch (error) {
            case NOT_INITIALIZED:
            case INITIALIZE_FAILED:
            case INVALID_ARGUMENT:
            case INIT_SANITY_CHECK_FAIL:
                reason = AdRequestResult.Configuration;
                break;
            
            default:
                reason = AdRequestResult.Error;
        }
        
        listener.onAdFailedToLoad(
                adapter,
                reason,
                message);
    }
    
    @Override
    public void onUnityAdsStart(String placementId) {
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void onUnityAdsFinish(
            String placementId,
            UnityAds.FinishState state) {
        
        switch (state) {
            case ERROR:
            case SKIPPED:
                listener.onAdClosed(adapter, false);
                break;
            
            case COMPLETED:
                listener.onAdClosed(adapter, true);
                break;
        }
    }
}
