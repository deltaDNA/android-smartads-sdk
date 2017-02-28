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

package com.deltadna.android.sdk.ads.provider.applovin;

import android.os.Handler;
import android.os.Looper;

import com.applovin.adview.AppLovinInterstitialAdDialog;

final class PollingLoadChecker {
    
    private static final int PERIOD = 500;
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Checker checker = new Checker();
    
    private final AppLovinInterstitialAdDialog ad;
    private final AppLovinEventForwarder forwarder;
    
    PollingLoadChecker(
            AppLovinInterstitialAdDialog ad,
            AppLovinEventForwarder forwarder) {
        
        this.ad = ad;
        this.forwarder = forwarder;
    }
    
    void start() {
        handler.postDelayed(checker, PERIOD);
    }
    
    void stop() {
        handler.removeCallbacks(checker);
    }
    
    private final class Checker implements Runnable {
        
        @Override
        public void run() {
            if (ad.isAdReadyToDisplay()) {
                handler.removeCallbacks(null);
                
                forwarder.setChecker(null);
                forwarder.adReceived(null);
            } else {
                handler.postDelayed(this, PERIOD);
            }
        }
    }
}
