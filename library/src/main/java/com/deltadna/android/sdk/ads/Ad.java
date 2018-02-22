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

import android.support.annotation.Nullable;

import com.deltadna.android.sdk.Engagement;

import org.json.JSONObject;

import java.util.Date;

public abstract class Ad<T extends Ad> {
    
    @Nullable
    protected final Engagement engagement;
    
    protected Ad(@Nullable Engagement engagement) {
        this.engagement = engagement;
    }
    
    /**
     * Gets whether an ad is available to be shown.
     *
     * @return {@code true} when an ad is available, else {@code false}
     */
    public abstract boolean isReady();
    
    /**
     * Shows an ad, if one is available.
     *
     * @return this instance
     */
    public abstract T show();
    
    /**
     * Gets the decision point associated with this ad.
     *
     * @return  the decision point, or {@code null} if the ad was created
     *          without an Engagement
     */
    @Nullable
    public String getDecisionPoint() {
        return (engagement != null) ? engagement.getDecisionPoint() : null;
    }
    
    /**
     * Gets the parameters returned by a matching Engage request.
     *
     * @return  the parameters
     */
    public JSONObject getParameters() {
        return (engagement != null
                && engagement.getJson() != null
                && engagement.getJson().has("parameters"))
                ? engagement.getJson().optJSONObject("parameters")
                : new JSONObject();
    }
    
    /**
     * Gets the last time an ad was shown at this decision point.
     *
     * @return the last time, or {@code null} if never
     */
    @Nullable
    public Date getLastShown() {
        final String decisionPoint = getDecisionPoint();
        return (decisionPoint != null)
                ? DDNASmartAds.instance().getAds().getLastShown(decisionPoint)
                : null;
    }
    
    /**
     * Gets the number of seconds to wait between showing ads at this decision
     * point.
     *
     * @return the number of seconds to wait
     */
    public int getAdShowWaitSecs() {
        final JSONObject params = getParameters();
        return (params != null) ? params.optInt("ddnaAdShowWaitSecs", 0) : 0;
    }
    
    /**
     * Gets the number of ads shown this session at this decision point.
     *
     * @return the number of ads shown this session
     */
    public int getSessionCount() {
        final String decisionPoint = getDecisionPoint();
        return (decisionPoint != null)
                ? DDNASmartAds.instance().getAds().getSessionCount(decisionPoint)
                : 0;
    }
    
    /**
     * Gets the number of ads allowed to show in a session at this decision
     * point.
     *
     * @return the number of ads allowed to show in a session
     */
    public int getSessionLimit() {
        final JSONObject params = getParameters();
        return (params != null) ? params.optInt("ddnaAdSessionCount", 0) : 0;
    }
    
    /**
     * Gets the number of ads shown today at this decision point.
     *
     * @return the number of ads shown today
     */
    public int getDailyCount() {
        final String decisionPoint = getDecisionPoint();
        return (decisionPoint != null)
                ? DDNASmartAds.instance().getAds().getDailyCount(decisionPoint)
                : 0;
    }
    
    /**
     * Gets the number of ads allowed to show in a day at this decision point.
     *
     * @return the number of ads allowed to show in a day
     */
    public int getDailyLimit() {
        final JSONObject params = getParameters();
        return (params != null) ? params.optInt("ddnaAdDailyCount", 0) : 0;
    }
}
