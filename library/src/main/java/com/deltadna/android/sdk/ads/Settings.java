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

/**
 * Settings to be used for SmartAds.
 */
public final class Settings {
    
    private boolean userConsent;
    private boolean ageRestrictedUser;
    
    /**
     * Returns whether the user has given tracking consent for advertising (GDPR).
     *
     * @return advertising tracking consent
     */
    public boolean isUserConsent() {
        return userConsent;
    }
    
    /**
     * Sets the user consent for advertising tracking (GDPR).
     * <p>
     * Changes will take effect when a new session will be started.
     *
     * @param userConsent   the user consent
     *
     * @return              this {@link Settings} instance
     */
    public Settings setUserConsent(boolean userConsent) {
        this.userConsent = userConsent;
        return this;
    }
    
    /**
     * Returns whether the user is age restricted (under 16) (GDPR).
     *
     * @return age restricted user
     */
    public boolean isAgeRestrictedUser() {
        return ageRestrictedUser;
    }
    
    /**
     * Sets age restriction (under 16) for the current user (GDPR).
     * <p>
     * Changes will take effect when a new session will be started.
     *
     * @param restrictedUser    the user age restriction
     * @return                  this {@link Settings} instance
     */
    public Settings setAgeRestrictedUser(boolean restrictedUser) {
        ageRestrictedUser = restrictedUser;
        return this;
    }
}
