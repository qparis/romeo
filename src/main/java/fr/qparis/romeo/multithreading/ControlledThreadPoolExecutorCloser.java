/*
 * Copyright 2016 Quentin PARIS

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License
 */
package fr.qparis.romeo.multithreading;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Tears down any registered @{link ExecutorService}
 * This component is mainly designed to be used as a Spring Bean
 * Registered thread pool will be properly closed once the application is exited
 */
public class ControlledThreadPoolExecutorCloser implements AutoCloseable {
    private final ExecutorService[] executorServices;

    public ControlledThreadPoolExecutorCloser(ExecutorService... executorServices) {
        this.executorServices = executorServices;
    }

    @PreDestroy
    @Override
    public void close() throws InterruptedException {
        for (ExecutorService executorService : executorServices) {
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.HOURS);
            executorService.shutdownNow();
        }
    }
}