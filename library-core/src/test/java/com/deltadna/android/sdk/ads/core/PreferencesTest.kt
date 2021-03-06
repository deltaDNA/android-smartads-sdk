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

package com.deltadna.android.sdk.ads.core

import com.google.common.truth.Truth.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import com.deltadna.android.sdk.ads.core.Preferences.*

@RunWith(JUnit4::class)
class PreferencesTest {
    
    @Test
    fun preferencesName() {
        assertThat(METRICS.preferencesName())
                .isEqualTo("com.deltadna.android.sdk.ads.metrics")
    }
}
