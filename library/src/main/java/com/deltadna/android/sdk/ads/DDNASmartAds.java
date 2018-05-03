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

package com.deltadna.android.sdk.ads;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.DDNA;
import com.deltadna.android.sdk.ads.core.utils.Preconditions;
import com.deltadna.android.sdk.ads.exceptions.NotInitialisedException;
import com.deltadna.android.sdk.ads.listeners.AdRegistrationListener;

/**
 * Singleton class for accessing the deltaDNA SmartAds SDK.
 * <p>
 * An instance should be retrieved by calling {@link #instance()}.
 * {@link com.deltadna.android.sdk.DDNA} should be initialised and started
 * before registering for ads through {@link #registerForAds(Activity)}. At
 * the point where you would like to display an ad, use one of the static
 * {@code create} methods in the {@link InterstitialAd}/{@link RewardedAd}
 * classes.
 * <p>
 * Listening for ad registration success or failure can be done using
 * {@link #setAdRegistrationListener(AdRegistrationListener)}.
 */
public final class DDNASmartAds {
    
    private static DDNASmartAds instance = null;
    
    private final Ads ads;
    private final EngageFactory engageFactory;
    
    /**
     * Initialises the {@link DDNASmartAds} singleton instance.
     *
     * @param configuration the configuration
     *
     * @return singleton instance
     */
    public static synchronized DDNASmartAds initialise(
            Configuration configuration) {
        
        Preconditions.checkArg(
                configuration != null,
                "configuration cannot be null");
        
        if (instance == null) {
            instance = new DDNASmartAds(
                    configuration.application,
                    configuration.settings,
                    configuration.activity);
        } else {
            Log.w(BuildConfig.LOG_TAG, "SDK has already been initialised");
        }
        
        return instance;
    }
    
    /**
     * Returns the {@link DDNASmartAds} singleton instance.
     *
     * @return singleton instance
     *
     * @throws  NotInitialisedException if {@link #initialise(Configuration)}
     *          has not been called
     */
    public static synchronized DDNASmartAds instance() {
        if (instance == null) {
            throw new NotInitialisedException();
        }
        
        return instance;
    }
    
    /**
     * Gets the Engage factory which provides an easier way of requesting
     * Engage actions.
     *
     * @return the {@link EngageFactory}
     */
    public EngageFactory getEngageFactory() {
        return engageFactory;
    }
    
    /**
     * Registers for ads.
     *
     * @param activity the activity to register with ads
     *
     * @deprecated as of version 1.8, replaced by automatic registration
     */
    @Deprecated
    public void registerForAds(Activity activity) {}
    
    /**
     * Sets a listener for ad registration status callbacks.
     * <p>
     * The listener is stored as a weak reference so the instance should be
     * attached to an object with a longer lifecycle, for example an
     * {@link Activity}.
     *
     * @param listener the listener for ad registration status
     */
    public void setAdRegistrationListener(
            @Nullable AdRegistrationListener listener) {
        
        ads.setAdRegistrationListener(listener);
    }
    
    /**
     * @deprecated as of version 1.8, replaced by automatic lifecycle callbacks
     */
    @Deprecated
    public void onPause() {}
    
    /**
     * @deprecated as of version 1.8, replaced by automatic lifecycle callbacks
     */
    @Deprecated
    public void onResume() {}
    
    /**
     * @deprecated as of version 1.8, replaced by automatic lifecycle callbacks
     */
    @Deprecated
    public void onDestroy() {}
    
    /**
     * Gets the {@link Settings} used for SmartAds. Any changes made to the
     * settings will be applied the next time registration for ads will take
     * place.
     * 
     * @return the settings
     */
    public Settings getSettings() {
        return ads.getSettings();
    }
    
    Ads getAds() {
        return ads;
    }
    
    private DDNASmartAds(
            Application application,
            Settings settings,
            @Nullable Class<? extends Activity> activity) {
        
        ads = new Ads(settings, application, activity);
        engageFactory = new EngageFactory(DDNA.instance(), ads);
    }
    
    /**
     * Provides a configuration when initialising the SDK through
     * {@link #initialise(Configuration)} inside of an {@link Application}
     * class.
     */
    public static final class Configuration {
        
        private final Application application;
        private final Settings settings;
        
        @Nullable
        private Class<? extends Activity> activity;
        
        public Configuration(Application application) {
            Preconditions.checkArg(
                    application != null,
                    "application cannot be null");
            
            this.application = application;
            
            settings = new Settings();
        }
        
        /**
         * Sets the {@link Activity} class which is used as the main entry and
         * exit point for the application.
         * <p>
         * This method should be used when the SDK fails to register for ads
         * correctly.
         *
         * @param cls the activity class
         * 
         * @return this {@link Configuration} instance
         */
        public Configuration activity(@Nullable Class<? extends Activity> cls) {
            activity = cls;
            return this;
        }
        
        /**
         * Allows changing of {@link Settings} values.
         *
         * @param modifier the settings modifier
         *
         * @return this {@link Configuration} instance
         */
        public Configuration withSettings(SettingsModifier modifier) {
            modifier.modify(settings);
            return this;
        }
    }
    
    public interface SettingsModifier {
        
        void modify(Settings settings);
    }
}
