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

package com.deltadna.android.sdk.ads.core;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

final class MainThread {
    
    static <T> T redirect(T target, Class<T> cls) {
        return (T) Proxy.newProxyInstance(
                cls.getClassLoader(),
                new Class[] { cls },
                new Redirector(target));
    }
    
    private static final class Redirector implements InvocationHandler {
        
        private final Looper mainLooper = Looper.getMainLooper();
        private final Handler mainHandler = new Handler(mainLooper);
        
        private final Object target;

        Redirector(Object target) {
            this.target = target;
        }
        
        @Override
        public Object invoke(
                Object proxy,
                final Method method,
                final Object[] args) throws Throwable {
            
            if (Thread.currentThread().equals(mainLooper.getThread())) {
                return method.invoke(target, args);
            } else {
                final AtomicReference<Object> value = new AtomicReference<>(null);
                final CountDownLatch latch = new CountDownLatch(1);
                
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            value.set(method.invoke(target, args));
                        } catch (Throwable t) {
                            throw new RuntimeException(t);
                        } finally {
                            latch.countDown();
                        }
                    }
                });
                
                latch.await();
                
                return value.get();
            }
        }
    }
}
