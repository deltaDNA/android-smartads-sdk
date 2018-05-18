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

package com.deltadna.android.sdk.ads.bindings;

import android.app.Activity;
import android.support.annotation.NonNull;

import org.json.JSONObject;

public abstract class MediationAdapter implements Comparable<MediationAdapter> {
    
    public final int eCPM;
    public final int demoteOnCode;
    public final Privacy privacy;
    
    private int waterfallIndex;
    
    private int requests;
    private int score;
    
    public MediationAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex) {
        
        this.eCPM = eCPM;
        this.demoteOnCode = demoteOnCode;
        this.privacy = privacy;
        this.waterfallIndex = waterfallIndex;
    }
    
    @Override
    public final int compareTo(@NonNull MediationAdapter another) {
        if (score == another.score) {
            // lower index means higher priority
            return ((Integer) waterfallIndex).compareTo(another.waterfallIndex);
        } else {
            // lower score means lower priority
            return ((Integer) score).compareTo(another.score) * -1;
        }
    }
    
    public final int getWaterfallIndex() {
        return waterfallIndex;
    }
    
    public final int getRequests() {
        return requests;
    }
    
    public final void incrementRequests() {
        requests++;
    }
    
    public final void updateScore(AdRequestResult result) {
        score += result.computeScore(demoteOnCode);
    }
    
    public final void reset(int waterfallIndex) {
        this.waterfallIndex = waterfallIndex;
        
        score = 0;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + '@' + hashCode()
                + '{'
                + "waterfallIndex=" + waterfallIndex
                + ", requests=" + requests
                + ", score=" + score
                + '}';
    }
    
    public abstract void requestAd(Activity activity, MediationListener listener, JSONObject configuration);

    public abstract void showAd();

    public abstract String getProviderString();

    public abstract String getProviderVersionString();

    public abstract void onDestroy();

    public abstract void onPause();

    public abstract void onResume();
    
    /**
     * Will be called when the adapter is swapped out for the next one, removed,
     * or when the waterfall has been reset.
     */
    public void onSwappedOut() {}
    
    public boolean isGdprCompliant() {
        return false;
    }
}
