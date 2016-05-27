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

package com.deltadna.android.sdk.ads.provider.adcolony

import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AdColonyAvailabilityMonitorTest {
    
    private val listener = mock<MediationListener>()
    private val adapter = mock<MediationAdapter>()
    private val forwarder = mock<AdColonyEventForwarder>()
    
    private var uut = AdColonyAvailabilityMonitor(listener, adapter)
    
    @Before
    fun before() {
        uut = AdColonyAvailabilityMonitor(listener, adapter)
    }
    
    @After
    fun after() {
        reset(listener, adapter, forwarder)
    }
    
    @Test
    fun onAvailableWhileAdNotShowing() {
        with(uut) {
            setForwarder(forwarder)
            onAdColonyAdAvailabilityChange(true, "reason")
            
            assertThat(isAvailable).isTrue()
            assertThat(reason).isEqualTo("reason")
            verify(listener).onAdLoaded(same(adapter))
            verifyZeroInteractions(forwarder)
        }
    }
    
    @Test
    fun onAvailableWhileAdShowing() {
        whenever(forwarder.isShowing).thenReturn(true)
        
        with(uut) {
            setForwarder(forwarder)
            // AdColony passes in `false` while an ad is showing
            onAdColonyAdAvailabilityChange(false, "reason")
            
            assertThat(isAvailable).isTrue()
            assertThat(reason).isEqualTo("reason")
            verify(listener).onAdLoaded(same(adapter))
        }
    }
    
    @Test
    fun onNotAvailable() {
        whenever(forwarder.isShowing).thenReturn(false)
        
        with(uut) {
            setForwarder(forwarder)
            onAdColonyAdAvailabilityChange(false, "reason")
            
            assertThat(isAvailable).isFalse()
            assertThat(reason).isEqualTo("reason")
            verify(listener).onAdFailedToLoad(
                    same(adapter),
                    eq(AdRequestResult.Error),
                    eq("reason"))
        }
    }
    
    @Test
    fun onNotAvailableWithoutForwarder() {
        with(uut) {
            onAdColonyAdAvailabilityChange(false, "reason")
            
            assertThat(isAvailable).isFalse()
            assertThat(reason).isEqualTo("reason")
            verify(listener).onAdFailedToLoad(
                    same(adapter),
                    eq(AdRequestResult.Error),
                    eq("reason"))
        }
    }
}
