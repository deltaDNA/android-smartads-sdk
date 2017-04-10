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

import android.app.Activity;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryAgentListener;

final class FlurryHelper {
    
    private static boolean initialised;
    
    static void initialise(Activity activity, String apiKey, boolean logging) {
        if (!initialised) {
            new FlurryAgent.Builder()
                    .withListener(new FlurryAgentListener() {
                        @Override
                        public void onSessionStarted() {
                            // logs an error otherwise...
                        }
                    })
                    .withLogEnabled(logging)
                    .withLogLevel(logging ? Log.VERBOSE : Log.WARN)
                    .build(activity.getApplicationContext(), apiKey);
            
            initialised = true;
        }
    }
    
    static boolean isInitialised() {
        return initialised;
    }
}
