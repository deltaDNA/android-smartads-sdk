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

package com.deltadna.android.sdk.ads.provider.amazon;

import android.util.Log;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.DefaultAdListener;
import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;

import java.util.Locale;

final class AmazonEventForwarder extends DefaultAdListener {
    
    private MediationListener listener;
    private MediationAdapter adapter;
    
    AmazonEventForwarder(MediationListener listener, MediationAdapter adapter) {
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onAdLoaded(Ad ad, AdProperties adProperties) {
        super.onAdLoaded(ad, adProperties);
        Log.d(BuildConfig.LOG_TAG, "Amazon Ad loaded");
        listener.onAdLoaded(adapter);
    }

    @Override
    public void onAdFailedToLoad(Ad ad, AdError error) {
        super.onAdFailedToLoad(ad, error);
        Log.w(BuildConfig.LOG_TAG, "Ad failed to load");
        
        final AdRequestResult adStatus;
        switch (error.getCode()) {
            case NETWORK_ERROR:
                adStatus = AdRequestResult.Network;
                break;
            case NETWORK_TIMEOUT:
                adStatus = AdRequestResult.Network;
                break;
            
            case NO_FILL:
                adStatus = AdRequestResult.NoFill;
                break;
            
            case INTERNAL_ERROR:
                adStatus = AdRequestResult.Error;
                break;
            
            case REQUEST_ERROR:
                adStatus = AdRequestResult.Configuration;
                break;
            
            default:
                Log.w(BuildConfig.LOG_TAG, "Unknown case: " + error.getCode());
                adStatus = AdRequestResult.Error;
        }
        
        listener.onAdFailedToLoad(
                adapter,
                adStatus,
                String.format(
                        Locale.US,
                        "Amazon error code: %s / %s",
                        error.getCode(),
                        error.getMessage()));
    }

    @Override
    public void onAdExpanded(Ad ad) {
        super.onAdExpanded(ad);
        listener.onAdClicked(adapter);
    }

    @Override
    public void onAdDismissed(Ad ad) {
        super.onAdDismissed(ad);
        listener.onAdClosed(adapter, true);
    }

    @Override
    public void onAdExpired(Ad ad) {
        super.onAdExpired(ad);
        listener.onAdFailedToShow(adapter, AdClosedResult.EXPIRED);
    }
}
