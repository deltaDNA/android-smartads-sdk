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

package com.deltadna.android.sdk.ads.provider.admob;

import android.content.Context;

import com.google.android.gms.ads.MobileAds;

class InitialisationHelper {
    
    static final String NON_PERSONALISED_ADS = "npa";
    
    private static boolean initialised;
    
    static void initialise(Context context, String appId) {
        synchronized (InitialisationHelper.class) {
            if (!initialised) {
                MobileAds.initialize(context, appId);
                initialised = true;
            }
        }
    }
}
