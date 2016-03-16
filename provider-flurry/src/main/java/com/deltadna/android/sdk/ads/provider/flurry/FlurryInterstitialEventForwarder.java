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

package com.deltadna.android.sdk.ads.provider.flurry;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.flurry.android.ads.FlurryAdErrorType;
import com.flurry.android.ads.FlurryAdInterstitial;
import com.flurry.android.ads.FlurryAdInterstitialListener;

import java.util.Locale;

final class FlurryInterstitialEventForwarder implements FlurryAdInterstitialListener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    FlurryInterstitialEventForwarder(
            MediationListener listener,
            MediationAdapter adapter) {
        
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onFetched(FlurryAdInterstitial flurryAdInterstitial) {
        Log.d(BuildConfig.LOG_TAG, "Flurry Ad loaded");
        listener.onAdLoaded(adapter);
    }

    @Override
    public void onRendered(FlurryAdInterstitial flurryAdInterstitial) {
    }

    @Override
    public void onDisplay(FlurryAdInterstitial flurryAdInterstitial) {
        listener.onAdShowing(adapter);
    }

    @Override
    public void onClose(FlurryAdInterstitial flurryAdInterstitial) {
        listener.onAdClosed(adapter, true);
    }

    @Override
    public void onAppExit(FlurryAdInterstitial flurryAdInterstitial) {
        listener.onAdLeftApplication(adapter);
    }

    @Override
    public void onClicked(FlurryAdInterstitial flurryAdInterstitial) {
        listener.onAdClicked(adapter);
    }

    @Override
    public void onVideoCompleted(FlurryAdInterstitial flurryAdInterstitial) {
    }

    @Override
    public void onError(FlurryAdInterstitial flurryAdInterstitial, FlurryAdErrorType flurryAdErrorType, int errorCode) {
        Log.d(BuildConfig.LOG_TAG, "Ad error: " + errorCode);
        
        final AdRequestResult adStatus;

        switch (flurryAdErrorType) {
            case FETCH:
                switch (errorCode) {
                    case 1: // No network connectivity - There is no internet connection
                        adStatus = AdRequestResult.Network;
                        break;
                    
                    case 2: // Missing ad controller - Could happen when ad has not been prepared yet
                        adStatus = AdRequestResult.NoFill;
                        break;
                    
                    case 3: // No context - A valid context is missing
                        adStatus = AdRequestResult.Configuration;
                        break;
                    
                    case 4: // Invalid ad unit
                        adStatus = AdRequestResult.Configuration;
                        break;
                    
                    case 17: // Ad not ready - Triggered when you call displayAd() on an ad object that is not ready
                        adStatus = AdRequestResult.NoFill;
                        break;
                    
                    case 18: // Wrong orientation - Device is in wrong orientation for banner or interstitial ads
                        adStatus = AdRequestResult.Configuration;
                        break;
                    
                    case 19: // No view group - Banner ad wasn't placed in a ViewGroup
                        adStatus = AdRequestResult.Configuration;
                        break;
                    
                    case 20: // Ad was unfilled - Ad was unfilled by server. Could be due to incorrect ad request, incorrect ad space configuration or no fill at request location at the moment
                        adStatus = AdRequestResult.NoFill;
                        break;
                    
                    case 21: // Incorrect class for ad space - Ad request made with incorrect class for corresponding ad space
                        adStatus = AdRequestResult.Configuration;
                        break;
                    
                    case 22: // Device locked - Device is locked during ad request
                        adStatus = AdRequestResult.Error;
                        break;
                    
                    default:
                        Log.w(BuildConfig.LOG_TAG, "Unknown case: " + errorCode);
                        adStatus = AdRequestResult.Error;
                }
                
                listener.onAdFailedToLoad(
                        adapter,
                        adStatus,
                        String.format(
                                Locale.US,
                                "Flurry error %d / %s",
                                errorCode,
                                flurryAdErrorType));
                break;
            
            case RENDER:
                listener.onAdFailedToShow(adapter, AdClosedResult.ERROR);
                break;
            
            case CLICK:
                listener.onAdFailedToShow(adapter, AdClosedResult.ERROR);
                break;
            
            default:
                Log.w(BuildConfig.LOG_TAG, "Unknown case: " + flurryAdErrorType);
        }
    }
}
