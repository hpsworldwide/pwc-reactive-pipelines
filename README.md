# reactive-pipelines
![](https://github.com/hpsworldwide/reactive-pipelines/workflows/Gradle%20Build/badge.svg)

## Overview

Well, Reactor 3 is awesome, but maybe you want to write a pipeline like this : 

```java
    pipeline (
            step1,
            step2,
            step3
    ).execute(context);

    //and some more code describing steps...
```

It means that `step2` will be executed after `step1` completes, and then `step3` will "wait" that `step2` completes to start.

And you want this to be done in a performant non-blocking way (no `Mono.block()` or async/await pattern with `CountDownLatch`) ?

Lucky you, that's what Reactive Pipeline do.

You can see examples in [SamplePipeline](src/test/java/hps/tools/reactive/pipelines/SamplePipeline.java).

## Getting started

* [Add the library with Maven or Gradle](#add-the-library-with-maven-or-gradle)
* [Write your first pipeline](#write-your-first-pipeline)
* [Customize your steps](#customize-your-steps)
* [Implement your own pipeline's context](#create-a-pipeline-context)
* [Parallelize some steps](#parallelize-some-steps)

### Add the library with Maven or Gradle

With Gradle :

```groovy
    repositories {
        ...
        maven {
            name "HPS Bintray"
            url "https://dl.bintray.com/hps-worldwide/hps-tools"
        }
    }
    ...
    dependencies {
        ...
        implementation group: 'com.hps.tools', name: 'reactive-pipelines', version: '1.0'
        ...
    }

```

With Maven :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"...>
    <modelVersion>4.0.0</modelVersion>
    ...
    <repositories>
        ...
        <repository>
            <id>HPS Bintray</id>
            <url>https://dl.bintray.com/hps-worldwide/hps-tools</url>
        </repository>
    </repositories>

    <dependencies>
        ...
        <dependency>
            <groupId>com.hps.tools</groupId>
            <artifactId>reactive-pipelines</artifactId>
            <version>1.0</version>
        </dependency>
        ...
    </dependencies>

</project>
```

## Write your first pipeline

```java
import hps.tools.reactive.pipelines.PipelineStep;
import hps.tools.reactive.pipelines.SimpleContext;
import reactor.core.publisher.Mono;
import static hps.tools.reactive.pipelines.PipelineUtils.pipeline;
public class MyFirstPipeline {
    /** The pipeline */
    Mono<SimpleContext> executePipeline(SimpleContext context) {
        return pipeline(
                selectProduct,
                clickBuyButton,
                processPayment
        ).execute(new SimpleContext());
    }
    /** A first step */
    PipelineStep<SimpleContext> selectProduct = c -> Mono.fromCallable(() -> {
        c.put("selectProduct", "cool stuff");
        return c;
    });
    /** A second step that needs the first step to be complete */
    PipelineStep<SimpleContext> clickBuyButton = c -> Mono.fromCallable(() -> {
        assert c.get("step1").equals("cool stuff");
        c.put("clickBuyButton", "ok");
        return c;
    });
    /** A third step that needs others steps to be complete */
    PipelineStep<SimpleContext> processPayment = c -> Mono.fromCallable(() -> {
        assert c.get("step1").equals("cool stuff");
        assert c.get("step2").equals("ok");
        c.put("processPayment", "start");
        return c;
    });
}
```

## Customize your steps

Pipeline steps are functions that takes a PipelineContext implementation in argument and return a Mono of this implementation.

You can use **lambda**, like in this example :

```java
PipelineStep<SimpleContext> selectProduct = c -> Mono.fromCallable(() -> {
    c.put("selectProduct", true);
    return c;
});
...
pipeline(
    selectProduct,
    ...
)
```

Or you may prefer **function references** :

```java
//In shoppingCartService's class
public Mono<SimpleContext> addProduct(SimpleContext context) {
    shoppingCart.put(context.get("selectProduct"));
}

//In pipeline's class
pipeline(
    selectProduct,
    shoppingCartService::addProduct,
    ...
)
```

You may also use **non-reactive lambda** for some steps : 

```java
...
Sync<SimpleContext> viewFees = c -> {
    display(c.get("fees"));
    return c;
};
...
pipeline(
    selectProduct,
    viewFees,
    ...
)
```

This may at last be useful if you have a **non reactive service** to call :

```java
//In confirmationService's class
public SimpleContext confirm(SimpleContext context) {
    if(c.get("processPayment").equals("start")) {
        c.put("processPayment", "confirmed");
    }
    return c;
}

//In pipeline's class
pipeline(
    step1,
    shoppingCartService::addProduct,
    (Sync<SimpleContext>) confirmationService::confirm,
    ...
)
```

> Note that you will have to cast it to `Sync<SimpleContext>`.

## Create a pipeline context

A pipeline context is an object that implements [PipelineContext](src/main/java/hps/tools/reactive/pipelines/PipelineContext.java).

The `failFast()` method is used to brake the pipeline when a step finishes by returning 'false' to this method. 

The way you store the data in your context is up to you, but you can start with [SimpleContext](src/main/java/hps/tools/reactive/pipelines/SimpleContext.java).

Be careful to use *threadsafe implementation* of this interface, even in non parallel pipelines.

### Parallelize some steps

You may parallelize some steps and compose elegant pipelines : 

```java
import static hps.tools.reactive.pipelines.PipelineUtils.parallelize;
import static hps.tools.reactive.pipelines.PipelineUtils.pipeline;
...
pipeline (
    selectProduct,
    parallelize(
       createAccount, 
       pipeline(
         clickBuyButton,
         confirm
       ),
       preparePayment
    ),
    processPayment
).execute(context);
```

## Contributions

We welcome code contributions through pull requests. 
Please join our contributors community and help us make this library even better!

## License

This Java library is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

(c) All rights reserved HPS Worldwide