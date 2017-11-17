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

package com.deltadna.android.sdk.ads.bindings;

import android.content.IntentFilter;

public final class Actions {
    
    private static final String PREFIX = "com.deltadna.android.sdk.ads.ACTION_";
    public static final String SESSION_UPDATED = PREFIX + "SESSION_UPDATED";
    public static final String FAILED_TO_REGISTER = PREFIX + "FAILED_TO_REGISTER";
    public static final String LOADED = PREFIX + "LOADED";
    public static final String SHOWING = PREFIX + "SHOWING";
    public static final String SHOWN = PREFIX + "SHOWN";
    public static final String SHOWN_AND_LOADED = PREFIX + "SHOWN_AND_LOADED";
    
    public static final String REASON = "reason";
    public static final String AGENT = "agent";
    public static final String NETWORK = "network";
    public static final String NETWORK_SHOWN = "network_shown";
    public static final String NETWORK_LOADED = "network_loaded";
    
    public enum Agent { INTERSTITIAL, REWARDED }
    
    public static IntentFilter FILTER = new IntentFilter();
    static {
        FILTER.addAction(SESSION_UPDATED);
        FILTER.addAction(FAILED_TO_REGISTER);
        FILTER.addAction(LOADED);
        FILTER.addAction(SHOWING);
        FILTER.addAction(SHOWN);
        FILTER.addAction(SHOWN_AND_LOADED);
    }
}
