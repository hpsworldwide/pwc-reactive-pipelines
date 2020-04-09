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

import reactor.core.publisher.Mono;

public interface SampleService {
    /**
     * This method fails if the context contains the key/value "fail=true"
     * @param sampleContext
     * @return
     */
    SampleContext failFast(SampleContext sampleContext);

    /**
     * An example of procedural method
     * @param sampleContext
     * @return
     */
    SampleContext syncTask(SampleContext sampleContext);

    /**
     * An example of async method
     * @param sampleContext
     * @return
     */
    Mono<SampleContext> asyncTask1(SampleContext sampleContext);

    /**
     * An example of async method
     * @param sampleContext
     * @return
     */
    Mono<SampleContext> asyncTask2(SampleContext sampleContext);

    /**
     * An example of async method
     * @param sampleContext
     * @return
     */
    Mono<SampleContext> asyncTask2bis(SampleContext sampleContext);

    /**
     * An example of async method
     * @param sampleContext
     * @return
     */
    Mono<SampleContext> asyncTask3(SampleContext sampleContext);

    /**
     * An example of async method
     * @param sampleContext
     * @return
     */
    Mono<SampleContext> asyncTask4(SampleContext sampleContext);
}
