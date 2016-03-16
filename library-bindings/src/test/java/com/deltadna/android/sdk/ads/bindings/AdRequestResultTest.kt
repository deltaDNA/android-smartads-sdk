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

package com.deltadna.android.sdk.ads.bindings

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import com.google.common.truth.Truth.assertThat

import com.deltadna.android.sdk.ads.bindings.AdRequestResult.*

@RunWith(JUnit4::class)
class AdRequestResultTest {
    
    @Test
    fun remove() {
        arrayOf(Configuration, Error).forEach { assertThat(it.remove()) }
        arrayOf(Loaded, NoFill, Network, Timeout, MaxRequests).forEach {
            assertThat(!it.remove())
        }
    }
    
    @Test
    fun demote() {
        arrayOf(Loaded, Configuration, Error).forEach {
            assertThat(it.demote(0))
        }
    }
    
    @Test
    fun computeScore() {
        assertThat(NoFill.computeScore(0b0)).isEqualTo(0)
        assertThat(NoFill.computeScore(0b10)).isEqualTo(0)
        
        assertThat(NoFill.computeScore(0b1)).isEqualTo(-1)
        assertThat(Network.computeScore(0b10)).isEqualTo(-1)
        assertThat(Timeout.computeScore(0b100)).isEqualTo(-1)
        assertThat(MaxRequests.computeScore(0b1000)).isEqualTo(-1)
    }
}