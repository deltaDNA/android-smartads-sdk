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

package com.deltadna.android.sdk.ads.provider.adcolony;

import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.jirbo.adcolony.AdColonyAdAvailabilityListener;

final class AdColonyAvailabilityMonitor implements AdColonyAdAvailabilityListener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    private boolean available;
    private String reason = "";
    
    @Nullable
    private AdColonyEventForwarder forwarder;
    
    AdColonyAvailabilityMonitor(
            MediationListener listener,
            MediationAdapter adapter) {
        
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onAdColonyAdAvailabilityChange(boolean available, String reason) {
        /*
         * This callback method is called with a false availability when an
         * ad is just being shown, so we override the value based on the
         * same criteria.
         */
        this.available = available
                || (forwarder != null && forwarder.isShowing());
        this.reason = reason;
        
        if(this.available) {
            Log.d(BuildConfig.LOG_TAG, "AdColonyAd loaded");
            listener.onAdLoaded(adapter);
        } else {
            Log.d(BuildConfig.LOG_TAG, "AdColony Ad failed to load");
            listener.onAdFailedToLoad(adapter, AdRequestResult.Error, reason);
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public String getReason() {
        return reason;
    }
    
    void setForwarder(@Nullable AdColonyEventForwarder forwarder) {
        this.forwarder = forwarder;
    }
}
