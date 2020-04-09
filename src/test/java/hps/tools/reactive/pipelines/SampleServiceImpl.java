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

import org.apache.commons.lang3.RandomUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class SampleServiceImpl implements SampleService {

    /**
     * This method fails if the context contains the key/value "fail=true"
     * @param sampleContext
     * @return
     */
    @Override
    public SampleContext failFast(SampleContext sampleContext) {
        sampleContext.put("failFast", Thread.currentThread().getName());
        boolean fail = Boolean.TRUE.equals(sampleContext.get("fail"));
        if(fail) {
            //do not optimize code here, because of threadsafe behaviour
            sampleContext.setSucceed(false);
        }
        return sampleContext;
    }

    /**
     * An example of procedural method
     * @param sampleContext
     * @return
     */
    @Override
    public SampleContext syncTask(SampleContext sampleContext) {
        sampleContext.put("syncTask", Thread.currentThread().getName());
        return sampleContext;
    }

    /**
     * An example of async task
     * @param sampleContext
     * @return
     */
    @Override
    public Mono<SampleContext> asyncTask1(SampleContext sampleContext) {
        return simulateAsyncProcess("asyncTask1", sampleContext);
    }

    /**
     * An example of async task
     * @param sampleContext
     * @return
     */
    @Override
    public Mono<SampleContext> asyncTask2(SampleContext sampleContext) {
        return simulateAsyncProcess("asyncTask2", sampleContext);
    }

    /**
     * An example of async task that depends on a previous task1
     * @param sampleContext
     * @return
     */
    @Override
    public Mono<SampleContext> asyncTask2bis(SampleContext sampleContext) {
        return simulateAsyncProcess("asyncTask2bis", sampleContext)
                .map(context -> {
                    if(context.get("asyncTask1") == null) {
                        sampleContext.failFast();
                    }
                    return sampleContext;
                });
    }

    /**
     * An example of async task
     * @param sampleContext
     * @return
     */
    @Override
    public Mono<SampleContext> asyncTask3(SampleContext sampleContext) {
        return simulateAsyncProcess("asyncTask3", sampleContext);
    }

    /**
     * An example of async task that depends on a previous task3
     * @param sampleContext
     * @return
     */
    @Override
    public Mono<SampleContext> asyncTask4(SampleContext sampleContext) {
        return simulateAsyncProcess("asyncTask4", sampleContext)
                .map(context -> {
                    if(context.get("asyncTask3") == null) {
                        sampleContext.failFast();
                    }
                    return sampleContext;
                });
    }

    private Mono<SampleContext> simulateAsyncProcess(String methodName, SampleContext sampleContext) {
        //simulate a random latency between 1 to 50 milliseconds
        final int fakeLatency = RandomUtils.nextInt(1, 50);
        return Mono.fromCallable(() -> {
            //write the thread name in the context with the method name's key
            sampleContext.put(methodName, Thread.currentThread().getName());
            return sampleContext;
        }).delayElement(Duration.ofMillis(fakeLatency));
    }
}
