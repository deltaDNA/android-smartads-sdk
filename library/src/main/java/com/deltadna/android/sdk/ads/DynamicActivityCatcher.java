/*
 * Copyright (c) 2018 deltaDNA Ltd. All rights reserved.
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

package com.deltadna.android.sdk.ads;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

final class DynamicActivityCatcher extends ActivityCatcher {
    
    private static final String TAG = BuildConfig.LOG_TAG
            + ' '
            + DynamicActivityCatcher.class.getSimpleName();
    
    private final Deque<Activity> created = new LinkedList<>();
    
    DynamicActivityCatcher(LifecycleCallbacks listener) {
        super(listener);
    }
    
    @Override
    public void onActivityCreated(Activity activity, Bundle state) {
        created.push(activity);
        Log.d(TAG, "Added " + activity);
    }
    
    @Override
    public void onActivityDestroyed(Activity activity) {
        created.remove(activity);
        Log.d(TAG, "Removed " + activity);
    }
    
    void onAnalyticsStarted() {
        try {
            activity = created.pop();
            Log.d(TAG, "Captured " + activity);
            
            created.clear();
        } catch (NoSuchElementException e) {
            Log.w(TAG, "Failed to retrieve activity");
        }
    }
    
    void onAnalyticsStopped() {
        activity = null;
        created.clear();
        Log.d(TAG, "Released activities");
    }
}
