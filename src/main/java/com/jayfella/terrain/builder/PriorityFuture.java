package com.jayfella.terrain.builder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by James on 29/04/2017.
 */
public class PriorityFuture<T> implements RunnableFuture<T> {

    private RunnableFuture<T> src;
    private int priority;

    public PriorityFuture(RunnableFuture<T> other, int priority) {
        this.src = other;
        this.priority = priority;


    }

    public int getPriority() {
        return this.priority;
    }


    @Override
    public void run() {
        src.run();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return src.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return src.isCancelled();
    }

    @Override
    public boolean isDone() {
        return src.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return src.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return src.get();
    }
}
