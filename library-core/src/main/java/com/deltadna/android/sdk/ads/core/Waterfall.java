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

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.core.utils.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class Waterfall {
    
    private static final String TAG = BuildConfig.LOG_TAG
            + ' '
            + Waterfall.class.getSimpleName();
    
    final List<MediationAdapter> adapters;
    private final int maxRequests;
    
    private int position;
    
    Waterfall(List<MediationAdapter> adapters, int maxRequests) {
        this.adapters = new ArrayList<>(adapters);
        this.maxRequests = maxRequests;
    }
    
    @Nullable
    MediationAdapter resetAndGetFirst() {
        Log.d(TAG, "Resetting waterfall " + Arrays.toString(adapters.toArray()));
        
        Collections.sort(adapters);
        for (int i = 0; i < adapters.size(); i++) {
            adapters.get(i).reset(i);
        }
        
        Log.d(TAG, "Reset waterfall " + Arrays.toString(adapters.toArray()));
        
        position = 0;
        return adapters.isEmpty() ? null : adapters.get(position);
    }
    
    @Nullable
    MediationAdapter getNext() {
        return (position + 1 < adapters.size())
                ? adapters.get(++position)
                : null;
    }
    
    void score(MediationAdapter adapter, AdRequestResult result) {
        Log.d(TAG, String.format(
                Locale.US,
                "Updating %s score due to %s",
                adapter,
                result));
        adapter.updateScore(result);
        
        if (result.remove()) {
            Log.d(TAG, "Removing " + adapter);
            remove(adapter);
        }
        
        if (result == AdRequestResult.Loaded) {
            adapter.incrementRequests();
            
            if (maxRequests > 0 && adapter.getRequests() >= maxRequests) {
                Log.d(TAG, String.format(
                        Locale.US,
                        "Updating %s score due to %s",
                        adapter,
                        AdRequestResult.MaxRequests));
                adapter.updateScore(AdRequestResult.MaxRequests);
            }
        }
    }
    
    void remove(MediationAdapter adapter) {
        final int index = adapters.indexOf(adapter);
        Preconditions.checkArg(index != -1, "Failed to find adapter");
        
        adapters.remove(index);
        if (position >= index) {
            position--;
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + '@' + hashCode()
                + '{'
                + Arrays.toString(adapters.toArray())
                + '}';
    }
}
