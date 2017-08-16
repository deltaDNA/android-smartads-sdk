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

import android.content.Context;
import android.content.SharedPreferences;

public final class Preferences {
    
    private static final String NAME = "com.deltadna.android.sdk.ads";
    
    private final SharedPreferences preferences;
    
    public Preferences(Context context) {
        preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }
    
    public SharedPreferences get() {
        return preferences;
    }
    
    public SharedPreferences.Editor edit() {
        return preferences.edit();
    }
}
