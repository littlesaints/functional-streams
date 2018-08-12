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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.Getter;

/**
 * <pre>
 * Create multiple streams from a single stream of inputs.
 * The application can configure multiple forks, each with a condition which when met, would push the input in it's corresponding stream.
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
 *
 * - If 'firstMatchOnly' is 'true', the input flows in 'only' the first stream whose predicate matches first as per the order of configuration
 *   otherwise it streams to all the 'when' streams whose predicate match (including the 'default' case).
 * </pre>
 *
 * @author Varun Anand
 * @since 1.0
 */
public class Fork<T, Q> implements Consumer<T>, AutoCloseable {

    @Getter
    private final String name = String.valueOf(System.currentTimeMillis());

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final Supplier<Q> exchangeProvider;

    private final Function<Q, Supplier<T>> exchangeReaderProvider;

    private final Function<Q, Consumer<T>> exchangeWriterProvider;

    private final Map <Predicate <T>, Consumer <T>> predicates = new LinkedHashMap <>(1);

    private final Collection<Future<?>> forks = new ArrayList <>(2);

    private final Collection<StreamHead <T>> streamHeads = new ArrayList <>(2);

    private final Consumer <T> acceptAction;

    private boolean defaultSet;

    private Fork(Supplier <Q> exchangeProvider, Function <Q, Supplier <T>> exchangeReaderProvider,
        Function <Q, Consumer <T>> exchangeWriterProvider, boolean firstMatchOnly) {
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

    /**
     * Create a Fork instance.
     *
     * @param firstMatchOnly set to 'true' if the input needs to be processed only by the first configured match / fork
     * or 'false' if the input needs to be processed by all matching forks (including if any 'default').
     * @param <T>
     * @return a Fork instance
     */
    public static <T> Fork <T, BlockingQueue<T>> of(boolean firstMatchOnly) {
        return Fork.of(LinkedTransferQueue::new, firstMatchOnly);
    }

    /**
     * Create a Fork instance.
     *
     * @param exchangeProvider a supplier for the BlockingQueue to be used for input exchange between the source {@link Stream}
     * and the target 'matching' {@link Stream}s of the forks.
     * @param firstMatchOnly set to 'true' if the input needs to be processed only by the first configured match / fork
     * or 'false' if the input needs to be processed by all matching forks (including if any 'default').
     * @param <T>
     * @return a Fork instance
     */
    public static <T> Fork <T, BlockingQueue<T>> of(Supplier<BlockingQueue<T>> exchangeProvider, boolean firstMatchOnly) {
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
        return Fork.of(exchangeProvider, reader, writer, firstMatchOnly);
    }

    private static <T, Q> Fork <T, Q> of(Supplier<Q> exchangeProvider, Function<Q, Supplier<T>> exchangeReaderProvider,
        Function<Q, Consumer<T>> exchangeWriterProvider, boolean firstMatchOnly) {
        return new Fork <>(exchangeProvider, exchangeReaderProvider, exchangeWriterProvider, firstMatchOnly);
    }

    /**
     * Configure a match case or fork.
     *
     * @param predicate the matching condition or filter.
     * @param streamProcessor The processing on the {@link Stream} that will have the input value of the predicate matches.
     * @return this Fork instance
     */
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