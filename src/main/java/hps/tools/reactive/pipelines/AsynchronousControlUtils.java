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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

/**
 * Utils method for asynchronous pipelines
 */
public class AsynchronousControlUtils {

    /**
     * Execute the given controls in parallel.
     * <ul>
     *  <li>Order of controls may not be guessed</li>
     *  <li>The first failing control ends the subscription and returns the given results</li>
     *  <li>ControlContext must be threadsafe and has to be used independently (no order) by each control</li>
     *  <li>The controlContext will be returned at end, so you can use it to store control data for logs, etc.</li>
     * </ul>
     *
     * @param controls : unordered list of control
     * @return a flux of controlResults
     */
    public static <T extends ControlContext> AsyncControl<T> parallelize(AsyncControl<T>...controls) {
        final Flux<AsyncControl<T>> resultFlux = Flux.fromArray(controls);
        final ParallelFlux<AsyncControl<T>> controlParallelFlux = resultFlux.parallel().runOn(Schedulers.elastic());
        return initialContext -> {
            return controlParallelFlux
                    //Parallel result flux
                    .flatMap(control -> control.execute(initialContext))
                    //Remove succeed
                    .filter(controlContext -> !controlContext.failFast())
                    //Merged flux
                    .sequential()
                    //First result
                    .next()
                    //Or create if empty
                    .switchIfEmpty(Mono.just(initialContext));
        };
    }

    /**
     * Execute the given controls in serial
     * <ul>
     *     <li>Order of response is the same that parameters</li>
     *     <li>ControlContext may be used to reused results between tasks accordingly on order</li>
     *     <li>The first failing control ends the subscription and returns the given results</li>
     * </ul>
     *
     * @param controls : ordered list of control
     * @return a flux of controlResults
     */
    public static <T extends ControlContext> AsyncControl<T> pipeline(AsyncControl<T>...controls) {
        final Flux<AsyncControl<T>> controlFlux = Flux.fromArray(controls);
        return initialContext -> controlFlux
                //Result flux
                .flatMap(control -> control.execute(initialContext))
                //Remove succeed
                .filter(controlContext -> !controlContext.failFast())
                //First error result
                .next()
                //Or create if empty
                .switchIfEmpty(Mono.just(initialContext));
    }

    private AsynchronousControlUtils() {
        //Do not instanciate
    }
}
