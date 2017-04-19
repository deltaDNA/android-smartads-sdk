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

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

import java.util.Locale;

final class EventForwarder implements IUnityAdsListener {
    
    private static final short DELAY = 1000;
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    private final MediationAdapter adapter;
    @Nullable
    private MediationListener listener;
    
    @Nullable
    private Boolean available;
    @Nullable
    private UnityAds.UnityAdsError lastError;
    @Nullable
    private String lastMessage;
    
    EventForwarder(
            MediationAdapter adapter,
            @Nullable MediationListener listener) {
        
        this.adapter = adapter;
        this.listener = listener;
    }
    
    @Override
    public void onUnityAdsReady(@Nullable String placementId) {
        Log.d(BuildConfig.LOG_TAG, "Unity ads ready");
        
        available = true;
        lastError = null;
        lastMessage = null;
        
        // this callback may be called multiple times in quick succession
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) listener.onAdLoaded(adapter);
                    }
                },
                DELAY);
    }
    
    @Override
    public void onUnityAdsError(UnityAds.UnityAdsError error, String message) {
        Log.w(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Unity ads error: %s/%s",
                error,
                message));
        
        available = false;
        lastError = error;
        lastMessage = message;
        
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
        
        if (listener != null) listener.onAdFailedToLoad(
                adapter,
                reason,
                message);
    }
    
    @Override
    public void onUnityAdsStart(String placementId) {
        Log.d(BuildConfig.LOG_TAG, "Unity ads start: " + placementId);
        if (listener != null) listener.onAdShowing(adapter);
    }
    
    @Override
    public void onUnityAdsFinish(
            String placementId,
            UnityAds.FinishState state) {
        
        Log.d(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Unity ads finish: %s/%s",
                placementId,
                state));
        
        final boolean complete;
        switch (state) {
            case ERROR:
            case SKIPPED:
                complete = false;
                break;
            
            case COMPLETED:
                complete = true;
                break;
            
            default:
                Log.w(BuildConfig.LOG_TAG, "Unknown case: " + state);
                complete = false;
        }
        
        if (listener != null) {
            listener.onAdClosed(adapter, complete);
            // avoid any further calls which Unity does
            listener = null;
        }
    }
    
    void requestPerformed(
            MediationListener listener,
            @Nullable String placementId) {
        
        this.listener = listener;
        
        if (available != null) {
            if (available) {
                onUnityAdsReady(placementId);
            } else {
                onUnityAdsError(lastError, lastMessage);
            }
        }
    }
}
