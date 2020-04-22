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

public class SampleService {

    /**
     * This method fails if the context contains the key/value "fail=true"
     * @param simpleContext
     * @return
     */
    public SimpleContext failFast(SimpleContext simpleContext) {
        simpleContext.put("failFast", getCurrentThreadName());
        boolean fail = Boolean.TRUE.equals(simpleContext.get("fail"));
        if(fail) {
            //do not optimize code here, because of threadsafe behaviour
            simpleContext.setSucceed(false);
        }
        return simpleContext;
    }

    /**
     * An example of procedural method
     * @param simpleContext
     * @return
     */
    public SimpleContext syncTask(SimpleContext simpleContext) {
        simpleContext.put("syncTask", getCurrentThreadName());
        return simpleContext;
    }

    /**
     * An example of async task
     * @param simpleContext
     * @return
     */
    public Mono<SimpleContext> asyncTask1(SimpleContext simpleContext) {
        return simulateAsyncProcess("asyncTask1", simpleContext);
    }

    /**
     * An example of async task
     * @param simpleContext
     * @return
     */
    public Mono<SimpleContext> asyncTask2(SimpleContext simpleContext) {
        return simulateAsyncProcess("asyncTask2", simpleContext);
    }

    /**
     * An example of async task that depends on a previous task1
     * @param simpleContext
     * @return
     */
    public Mono<SimpleContext> asyncTask2bis(SimpleContext simpleContext) {
        return simulateAsyncProcess("asyncTask2bis", simpleContext)
                .map(context -> {
                    if(context.get("asyncTask1") == null) {
                        simpleContext.failFast();
                    }
                    return simpleContext;
                });
    }

    /**
     * An example of async task
     * @param simpleContext
     * @return
     */
    public Mono<SimpleContext> asyncTask3(SimpleContext simpleContext) {
        return simulateAsyncProcess("asyncTask3", simpleContext);
    }

    /**
     * An example of async task that depends on a previous task3
     * @param simpleContext
     * @return
     */
    public Mono<SimpleContext> asyncCompositeFinal(SimpleContext simpleContext) {
        StringBuilder orderProblem = new StringBuilder();
        if(simpleContext.get("asyncTask1") == null) {
            orderProblem.append("asyncTask1\n");
        }
        if(simpleContext.get("asyncTask2") == null) {
            orderProblem.append("asyncTask2\n");
        }
        if(simpleContext.get("asyncTask3") == null) {
            orderProblem.append("asyncTask3\n");
        }
        if(simpleContext.get("failFast") == null) {
            orderProblem.append("failFast\n");
        }
        if(simpleContext.get("syncTask") == null) {
            orderProblem.append("syncTask\n");
        }
        if(orderProblem.length() > 0) {
// TODO           throw new RuntimeException(getCurrentThreadName() + ": Missing following steps before final step : \n" + orderProblem.toString());
        }
        return simulateAsyncProcess("asyncCompositeFinal", simpleContext);
    }

    private Mono<SimpleContext> simulateAsyncProcess(String methodName, SimpleContext simpleContext) {
        //simulate a random latency between 1 to 50 milliseconds
        final int fakeLatency = RandomUtils.nextInt(1, 50);
        return Mono.fromCallable(() -> {
            //write the thread name in the context with the method name's key
            simpleContext.put(methodName, getCurrentThreadName());
            return simpleContext;
        }).delayElement(Duration.ofMillis(fakeLatency));
    }

    private String getCurrentThreadName() {
        return Thread.currentThread().getName();
    }
}
