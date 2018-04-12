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

package com.deltadna.android.sdk.ads.provider.machinezone;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.AdShowResult;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.fractionalmedia.sdk.AdRequest;
import com.fractionalmedia.sdk.AdZoneError;

import java.util.Locale;

final class EventForwarder {
      
    private final MachineZoneAdapter adapter;
    private final MediationListener listener;
    
    EventForwarder(MachineZoneAdapter adapter, MediationListener listener) {
        this.adapter = adapter;
        this.listener = listener;
    }
    
    void onLoaded(AdRequest request) {
        Log.d(BuildConfig.LOG_TAG, "On loaded: " + request);
        
        adapter.onAdLoaded(request);
        listener.onAdLoaded(adapter);
    }
    
    void onFailed(AdRequest request, AdZoneError error) {
        Log.w(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "On failed: %s/%s",
                request,
                error));
        
        final AdRequestResult result;
        switch (error) {
            case E_30500:
            case E_30501:
                listener.onAdFailedToShow(adapter, AdShowResult.ERROR);
                return;
                
            case E_30502:
                listener.onAdFailedToShow(adapter, AdShowResult.EXPIRED);
                return;
                
            case E_30000:
            case E_30401:
                result = AdRequestResult.NoFill;
                break;
                
            case E_30101:
            case E_30102:
            case E_30103:
                result = AdRequestResult.Configuration;
                break;
                
            case E_30403:
            case E_30600:
            case E_30603:
                result = AdRequestResult.Network;
                break;
                
            default:
                result = AdRequestResult.Error;
        }
        
        listener.onAdFailedToLoad(adapter, result, error.toString());
    }
    
    void onExpanded(AdRequest request) {
        Log.d(BuildConfig.LOG_TAG, "On expanded: " + request);
    }
    
    void onClicked(AdRequest request) {
        Log.d(BuildConfig.LOG_TAG, "On clicked: " + request);
        listener.onAdClicked(adapter);
    }
    
    void onCollapsed(AdRequest request, boolean complete) {
        Log.d(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "On collapsed: %s/%s",
                request,
                complete));
        listener.onAdClosed(adapter, complete);
    }
}
