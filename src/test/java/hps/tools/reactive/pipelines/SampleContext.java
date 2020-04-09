/*
 * Copyright (c) 2020 HPS
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package hps.tools.reactive.pipelines;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple threadSafe pipeline context implementation
 */
public class SampleContext implements PipelineContext {

    private final Map<String, Object> payload = new ConcurrentHashMap<>();

    private final AtomicBoolean succeed = new AtomicBoolean(true);

    @Override
    public boolean isFailFast() {
        return !succeed.get();
    }

    /**
     * Set failFast attribute to true
     */
    public void failFast() {
        setSucceed(false);
    }

    public boolean getSucceed() {
        return succeed.get();
    }

    public void setSucceed(boolean succeed) {
        this.succeed.set(succeed);
    }

    /**
     * Get data store in control context
     * @param key
     * @return
     */
    public <T> T get(String key) {
        return (T) payload.get(key);
    }

    /**
     * Store data in control context
     * @param key
     * @param value
     */
    public <T> void put(String key, T value) {
        payload.put(key, value);
    }

    public void reset() {
        payload.clear();
        succeed.set(true);
    }
}
