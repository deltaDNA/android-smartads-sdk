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
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.Nullable;

abstract class ActivityCatcher implements Application.ActivityLifecycleCallbacks {
    
    private final LifecycleCallbacks listener;
    
    @Nullable
    protected Activity activity;
    
    ActivityCatcher(LifecycleCallbacks listener) {
        this.listener = listener;
    }
    
    @Override
    public abstract void onActivityCreated(Activity activity, Bundle state);
    
    @Override
    public void onActivityStarted(Activity activity) {}
    
    @Override
    public void onActivityResumed(Activity activity) {
        if (activity.equals(getActivity())) listener.onResumed();
    }
    
    @Override
    public void onActivityPaused(Activity activity) {
        if (activity.equals(getActivity())) listener.onPaused();
    }
    
    @Override
    public void onActivityStopped(Activity activity) {}
    
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle state) {}
    
    @Override
    public abstract void onActivityDestroyed(Activity activity);
    
    void onAnalyticsStarted() {}
    
    void onAnalyticsStopped() {}
    
    @Nullable
    final Activity getActivity() {
        return activity;
    }
    
    interface LifecycleCallbacks {
        
        void onResumed();
        void onPaused();
    }
}
