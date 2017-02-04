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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Controlled Thread Pool Executor
 * By default, Java's executors won't block the submitter when the queue is full
 * This class implements a safer mechanism
 */
public class ControlledThreadPoolExecutor extends ThreadPoolExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControlledThreadPoolExecutor.class);

    private final Semaphore semaphore;
    private final String name;
    private final AtomicLong processed = new AtomicLong(0);
    private final int numberOfThreads;

    public ControlledThreadPoolExecutor(String name, int numberOfThread, int queueSize) {
        super(numberOfThread, numberOfThread, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>(queueSize));
        this.semaphore = new Semaphore(queueSize);
        this.name = name;
        this.numberOfThreads = numberOfThread;
    }

    @Override
    public void execute(Runnable runnable) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            this.shutdownNow();
        }
        super.execute(runnable);
    }

    @Override
    public void afterExecute(Runnable runnable, Throwable throwable) {
        super.afterExecute(runnable, throwable);
        if (throwable != null) {
            LOGGER.error(ExceptionUtils.getStackTrace(throwable));
        }
        semaphore.release();
        processed.addAndGet(1);
    }

    /**
     * Get the number of tasks the pool has processed
     *
     * @return The number of processed tasks
     */
    public long getNumberOfProcessedTasks() {
        return processed.get();
    }

    /**
     * Get the total size of the queue
     *
     * @return the size of the queue
     */
    public int getQueueSize() {
        return this.getQueue().size() + this.getQueue().remainingCapacity();
    }

    /**
     * Get the number of items in the queue
     *
     * @return the current number of items in the queue
     */
    public int getQueueNumberOfItems() {
        return this.getQueue().size() + this.numberOfThreads;
    }

    /**
     * @return The name of the pool
     */
    public String getName() {
        return name;
    }
}