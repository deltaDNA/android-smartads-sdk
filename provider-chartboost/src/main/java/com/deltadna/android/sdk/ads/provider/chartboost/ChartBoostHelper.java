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

package com.deltadna.android.sdk.ads.provider.chartboost;

import android.app.Activity;
import android.util.Log;

import com.chartboost.sdk.Chartboost;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;

final class ChartBoostHelper {
    
    private static final ChartBoostEventForwarder FORWARDER =
            new ChartBoostEventForwarder();
    
    private static boolean initialised;
    
    public static void initialise(Activity activity, String appId, String appSignature) {
        if(!initialised) {
            Chartboost.startWithAppId(activity, appId, appSignature);
            Chartboost.onCreate(activity);
            Chartboost.onStart(activity);
            Chartboost.setDelegate(FORWARDER);
            initialised = true;
            Log.d(BuildConfig.LOG_TAG, "Initialising ChartBoost");
        }
    }

    public static boolean isInitialised() {
        return initialised;
    }

    public static void setRewardedListeners(MediationListener rewardedMediationListener, MediationAdapter rewardedAdMediationAdapter) {
        FORWARDER.setRewardedListeners(rewardedMediationListener, rewardedAdMediationAdapter);
    }

    public static void setInterstitialListeners(MediationListener interstitialMediationListener, MediationAdapter interstitialMediationAdapter) {
        FORWARDER.setInterstitialListeners(interstitialMediationListener, interstitialMediationAdapter);
    }
}
