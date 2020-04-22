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

package hps.tools.reactive.pipelines

import spock.lang.Specification

class PipelineUtilsTest extends Specification {

    def "Execute a pipeline keeping tasks order"() {
        given:
        def service = Spy(SampleService)
        SamplePipeline samplePipeline = new SamplePipeline(service)

        when: 'sequentialWorkflow is called'
        def sampleContext = new SimpleContext()
        def responseContext = samplePipeline.sequentialWorkflow(sampleContext).block()

        then: '1) asyncTask1 is called'
        1 * service.asyncTask1(_)

        then: '2) failFast is called'
        1 * service.failFast(_)

        then: '3) asyncTask2 is called'
        1 * service.asyncTask2(_)

        then: '4) syncTask is called'
        1 * service.syncTask(_)

        then: 'workflow complete successfully'
        0 * service./.*/
        responseContext.succeed

        when: 'sequentialWorkflow is called with `fail`'
        sampleContext.reset()
        sampleContext.put('fail', true)
        responseContext = samplePipeline.sequentialWorkflow(sampleContext).block()

        then: 'workflow failed after the first task'
        1 * service.asyncTask1(_)
        1 * service.failFast(_)
        0 * service.asyncTask2(_)
        0 * service.syncTask(_)
        !responseContext.succeed

    }

    def "Execute a parallelized pipeline"() {
        given:
        def service = Spy(SampleService)
        SamplePipeline samplePipeline = new SamplePipeline(service)

        when: 'sequentialWorkflow is called'
        def sampleContext = new SimpleContext()
        def responseContext = samplePipeline.parallelWorkflow(sampleContext).block()

        then: 'all tasks are called in any order'
        1 * service.asyncTask1(_)
        1 * service.failFast(_)
        1 * service.asyncTask2bis(_)
        1 * service.syncTask(_)

        then: 'workflow complete successfully'
        0 * service./.*/
        responseContext.succeed

        then: 'all tasks are executed in differents threads'
        responseContext.get('asyncTask1') != responseContext.get('failFast')
        responseContext.get('asyncTask1') != responseContext.get('asyncTask2bis')
        responseContext.get('asyncTask1') != responseContext.get('syncTask')
        responseContext.get('failFast') != responseContext.get('asyncTask2bis')
        responseContext.get('failFast') != responseContext.get('syncTask')
        responseContext.get('asyncTask2bis') != responseContext.get('syncTask')

        when: 'sequentialWorkflow is called with `fail`'
        sampleContext.reset()
        sampleContext.put('fail', true)
        responseContext = samplePipeline.sequentialWorkflow(sampleContext).block()

        then: 'workflow has failed has fast as possible'
        !sampleContext.getSucceed()
    }

    def "Execute a composite pipeline"() {
        given:
        def service = Spy(SampleService)
        SamplePipeline samplePipeline = new SamplePipeline(service)

        when: 'composite workflow is called'
        def sampleContext = new SimpleContext()
        def responseContext = samplePipeline.compositeWorkflow(sampleContext).block()

        then: '1) asyncTask1 is called'
        1 * service.asyncTask1(_)

        then: '3) failFast is called'
        1 * service.failFast(_)

        then: '2) asyncTask3 is called'
        1 * service.asyncTask3(_)

        then: '4) asyncCompositeFinal is called after other'
        1 * service.asyncCompositeFinal(_)

        then: 'workflow complete successfully'
        responseContext.succeed

        then: 'parallel tasks are executed in differents threads'
        responseContext.get('asyncTask2') != responseContext.get('asyncTask3')
        responseContext.get('asyncTask2') != responseContext.get('syncTask')
        responseContext.get('asyncTask3') != responseContext.get('syncTask')

        when: 'composite workflow is called with `fail`'
        sampleContext.reset()
        sampleContext.put('fail', true)
        responseContext = samplePipeline.compositeWorkflow(sampleContext).block()

        then: 'workflow failed after the asyncTask3'
        1 * service.asyncTask1(_)
        1 * service.failFast(_)
        0 * service.asyncTask3(_)
        0 * service.asyncCompositeFinal(_)
        !responseContext.succeed
    }

}
