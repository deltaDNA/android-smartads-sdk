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

package com.deltadna.android.sdk.ads.provider.inmobi;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiInterstitial;

import java.util.Locale;
import java.util.Map;

final class InMobiRewardedEventForwarder implements
        InMobiInterstitial.InterstitialAdListener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    private boolean completed;
    
    InMobiRewardedEventForwarder(
            MediationListener listener,
            MediationAdapter adapter) {
        
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onAdRewardActionCompleted(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
        completed = true;
    }

    @Override
    public void onAdDisplayed(InMobiInterstitial inMobiInterstitial) {
        completed = false;
        listener.onAdShowing(adapter);
    }

    @Override
    public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
        listener.onAdClosed(adapter, completed);
    }

    @Override
    public void onAdInteraction(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
        listener.onAdClicked(adapter);
    }

    @Override
    public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial) {
        Log.d(BuildConfig.LOG_TAG, "Ad load succeeded");
        listener.onAdLoaded(adapter);
    }

    @Override
    public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus inMobiAdRequestStatus) {
        Log.w(BuildConfig.LOG_TAG, "Ad load failed: " + inMobiAdRequestStatus.getMessage());
        
        final AdRequestResult adStatus;
        switch (inMobiAdRequestStatus.getStatusCode()) {
            case NETWORK_UNREACHABLE:
                adStatus = AdRequestResult.Network;
                break;
            
            case NO_FILL:
                adStatus = AdRequestResult.NoFill;
                break;
            
            case REQUEST_INVALID:
                adStatus = AdRequestResult.Error;
                break;
            
            case REQUEST_PENDING:
            case REQUEST_TIMED_OUT:
            case INTERNAL_ERROR:
            case SERVER_ERROR:
            case AD_ACTIVE:
            case EARLY_REFRESH_REQUEST:
                adStatus = AdRequestResult.Error;
                break;
            
            default:
                Log.w(  BuildConfig.LOG_TAG,
                        "Unknown case: " + inMobiAdRequestStatus.getStatusCode());
                adStatus = AdRequestResult.Error;
        }
        
        listener.onAdFailedToLoad(
                adapter,
                adStatus,
                String.format(
                        Locale.US,
                        "InMobi ad load failed: %s / %s",
                        inMobiAdRequestStatus.getStatusCode(),
                        inMobiAdRequestStatus.getMessage()));
    }

    @Override
    public void onUserLeftApplication(InMobiInterstitial inMobiInterstitial) {
        listener.onAdLeftApplication(adapter);
    }
}
