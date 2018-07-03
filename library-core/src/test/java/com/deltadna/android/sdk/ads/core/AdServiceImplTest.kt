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

package com.deltadna.android.sdk.ads.core

import android.app.Activity
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.AdShowResult
import com.deltadna.android.sdk.ads.core.network.DummyAdapter
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class AdServiceImplTest {
    
    private var activity = Robolectric.buildActivity(Activity::class.java).create()
    private val listener = mock<AdServiceListener>()
    
    private var uut = AdServiceImpl(activity.get(), listener, "1")
    
    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create()
        
        uut = AdServiceImpl(
                // needed just to stop the version name from being null
                spy(activity.get()).apply {
                    whenever(packageManager).then {
                        mock<PackageManager>().apply {
                            whenever(getPackageInfo(packageName, 0)).then {
                                PackageInfo().apply {
                                    versionName = "1"
                                }
                            }
                        }
                    }
                },
                listener,
                "1")
    }
    
    @After
    fun after() {
        activity.destroy()
        reset(listener)
    }
    
    @Test
    fun disabled() {
        withService(Config(adShowSession = false)) {
            verify(listener).onFailedToRegisterForInterstitialAds(any())
            verify(listener).onFailedToRegisterForRewardedAds(any())
            
            assertThat(isInterstitialAdAllowed("point", params(), false)).isFalse()
            assertThat(isRewardedAdAllowed("point", params(), false)).isFalse()
            
            assertThat(hasLoadedInterstitialAd()).isFalse()
            assertThat(hasLoadedRewardedAd()).isFalse()
            
            showInterstitialAd("point", params())
            verify(listener).onInterstitialAdFailedToOpen(eq("Not registered"))
            
            showRewardedAd("point", params())
            verify(listener).onRewardedAdFailedToOpen(eq("Not registered"))
            
            verify(listener, never()).onRecordEvent(any(), any())
        }
    }
    
    @Test
    fun noNetworks() {
        withService(Config(interstitial = emptyList(), rewarded = emptyList())) {
            verify(listener).onFailedToRegisterForInterstitialAds(any())
            verify(listener).onFailedToRegisterForRewardedAds(any())
            
            assertThat(isInterstitialAdAllowed("point", params(), false)).isFalse()
            assertThat(isRewardedAdAllowed("point", params(), false)).isFalse()
            
            assertThat(hasLoadedInterstitialAd()).isFalse()
            assertThat(hasLoadedRewardedAd()).isFalse()
            
            showInterstitialAd("point", params())
            verify(listener).onInterstitialAdFailedToOpen(eq("Not registered"))
            
            showRewardedAd("point", params())
            verify(listener).onRewardedAdFailedToOpen(eq("Not registered"))
            
            verify(listener, never()).onRecordEvent(any(), any())
        }
    }
    
    @Test
    fun disallowedByEngage() {
        withService {
            val params = params("adShowPoint" to false)
            
            assertThat(isInterstitialAdAllowed("point", params, false)).isFalse()
            assertThat(isRewardedAdAllowed("point", params, false)).isFalse()
            verify(listener, never()).onRecordEvent(eq("adShow"), any())
            
            showInterstitialAd("point", params)
            
            verify(listener).onRecordEvent(
                    eq("adShow"),
                    argThat {
                        contains("\"adType\":\"INTERSTITIAL\"") &&
                        contains("\"adPoint\":\"point\"") &&
                        contains("\"adStatus\":\"${AdShowResult.AD_SHOW_POINT.status}\"")
                    })
            verify(listener).onInterstitialAdFailedToOpen(
                    eq(AdShowResult.AD_SHOW_POINT.status))
            
            showRewardedAd("point", params)
            
            verify(listener).onRecordEvent(
                    eq("adShow"),
                    argThat {
                        contains("\"adType\":\"REWARDED\"") &&
                        contains("\"adPoint\":\"point\"") &&
                        contains("\"adStatus\":\"${AdShowResult.AD_SHOW_POINT.status}\"")
                    })
            verify(listener).onRewardedAdFailedToOpen(
                    eq(AdShowResult.AD_SHOW_POINT.status))
        }
    }
    
    @Test
    fun allowedAndShownWithoutEngagement() {
        withService {
            assertThat(isInterstitialAdAllowed(null, null, false)).isTrue()
            assertThat(isRewardedAdAllowed(null, null, false)).isTrue()
            verify(listener, never()).onRecordEvent(eq("adShow"), any())
            
            showInterstitialAd(null, null)
            advance(DummyAdapter.DISMISS_AFTER)
            
            verify(listener).onRecordEvent(eq("adShow"), argThat {
                contains("\"adType\":\"INTERSTITIAL\"") &&
                !contains("\"adPoint\"") &&
                contains("\"adStatus\":\"${AdShowResult.FULFILLED.status}\"")
            })
            inOrder(listener) {
                verify(listener).onInterstitialAdOpened()
                verify(listener).onInterstitialAdClosed()
            }
            
            showRewardedAd(null, null)
            advance(DummyAdapter.DISMISS_AFTER)
            
            verify(listener).onRecordEvent(eq("adShow"), argThat {
                contains("\"adType\":\"REWARDED\"") &&
                !contains("\"adPoint\"") &&
                contains("\"adStatus\":\"${AdShowResult.FULFILLED.status}\"")
            })
            inOrder(listener) {
                verify(listener).onRewardedAdOpened(isNull())
                verify(listener).onRewardedAdClosed(any())
            }
        }
    }
    
    @Test
    fun allowedAndShownWithEngagement() {
        withService {
            assertThat(isInterstitialAdAllowed("point", params(), false)).isTrue()
            assertThat(isRewardedAdAllowed("point", params(), false)).isTrue()
            verify(listener, never()).onRecordEvent(eq("adShow"), any())
            
            showInterstitialAd("point", params())
            advance(DummyAdapter.DISMISS_AFTER)
            
            verify(listener).onRecordEvent(eq("adShow"), argThat {
                contains("\"adType\":\"INTERSTITIAL\"") &&
                contains("\"adPoint\":\"point\"") &&
                contains("\"adStatus\":\"${AdShowResult.FULFILLED.status}\"")
            })
            inOrder(listener) {
                verify(listener).onInterstitialAdOpened()
                verify(listener).onInterstitialAdClosed()
            }
            
            showRewardedAd("point", params())
            advance(DummyAdapter.DISMISS_AFTER)
            
            verify(listener).onRecordEvent(eq("adShow"), argThat {
                contains("\"adType\":\"REWARDED\"") &&
                contains("\"adPoint\":\"point\"") &&
                contains("\"adStatus\":\"${AdShowResult.FULFILLED.status}\"")
            })
            inOrder(listener) {
                verify(listener).onRewardedAdOpened(eq("point"))
                verify(listener).onRewardedAdClosed(any())
            }
        }
    }
    
    @Test
    fun minTimeNotElapsed() {
        withService(Config(adMinimumInterval = DummyAdapter.DISMISS_AFTER / 1000 * 2)) {
            // success
            assertThat(isInterstitialAdAllowed("point", params(), true)).isTrue()
            showInterstitialAd("point", params())
            // fail; too early
            advance(DummyAdapter.DISMISS_AFTER)
            assertThat(isInterstitialAdAllowed("point", params(), false)).isTrue()
            assertThat(isInterstitialAdAllowed("point", params(), true)).isFalse()
            showInterstitialAd("point", params())
            // success
            advance(DummyAdapter.DISMISS_AFTER)
            assertThat(isInterstitialAdAllowed("point", params(), true)).isTrue()
            showInterstitialAd("point", params())
            advance(DummyAdapter.DISMISS_AFTER)
            
            inOrder(listener) {
                // success
                verify(listener).onRecordEvent(eq("adShow"), argThat {
                    contains("\"adType\":\"INTERSTITIAL\"") &&
                    contains("\"adPoint\":\"point\"") &&
                    contains("\"adStatus\":\"${AdShowResult.FULFILLED.status}\"")
                })
                verify(listener).onInterstitialAdOpened()
                verify(listener).onInterstitialAdClosed()
                
                // fail; too early
                verify(listener).onRecordEvent(eq("adShow"), argThat {
                    contains("\"adType\":\"INTERSTITIAL\"") &&
                    contains("\"adPoint\":\"point\"") &&
                    contains("\"adStatus\":\"${AdShowResult.MIN_TIME_NOT_ELAPSED.status}\"")
                })
                verify(listener).onInterstitialAdFailedToOpen(
                        eq("Minimum environment time between ads not elapsed"))
                
                // success
                verify(listener).onRecordEvent(eq("adShow"), argThat {
                    contains("\"adType\":\"INTERSTITIAL\"") &&
                    contains("\"adPoint\":\"point\"") &&
                    contains("\"adStatus\":\"${AdShowResult.FULFILLED.status}\"")
                })
                verify(listener).onInterstitialAdOpened()
                verify(listener).onInterstitialAdClosed()
            }
            
            // success
            assertThat(isRewardedAdAllowed("point", params(), true)).isTrue()
            showRewardedAd("point", params())
            // fail; too early
            advance(DummyAdapter.DISMISS_AFTER)
            assertThat(isRewardedAdAllowed("point", params(), false)).isTrue()
            assertThat(isRewardedAdAllowed("point", params(), true)).isFalse()
            showRewardedAd("point", params())
            // success
            advance(DummyAdapter.DISMISS_AFTER)
            assertThat(isRewardedAdAllowed("point", params(), true)).isTrue()
            showRewardedAd("point", params())
            advance(DummyAdapter.DISMISS_AFTER)
            
            inOrder(listener) {
                // success
                verify(listener).onRecordEvent(eq("adShow"), argThat {
                    contains("\"adType\":\"REWARDED\"") &&
                    contains("\"adPoint\":\"point\"") &&
                    contains("\"adStatus\":\"${AdShowResult.FULFILLED.status}\"")
                })
                verify(listener).onRewardedAdOpened(eq("point"))
                verify(listener).onRewardedAdClosed(any())
                
                // fail; too early
                verify(listener).onRecordEvent(eq("adShow"), argThat {
                    contains("\"adType\":\"REWARDED\"") &&
                    contains("\"adPoint\":\"point\"") &&
                    contains("\"adStatus\":\"${AdShowResult.MIN_TIME_NOT_ELAPSED.status}\"")
                })
                verify(listener).onRewardedAdFailedToOpen(
                        eq("Minimum environment time between ads not elapsed"))
                
                // success
                verify(listener).onRecordEvent(eq("adShow"), argThat {
                    contains("\"adType\":\"REWARDED\"") &&
                    contains("\"adPoint\":\"point\"") &&
                    contains("\"adStatus\":\"${AdShowResult.FULFILLED.status}\"")
                })
                verify(listener).onRewardedAdOpened(eq("point"))
                verify(listener).onRewardedAdClosed(any())
            }
        }
    }
    
    @Test
    fun minTimeDecisionPointNotElapsed() {
        withService {
            val params = params("ddnaAdShowWaitSecs" to DummyAdapter.DISMISS_AFTER / 1000 * 2)
            
            // success
            assertThat(isInterstitialAdAllowed("point1", params, true)).isTrue()
            showInterstitialAd("point1", params)
            // fail; too early
            advance(DummyAdapter.DISMISS_AFTER)
            assertThat(isInterstitialAdAllowed("point1", params, false)).isTrue()
            assertThat(isInterstitialAdAllowed("point1", params, true)).isFalse()
            showInterstitialAd("point1", params)
            // success
            advance(DummyAdapter.DISMISS_AFTER)
            assertThat(isInterstitialAdAllowed("point1", params, true)).isTrue()
            showInterstitialAd("point1", params)
            advance(DummyAdapter.DISMISS_AFTER)
            
            inOrder(listener) {
                // success
                verify(listener).onRecordEvent(eq("adShow"), argThat {
                    contains("\"adType\":\"INTERSTITIAL\"") &&
                    contains("\"adPoint\":\"point1\"") &&
                    contains("\"adStatus\":\"${AdShowResult.FULFILLED.status}\"")
                })
                verify(listener).onInterstitialAdOpened()
                verify(listener).onInterstitialAdClosed()
                
                // fail; too early
                verify(listener).onRecordEvent(eq("adShow"), argThat {
                    contains("\"adType\":\"INTERSTITIAL\"") &&
                    contains("\"adPoint\":\"point1\"") &&
                    contains("\"adStatus\":\"${AdShowResult.MIN_TIME_DECISION_POINT_NOT_ELAPSED.status}\"")
                })
                verify(listener).onInterstitialAdFailedToOpen(
                        eq("Minimum decision point time between ads not elapsed"))
                
                // success
                verify(listener).onRecordEvent(eq("adShow"), argThat {
                    contains("\"adType\":\"INTERSTITIAL\"") &&
                    contains("\"adPoint\":\"point1\"") &&
                    contains("\"adStatus\":\"${AdShowResult.FULFILLED.status}\"")
                })
                verify(listener).onInterstitialAdOpened()
                verify(listener).onInterstitialAdClosed()
            }
            
            // success
            assertThat(isRewardedAdAllowed("point2", params, true)).isTrue()
            showRewardedAd("point2", params)
            // fail; too early
            advance(DummyAdapter.DISMISS_AFTER)
            assertThat(isRewardedAdAllowed("point2", params, false)).isTrue()
            assertThat(isRewardedAdAllowed("point2", params, true)).isFalse()
            showRewardedAd("point2", params)
            // success
            advance(DummyAdapter.DISMISS_AFTER)
            assertThat(isRewardedAdAllowed("point2", params, true)).isTrue()
            showRewardedAd("point2", params)
            advance(DummyAdapter.DISMISS_AFTER)
            
            inOrder(listener) {
                // success
                verify(listener).onRecordEvent(eq("adShow"), argThat {
                    contains("\"adType\":\"REWARDED\"") &&
                    contains("\"adPoint\":\"point2\"") &&
                    contains("\"adStatus\":\"${AdShowResult.FULFILLED.status}\"")
                })
                verify(listener).onRewardedAdOpened(eq("point2"))
                verify(listener).onRewardedAdClosed(any())
                
                // fail; too early
                verify(listener).onRecordEvent(eq("adShow"), argThat {
                    contains("\"adType\":\"REWARDED\"") &&
                    contains("\"adPoint\":\"point2\"") &&
                    contains("\"adStatus\":\"${AdShowResult.MIN_TIME_DECISION_POINT_NOT_ELAPSED.status}\"")
                })
                verify(listener).onRewardedAdFailedToOpen(
                        eq("Minimum decision point time between ads not elapsed"))
                
                // success
                verify(listener).onRecordEvent(eq("adShow"), argThat {
                    contains("\"adType\":\"REWARDED\"") &&
                    contains("\"adPoint\":\"point2\"") &&
                    contains("\"adStatus\":\"${AdShowResult.FULFILLED.status}\"")
                })
                verify(listener).onRewardedAdOpened(eq("point2"))
                verify(listener).onRewardedAdClosed(any())
            }
        }
    }
    
    @Test
    fun sessionLimitReached() {
        withService(Config(adMaxPerSession = 1)) {
            showInterstitialAd("point", params())
            advance(DummyAdapter.DISMISS_AFTER)
            showInterstitialAd("point", params())
            
            verify(listener).onInterstitialAdOpened()
            verify(listener).onInterstitialAdClosed()
            verify(listener).onRecordEvent(eq("adShow"), argThat {
                contains("\"adType\":\"INTERSTITIAL\"") &&
                contains("\"adPoint\":\"point\"") &&
                contains("\"adStatus\":\"${AdShowResult.SESSION_LIMIT_REACHED.status}\"")
            })
            verify(listener).onInterstitialAdFailedToOpen(
                    eq("Session limit for environment reached"))
            
            showRewardedAd("point", params())
            advance(DummyAdapter.DISMISS_AFTER)
            showRewardedAd("point", params())
            
            verify(listener).onRewardedAdOpened(eq("point"))
            verify(listener).onRewardedAdClosed(any())
            verify(listener).onRecordEvent(eq("adShow"), argThat {
                contains("\"adType\":\"REWARDED\"") &&
                contains("\"adPoint\":\"point\"") &&
                contains("\"adStatus\":\"${AdShowResult.SESSION_LIMIT_REACHED.status}\"")
            })
            verify(listener).onRewardedAdFailedToOpen(
                    eq("Session limit for environment reached"))
        }
    }
    
    @Test
    fun sessionDecisionPointLimitReached() {
        withService {
            val params = params("ddnaAdSessionCount" to 1)
            
            showInterstitialAd("point1", params)
            advance(DummyAdapter.DISMISS_AFTER)
            showInterstitialAd("point1", params)
            
            verify(listener).onInterstitialAdOpened()
            verify(listener).onInterstitialAdClosed()
            verify(listener).onRecordEvent(eq("adShow"), argThat {
                contains("\"adType\":\"INTERSTITIAL\"") &&
                contains("\"adPoint\":\"point1\"") &&
                contains("\"adStatus\":\"${AdShowResult.SESSION_DECISION_POINT_LIMIT_REACHED.status}\"")
            })
            verify(listener).onInterstitialAdFailedToOpen(
                    eq("Session limit for decision point reached"))
            
            showRewardedAd("point2", params)
            advance(DummyAdapter.DISMISS_AFTER)
            showRewardedAd("point2", params)
            
            verify(listener).onRewardedAdOpened(eq("point2"))
            verify(listener).onRewardedAdClosed(any())
            verify(listener).onRecordEvent(eq("adShow"), argThat {
                contains("\"adType\":\"REWARDED\"") &&
                contains("\"adPoint\":\"point2\"") &&
                contains("\"adStatus\":\"${AdShowResult.SESSION_DECISION_POINT_LIMIT_REACHED.status}\"")
            })
            verify(listener).onRewardedAdFailedToOpen(
                    eq("Session limit for decision point reached"))
        }
    }
    
    fun eventsRecorded() {
        withService {
            verify(listener).onRecordEvent(eq("adRequest"), argThat {
                contains("\"adProvider\":\"Dummy\"") &&
                contains("\"adType\":\"INTERSTITIAL\"") &&
                contains("\"adStatus\":\"${AdRequestResult.Loaded}") &&
                contains("\"adProviderVersion\":") &&
                contains("\"adSdkVersion\":") &&
                contains("\"adRequestTimeMs\":") &&
                contains("\"adWaterfallIndex\":") &&
                !contains("\"adProviderError\"")
            })
            verify(listener).onRecordEvent(eq("adRequest"), argThat {
                contains("\"adProvider\":\"Dummy\"") &&
                contains("\"adType\":\"REWARDED\"") &&
                contains("\"adStatus\":\"${AdRequestResult.Loaded}") &&
                contains("\"adProviderVersion\":") &&
                contains("\"adSdkVersion\":") &&
                contains("\"adRequestTimeMs\":") &&
                contains("\"adWaterfallIndex\":") &&
                !contains("\"adProviderError\"")
            })
            
            uut.isInterstitialAdAllowed("point", params(), false)
            uut.isRewardedAdAllowed("point", params(), false)
            
            verifyNoMoreInteractions(listener)
            
            uut.showInterstitialAd(null, null)
            advance(DummyAdapter.DISMISS_AFTER)
            
            verify(listener).onRecordEvent(eq("adShow"), argThat {
                contains("\"adProvider\":\"Dummy\"") &&
                contains("\"adType\":\"INTERSTITIAL\"") &&
                contains("\"adStatus\":\"${AdShowResult.FULFILLED}") &&
                contains("\"adProviderVersion\":") &&
                contains("\"adSdkVersion\":")
            })
            verify(listener).onRecordEvent(eq("adClosed"), argThat {
                contains("\"adProvider\":\"Dummy\"") &&
                contains("\"adType\":\"INTERSTITIAL\"") &&
                contains("\"adProviderVersion\":") &&
                contains("\"adClicked\":") &&
                contains("\"adLeftApplication\":") &&
                contains("\"adEcpm\":")
            })
            
            uut.showRewardedAd("point", params())
            advance(DummyAdapter.DISMISS_AFTER)
            
            verify(listener).onRecordEvent(eq("adShow"), argThat {
                contains("\"adProvider\":\"Dummy\"") &&
                contains("\"adType\":\"REWARDED\"") &&
                contains("\"adStatus\":\"${AdShowResult.FULFILLED}") &&
                contains("\"adProviderVersion\":") &&
                contains("\"adSdkVersion\":")
            })
            verify(listener).onRecordEvent(eq("adClosed"), argThat {
                contains("\"adProvider\":\"Dummy\"") &&
                contains("\"adType\":\"REWARDED\"") &&
                contains("\"adProviderVersion\":") &&
                contains("\"adStatus\":\"Success") &&
                contains("\"adClicked\":") &&
                contains("\"adLeftApplication\":") &&
                contains("\"adEcpm\":")
            })
        }
    }
    
    private fun withService(
            config: Config = Config(),
            block: AdServiceImpl.() -> Unit) {
        with(config) {
            uut.configure(
                    jsonObject("parameters" to jsonObject(
                            "adShowSession" to adShowSession,
                            "adFloorPrice" to adFloorPrice,
                            "adDemoteOnRequestCode" to onDemoteRequestCode,
                            "adMaxPerNetwork" to maxPerNetwork,
                            "adMinimumInterval" to adMinimumInterval,
                            "adMaxPerSession" to adMaxPerSession,
                            "adProviders" to jsonArray(
                                    *interstitial.map { jsonObject(
                                            "adProvider" to it.name,
                                            "eCPM" to Int.MAX_VALUE)
                                    }.toTypedArray()),
                            "adRewardedProviders" to jsonArray(
                                    *rewarded.map { jsonObject(
                                            "adProvider" to it.name,
                                            "eCPM" to Int.MAX_VALUE)
                                    }.toTypedArray())))
                            .convert(),
                    false,
                    true,
                    false)
        }
        
        block(uut)
    }
    
    private fun advance(time: Int) {
        with(time.toLong()) {
            Robolectric
                    .getForegroundThreadScheduler()
                    .advanceBy(this, TimeUnit.MILLISECONDS)
            Thread.sleep(this)
        }
    }
    
    private fun params(vararg values: Pair<String, *>) = jsonObject(*values).convert()
    
    private data class Config(
            val decisionPoint: String = "",
            val adShowSession: Boolean = true,
            val adFloorPrice: Int = 0,
            val onDemoteRequestCode: Int = 0,
            val maxPerNetwork: Int = 0,
            val adMinimumInterval: Int = 0,
            val adMaxPerSession: Int = -1,
            val interstitial: List<AdProvider> = listOf(AdProvider.DUMMY),
            val rewarded: List<AdProvider> = listOf(AdProvider.DUMMY))
}
