package com.dailu.nettyclient.utils;

import com.dailu.nettyclient.exception.CustomException;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Paginated job
 */
@SuppressWarnings("unused")
public class PaginatedJob<T> {
    private final Function<Pagination, Collection<T>> producer;
    private final Pagination pagination;
    private int limitTimes = Integer.MAX_VALUE;


    private final LinkedBlockingQueue<Object> concurrentQueue;

    private final Object emptyObject = new Object();

    private ExecutorService executor = Executors.newFixedThreadPool(5);

    /**
     * 分批次执行任务.
     *
     * @param batchSize 一批次处理的数据量
     * @param producer  数据源产生器. 一次只能产生batchSize对应的数据量，少了将停止循环，多了会产生bug。
     */
    public PaginatedJob(int batchSize, Function<Pagination, Collection<T>> producer) {
        this.producer = producer;
        this.concurrentQueue = new LinkedBlockingQueue<>(limitTimes);
        this.pagination = new Pagination(batchSize);
    }

    /**
     * 分批次执行任务.
     *
     * @param batchSize                  一批次处理的数据量
     * @param maxConcurrentEachLoopCount 最大并发循环数,小于0时使用Integer.MAX_VALUE
     * @param producer                   数据源产生器. 一次只能产生batchSize对应的数据量
     */
    public PaginatedJob(int batchSize, int maxConcurrentEachLoopCount, Function<Pagination, Collection<T>> producer) {
        this.producer = producer;
        maxConcurrentEachLoopCount = maxConcurrentEachLoopCount < 0 ? limitTimes : maxConcurrentEachLoopCount;
        this.concurrentQueue = new LinkedBlockingQueue<>(maxConcurrentEachLoopCount);
        this.pagination = new Pagination(batchSize);
    }

    /**
     * 设置最大循环次数，防止死循环。默认次数{@link Integer#MAX_VALUE 2147483647}.
     *
     * @param limitTimes the limit times
     * @return the paginal job
     */
    public PaginatedJob<T> timesLimit(int limitTimes) {
        this.limitTimes = limitTimes;
        return this;
    }

    /**
     * @param limitTimes            最大循环次数
     * @param maxConcurrentEachLoop 最大并发循环数
     */
    public PaginatedJob<T> timesLimit(int limitTimes, int maxConcurrentEachLoop) {
        this.limitTimes = limitTimes;
        return this;
    }

    private Collection<T> produceNextPage() {
        pagination.nextPage();
        Collection<T> elements = producer.apply(pagination);
        if (elements.size() > pagination.getLimit()) {
            throw new IllegalArgumentException("分页任务一次返回的数据量[" + elements.size() + "]大于batchSize[" + pagination.getLimit() + "]");
        }
        if (pagination.getPage() > limitTimes) {
            throw new IllegalStateException("分页任务已处理 " + limitTimes + " 批次数据，超过最大循环次数！请分析可能存在的bug或调整最大循环次数。");
        }
        return elements;
    }

    /**
     * 针对每一批数据进行处理.
     *
     * @param <E>      the type parameter
     * @param function 具体处理逻辑
     * @return the e
     */
    public <E extends Concatenate> E each(Function<Collection<T>, E> function) {
        Collection<T> elements;
        E result = null;
        do {
            elements = produceNextPage();
            E r = function.apply(elements);
            if (result == null) {
                result = r;
            } else {
                result.concat(r);
            }
        } while (elements.size() == pagination.getLimit());
        return result;
    }

    /**
     * 针对每一批数据进行处理. 不管返回值.
     *
     * @param consumer 具体处理逻辑
     */
    public void eachDo(Consumer<Collection<T>> consumer) {
        Collection<T> elements;
        do {
            elements = produceNextPage();
            consumer.accept(elements);
        } while (elements.size() == pagination.getLimit());
    }

    /**
     * 注意:
     * <p>针对每一批数据进行处理. 当循环次数超过concurrentQueue的size时即阻塞,</p>
     *
     * @param consumer 具体处理逻辑
     */
    public void eachDoLimited(Function<Collection<T>, List<Future<T>>> consumer) {
        Collection<T> elements;
        do {
            try {
                concurrentQueue.put(emptyObject);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw CustomException.wrap(e.getMessage(), e);
            }
            elements = produceNextPage();
            List<Future<T>> futures = consumer.apply(elements);
            FutureResults<T> tFutureResults = new FutureResults<>(futures);
            tFutureResults.extract();
        } while (elements.size() == pagination.getLimit());
    }


    public void reduceLoopQueue() {
        concurrentQueue.poll();
    }

    /**
     * The type Pagination.
     */
    public static class Pagination {
        /**
         * 页吗. 从1开始计数.
         */
        private int page = 0;
        /**
         * 分页大小.
         */
        private final int limit;


        /**
         * Instantiates a new Pagination.
         *
         * @param limit the limit
         */
        public Pagination(int limit) {
            this.limit = limit;
        }

        /**
         * Next page.
         */
        public void nextPage() {
            page++;
        }

        /**
         * 当前索引位置.
         *
         * @return the offset
         */
        public int getOffset() {
            return (page - 1) * limit;
        }

        /**
         * 当前页码. 从1开始计数.
         *
         * @return the page
         */
        public int getPage() {
            return page;
        }

        /**
         * 分页大小.
         *
         * @return the limit
         */
        public int getLimit() {
            return limit;
        }
    }

    /**
     * The interface Concatenate.
     */
    @FunctionalInterface
    @SuppressWarnings("UnusedReturnValue")
    public interface Concatenate {
        /**
         * Concat boolean.
         *
         * @param t the t
         * @return the boolean
         */
        boolean concat(Concatenate t);
    }

    @Getter
    class FutureResults<T> {
        private final List<Future<T>> futures;

        private FutureResults(List<Future<T>> futures) {
            if (CollectionUtils.isEmpty(futures)) {
                throw new IllegalArgumentException("futures can not be empty");
            }
            this.futures = futures;
        }

        public void extract() {
            executor.submit(() -> {
                futures.forEach(future -> {
                    try {
                        future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }
                        throw CustomException.wrap(e.getMessage(), e);
                    }
                });
                onFinished();
            });
        }

        private void onFinished() {
            concurrentQueue.poll();
        }
    }


    @Getter
    public class QueueFuture<T> extends FutureTask<T> {

        public QueueFuture(Callable<T> callable) {
            super(callable);
        }

        @Override
        public void done() {
            concurrentQueue.poll();
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {

            return super.get();
        }
    }
}
