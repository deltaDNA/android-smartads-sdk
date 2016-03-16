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

package com.deltadna.android.sdk.ads.core.network;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationListener;

public class DummyMediationInterstitialEventForwarder extends DummyListener {

    private MediationListener mediationListener;
    private DummyAdapter adapter;

    public DummyMediationInterstitialEventForwarder(MediationListener listener, DummyAdapter adapter) {
        this.mediationListener = listener;
        this.adapter = adapter;
    }

    @Override
    public void onAdReady() {
        mediationListener.onAdLoaded(adapter);
    }

    @Override
    public void onAdFailed(int errorCode) {

        // Translate error codes
        switch (errorCode) {
            case DummyInterstitial.BAD_REQUEST:
                mediationListener.onAdFailedToLoad(adapter, AdRequestResult.Error, "Dummy error: " + errorCode);
                break;
            default:
                mediationListener.onAdFailedToLoad(adapter, AdRequestResult.Error, "Dummy error: " + errorCode);
                break;
        }
    }

    @Override
    public void onAdClosed() {
        mediationListener.onAdClosed(adapter, true);
    }
}
