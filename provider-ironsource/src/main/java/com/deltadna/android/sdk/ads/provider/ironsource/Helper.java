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

package com.deltadna.android.sdk.ads.provider.ironsource;

import android.app.Activity;

import com.deltadna.android.sdk.ads.bindings.Privacy;
import com.ironsource.mediationsdk.IronSource;

class Helper {
    
    private static boolean initialised;
    private static boolean consent;
    
    static void initialise(Activity activity, Privacy privacy, String appKey) {
        synchronized (Helper.class) {
            if (!initialised) {
                IronSource.init(activity, appKey);
                IronSource.setConsent(consent = privacy.userConsent);
                IronSource.setMediationType("DeltaDNA");
                
                initialised = true;
            } else if (consent != privacy.userConsent) {
                IronSource.setConsent(consent = privacy.userConsent);
            }
        }
    }
}
