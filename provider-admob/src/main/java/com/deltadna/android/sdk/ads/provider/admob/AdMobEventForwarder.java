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

package com.deltadna.android.sdk.ads.provider.admob;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

final class AdMobEventForwarder extends AdListener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    AdMobEventForwarder(MediationListener listener, MediationAdapter adapter) {
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onAdClosed() {
        super.onAdClosed();
        listener.onAdClosed(adapter, true);
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        super.onAdFailedToLoad(errorCode);
        Log.w(BuildConfig.LOG_TAG, "Ad failed to load");
        
        final AdRequestResult adStatus;
        switch(errorCode) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                adStatus = AdRequestResult.Error;
                break;
            
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                adStatus = AdRequestResult.Configuration;
                break;
            
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                adStatus = AdRequestResult.Network;
                break;
            
            case AdRequest.ERROR_CODE_NO_FILL:
                adStatus = AdRequestResult.NoFill;
                break;
            
            default:
                Log.w(BuildConfig.LOG_TAG, "Not handled: " + errorCode);
                adStatus = AdRequestResult.Error;
        }
        
        listener.onAdFailedToLoad(
                adapter, adStatus, "AdMob error code: " + errorCode);
    }

    @Override
    public void onAdLeftApplication() {
        super.onAdLeftApplication();
        listener.onAdLeftApplication(adapter);
    }

    @Override
    public void onAdOpened() {
        super.onAdOpened();
        listener.onAdShowing(adapter);
    }

    @Override
    public void onAdLoaded() {
        super.onAdLoaded();
        Log.d(BuildConfig.LOG_TAG, "Ad loaded");
        listener.onAdLoaded(adapter);
    }
}
