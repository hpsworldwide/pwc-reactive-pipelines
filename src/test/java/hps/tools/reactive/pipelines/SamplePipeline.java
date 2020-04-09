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

import static hps.tools.reactive.pipelines.AsynchronousControlUtils.parallelize;
import static hps.tools.reactive.pipelines.AsynchronousControlUtils.pipeline;

/**
 * Examples of pipelines
 */
public class SamplePipeline {

    private final SampleService service;

    public SamplePipeline(SampleService service) {
        this.service = service;
    }

    public Mono<SampleContext> sequentialWorkflow(SampleContext controlContext) {
        return pipeline(
                // is equivalent to
                // controlContext -> service.asyncTask(controlContext)
                service::asyncTask1,
                (Sync<SampleContext>) service::failFast,
                service::asyncTask2,
                (Sync<SampleContext>) service::syncTask
        ).execute(controlContext);
    }

    public Mono<SampleContext> parallelWorkflow(SampleContext controlContext) {
        return parallelize(
                service::asyncTask1,
                (Sync<SampleContext>) service::failFast,
                service::asyncTask2bis,
                (Sync<SampleContext>) service::syncTask
        ).execute(controlContext);
    }

    public Mono<SampleContext> compositeWorkflow(SampleContext controlContext) {
        return pipeline(
                service::asyncTask1,
                parallelize(
                    service::asyncTask2,
                    pipeline(
                        (Sync<SampleContext>) service::failFast,
                        service::asyncTask3
                    ),
                    (Sync<SampleContext>) service::syncTask
                ),
                service::asyncCompositeFinal
        ).execute(controlContext);
    }

}
