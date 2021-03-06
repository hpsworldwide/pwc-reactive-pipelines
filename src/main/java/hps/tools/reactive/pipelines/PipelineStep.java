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

/**
 * Functionnal interface for writing reactive task.<br/>
 * You may use a reference to any function that has this method's signature :<br/>
 * <code>public Mono<T> anyMethod(T controlContext){}</code>
 *
 * @param <T>
 */
public interface PipelineStep<T extends PipelineContext> {
    Mono<T> execute(T controlContext);
}
