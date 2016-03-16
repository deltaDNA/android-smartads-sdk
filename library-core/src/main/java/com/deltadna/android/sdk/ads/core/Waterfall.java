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

package com.deltadna.android.sdk.ads.core;

import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.MediationAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class Waterfall {
    
    private final List<MediationAdapter> adapters;
    
    private int index;
    
    Waterfall(List<MediationAdapter> adapters) {
        this.adapters = new ArrayList<>(adapters);
    }
    
    List<MediationAdapter> getAdapters() {
        return adapters;
    }
    
    @Nullable
    MediationAdapter resetAndGetFirst() {
        Log.d(  BuildConfig.LOG_TAG,
                "Resetting waterfall " + Arrays.toString(adapters.toArray()));
        
        Collections.sort(adapters);
        for (int i = 0; i < adapters.size(); i++) {
            adapters.get(i).reset(i);
        }
        
        Log.d(  BuildConfig.LOG_TAG,
                "Reset waterfall " + Arrays.toString(adapters.toArray()));
        
        index = 0;
        return adapters.isEmpty() ? null : adapters.get(index);
    }
    
    @Nullable
    MediationAdapter getNext() {
        return (adapters.size() > ++index) ? adapters.get(index) : null;
    }
    
    @Nullable
    MediationAdapter removeAndGetNext() {
        if (index < adapters.size()) {
            adapters.remove(index);
        } else {
            Log.w(  BuildConfig.LOG_TAG,
                    "Failed to remove adapter at index " + index);
        }
        
        return (adapters.size() > index) ? adapters.get(index) : null;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + '@' + hashCode()
                + '{'
                + Arrays.toString(adapters.toArray())
                + '}';
    }
}
