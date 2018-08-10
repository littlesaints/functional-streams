/*
 *                     functional-streams
 *               Copyright (C) 2018  Varun Anand
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.littlesaints.protean.functions.streams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * <pre>
 * The purpose of a Fork is to provide alternate condition data pipelines.
 * This can be used in a terminal operation of a java streams pipeline to provide conditional streams for further processing.
 *
 * Usage:
 *
 * {@code
 * boolean firstMatchOnly = false;
 * IntStream.range(- 10, 10).boxed().forEach(
 *     Fork. < Integer > of (firstMatchOnly)
 *         .when(input -> input < 0,
 *             stream -> stream.map(Objects::toString).map("negative :: "::concat).forEach(System.out::println))
 *         .when(input -> input < 5,
 *             stream -> stream.map(Objects::toString).map("small positive:: "::concat).forEach(System.out::println))
 *         .orDefault(stream -> stream.map(Objects::toString).map("big positive:: "::concat).forEach(System.out::println))
 *     );
 * }
 *
 * Note:
 * - If 'firstMatchOnly' is 'true', the input flows in 'only' the first stream whose predicate matches first as per the order of configuration
 * otherwise it streams to all the 'when' streams whose predicate match (including the 'default' case).
 * </pre>
 *
 * @author Varun Anand
 * @since 0.1
 */
public class Fork<T, Q> implements Consumer<T>, AutoCloseable {

    private final ExecutorService executor;

    private final Supplier<Q> exchangeProvider;

    private final  Function<Q, Supplier<T>> exchangeReaderProvider;

    private final Function<Q, Consumer<T>> exchangeWriterProvider;

    private final Map <Predicate <T>, Consumer <T>> predicates = new LinkedHashMap <>(1);

    private final Collection<Future<?>> forks = new ArrayList <>(2);

    private final Collection<StreamHead <T>> streamHeads = new ArrayList <>(2);

    private final Consumer <T> acceptAction;

    private boolean defaultSet;

    private Fork(Supplier <Q> exchangeProvider, Function <Q, Supplier <T>> exchangeReaderProvider,
        Function <Q, Consumer <T>> exchangeWriterProvider, ExecutorService executor, boolean firstMatchOnly) {
        this.executor = executor;
        this.exchangeProvider = exchangeProvider;
        this.exchangeReaderProvider = exchangeReaderProvider;
        this.exchangeWriterProvider = exchangeWriterProvider;
        if (firstMatchOnly) {
            this.acceptAction = t -> predicates.entrySet().stream().sequential()
                    .filter(e -> e.getKey().test(t))
                    .findFirst()
                    .ifPresent(e -> e.getValue().accept(t));
        } else {
            this.acceptAction = t -> predicates.entrySet().stream().sequential()
                .filter(e -> e.getKey().test(t))
                .map(Entry::getValue)
                .forEach(c -> c.accept(t));
        }
    }

    public static <T> Fork <T, BlockingQueue<T>> of(boolean firstMatchOnly) {
        return Fork.of(ForkJoinPool.commonPool(), firstMatchOnly);
    }

    public static <T> Fork <T, BlockingQueue<T>> of(ExecutorService executor, boolean firstMatchOnly) {
        return Fork.of(LinkedBlockingQueue::new, executor, firstMatchOnly);
    }

    public static <T> Fork <T, BlockingQueue<T>> of(Supplier<BlockingQueue<T>> exchangeProvider, ExecutorService executor, boolean firstMatchOnly) {
        final Function<BlockingQueue<T>, Supplier<T>> reader = q -> () -> {
            while (true) {
                try {
                    return q.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        final Function<BlockingQueue<T>, Consumer<T>> writer = q -> t -> {
            while (true) {
                try {
                    q.put(t);
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        return Fork.of(exchangeProvider, reader, writer, executor, firstMatchOnly);
    }

    public static <T, Q> Fork <T, Q> of(Supplier<Q> exchangeProvider, Function<Q, Supplier<T>> exchangeReaderProvider,
        Function<Q, Consumer<T>> exchangeWriterProvider, ExecutorService executor, boolean firstMatchOnly) {
        return new Fork <>(exchangeProvider, exchangeReaderProvider, exchangeWriterProvider, executor, firstMatchOnly);
    }

    public Fork <T, Q> when(Predicate <T> predicate, Consumer <Stream <T>> streamProcessor) {
        if (defaultSet) {
            throw new IllegalStateException("Default is already set !! 'orDefault' case should be set ONLY after all 'when' cases have been configured.");
        }
        final Q messageExchange = exchangeProvider.get();
        final StreamHead <T> streamHead = StreamHead.of(exchangeReaderProvider.apply(messageExchange));
        streamHeads.add(streamHead);
        predicates.put(predicate, exchangeWriterProvider.apply(messageExchange));
        forks.add(executor.submit(() -> streamProcessor.accept(streamHead.get())));
        return this;
    }

    /**
     * <pre>
     * The default case, which matches when no other 'when' case does.
     * This should be configured after all 'when' cases have been configured, in code.
     * </pre>
     * @param streamProcessor the consumer of the resulting stream
     * @return this Fork instance
     */
    public Fork <T, Q> orDefault(Consumer <Stream <T>> streamProcessor) {
        when(t -> true, streamProcessor);
        defaultSet = true;
        return this;
    }

    @Override
    public void accept(T t) {
        acceptAction.accept(t);
    }

    @Override
    public void close() {
        streamHeads.forEach(StreamHead::close);
        forks.forEach(f -> f.cancel(true));
    }
}