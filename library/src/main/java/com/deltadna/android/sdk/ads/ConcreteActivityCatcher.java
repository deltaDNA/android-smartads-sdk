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

/**
 * {@link ActivityCatcher} which tries to capture a specific {@link Activity}
 * as set by the client during {@link DDNASmartAds} initialisation.
 */
final class ConcreteActivityCatcher extends ActivityCatcher {
    
    private static final String TAG = BuildConfig.LOG_TAG
            + ' '
            + ConcreteActivityCatcher.class.getSimpleName();
    
    private final Class<? extends Activity> activityClass;
    
    ConcreteActivityCatcher(
            LifecycleCallbacks listener,
            Class<? extends Activity> activityClass) {
        
        super(listener);
        
        this.activityClass = activityClass;
    }
    
    @Override
    public void onActivityCreated(Activity activity, Bundle state) {
        if (activityClass.equals(activity.getClass())) {
            this.activity = activity;
            Log.d(TAG, "Captured " + activity);
        }
    }
    
    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity.equals(getActivity())) {
            this.activity = null;
            Log.d(TAG, "Released " + activity);
        }
    }
}
