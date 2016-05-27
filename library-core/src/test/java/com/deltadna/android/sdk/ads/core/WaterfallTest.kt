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

import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class WaterfallTest {
    
    @Test
    fun resetAndGetFirst() {
        var stubs = stubbedAdapters(2)
        with(Waterfall(stubs, 1)) {
            assertThat(resetAndGetFirst()).isSameAs(stubs[0])
            assertThat(adapters[0]).isSameAs(stubs[0])
            assertThat(adapters[1]).isSameAs(stubs[1])
        }
        
        stubs = stubbedAdapters(3)
        stubs[0].updateScore(AdRequestResult.NoFill)
        stubs[2].updateScore(AdRequestResult.NoFill)
        with(Waterfall(stubs, 1)) {
            assertThat(resetAndGetFirst()).isSameAs(stubs[1])
            assertThat(adapters[1]).isSameAs(stubs[0])
            assertThat(adapters[2]).isSameAs(stubs[2])
        }
    }
    
    @Test
    fun getNext() {
        val stubs = stubbedAdapters(2)
        with(Waterfall(stubs, 1)) {
            assertThat(next).isSameAs(stubs[1])
            assertThat(next).isNull()
        }
    }
    
    @Test
    fun score() {
        val stubs = stubbedAdapters(3)
        with(Waterfall(stubs, 1)) {
            with(resetAndGetFirst()!!) {
                score(this, AdRequestResult.NoFill)
                score(this, AdRequestResult.NoFill)
                assertThat(this.requests).isEqualTo(0)
            }
            
            score(next!!, AdRequestResult.Error)
            
            with(next!!) {
                score(this, AdRequestResult.Loaded)
                assertThat(this.requests).isEqualTo(1)
            }
            
            resetAndGetFirst()
            
            assertThat(adapters).isEqualTo(listOf(stubs[2], stubs[0]))
        }
    }
    
    @Test
    fun remove() {
        val stubs = stubbedAdapters(2)
        with(Waterfall(stubs, 1)) {
            remove(resetAndGetFirst())
            assertThat(adapters.size).isEqualTo(1)
            
            with(next) {
                assertThat(this).isSameAs(stubs[1])
                remove(this)
            }
            
            assertThat(adapters.isEmpty())
            assertThat(next).isNull()
        }
    }
}
