/*
 * Copyright (c) 2017 deltaDNA Ltd. All rights reserved.
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

package com.deltadna.android.sdk.ads.provider.applovin

import com.applovin.adview.AppLovinInterstitialAdDialog
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class PollingLoadCheckerTest {
    
    private var ad = mock<AppLovinInterstitialAdDialog>()
    private var forwarder = mock<AppLovinEventForwarder>()
    
    private var uut = PollingLoadChecker(ad, forwarder)
    
    @Before
    fun before() {
        uut = PollingLoadChecker(ad, forwarder)
    }
    
    @After
    fun after() {
        reset(ad, forwarder)
    }
    
    @Test
    fun adReady() {
        whenever(ad.isAdReadyToDisplay).then { true }
        
        uut.start()
        advance(500)
        
        verify(forwarder).setChecker(isNull())
        verify(forwarder).adReceived(isNull())
        
        advance(1000)
        verifyNoMoreInteractions(forwarder)
    }
    
    @Test
    fun adNotReady() {
        uut.start()
        advance(1000)
        
        verify(ad, times(2)).isAdReadyToDisplay
    }
    
    @Test
    fun stop() {
        uut.start()
        uut.stop()
        advance(500)
        
        verifyNoMoreInteractions(ad)
    }
    
    private val advance = { by: Long ->
        RuntimeEnvironment.getMasterScheduler().advanceBy(
                by,
                TimeUnit.MILLISECONDS)
    }
}
