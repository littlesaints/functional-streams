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

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * <pre>
 * This class facilitates combining multiple {@link Stream}s of input.
 *
 * It can be used in the following ways:
 *
 * - Fork (create) multiple streams from a single stream.
 *   The application can configure multiple forks, with or without predicates.
 *   If a predicate is configured, the forked stream will receive the input only when the predicate is satisfied i.e. return 'true'.
 *
 *   Usage:
 *
 *   {@code
 *   IntStream.range(- 10, 10).boxed().forEach(
 *     ForkJoin. < Integer > newInstance ()
 *          // Fork with predicate
 *         .fork(input -> input < 0,
 *             stream -> stream.map(Objects::toString).map("negative :: "::concat).forEach(System.out::println))
 *         .fork(input -> input >= 0 && input < 5,
 *             stream -> stream.map(Objects::toString).map("small positive:: "::concat).forEach(System.out::println))
 *          // Fork without any predicate i.e. always true
 *         .fork(stream -> stream.map(Objects::toString).map("always true:: "::concat).forEach(System.out::println))
 *     );
 *   }
 *
 * - Join multiple streams in a single stream.
 *   The application can configure multiple forks, ideally with a predicate / filter which fork met, would push the input in it's corresponding stream.
 *
 *   Usage:
 *
 *   {@code
 *   Consumer<Integer> forkJoin = ForkJoin. < Integer > newInstance ()
 *          .fork(stream -> stream.map(Objects::toString).map("always true:: "::concat).forEach(System.out::println));
 *   IntStream.range(- 10, 0).boxed().forEach(forkJoin);
 *   IntStream.range(0, 10).boxed().forEach(forkJoin);
 *
 * - Combine 'm' input streams into 'n' output streams.
 *
 *   Usage:
 *
 *   {@code
 *   Consumer<Integer> forkJoin = ForkJoin. < Integer > newInstance ()
 *          // Fork with predicate
 *         .fork(input -> input < 0,
 *             stream -> stream.map(Objects::toString).map("negative :: "::concat).forEach(System.out::println))
 *         .fork(input -> input >= 0 && input < 5,
 *             stream -> stream.map(Objects::toString).map("small positive:: "::concat).forEach(System.out::println))
 *          // Fork without any predicate i.e. always true
 *         .fork(stream -> stream.map(Objects::toString).map("always true:: "::concat).forEach(System.out::println));
 *
 *   IntStream.range(- 10, 0).boxed().forEach(forkJoin);
 *   IntStream.range(0, 10).boxed().forEach(forkJoin);
 * </pre>
 *
 * @author Varun Anand
 * @since 1.0
 */
public class ForkJoin<T, Q> implements Consumer<T>, AutoCloseable {

    @Getter
    private final String name = String.valueOf(System.currentTimeMillis());

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final Supplier<Q> exchangeProvider;

    private final Function<Q, Supplier<T>> exchangeReaderProvider;

    private final Function<Q, Consumer<T>> exchangeWriterProvider;

    private final Map <Predicate <T>, Consumer <T>> predicates = new LinkedHashMap <>(1);

    private final Collection<Future<?>> forks = new ArrayList <>(2);

    private final Collection<Stream<T>> streams = new ArrayList <>(2);

    private final Consumer <T> acceptAction;

    private ForkJoin(Supplier <Q> exchangeProvider, Function <Q, Supplier <T>> exchangeReaderProvider,
                     Function <Q, Consumer <T>> exchangeWriterProvider) {
        this.exchangeProvider = exchangeProvider;
        this.exchangeReaderProvider = exchangeReaderProvider;
        this.exchangeWriterProvider = exchangeWriterProvider;
        this.acceptAction = t -> predicates.entrySet().stream().sequential()
            .filter(e -> e.getKey().test(t))
            .map(Entry::getValue)
            .forEach(c -> c.accept(t));
    }

    /**
     * Create a ForkJoin instance.
     *
     * @param <T>
     * @return a ForkJoin instance
     */
    public static <T> ForkJoin<T, BlockingQueue<T>> newInstance() {
        return ForkJoin.of(LinkedTransferQueue::new);
    }

    /**
     * Create a ForkJoin instance.
     *
     * @param exchangeProvider a supplier for the BlockingQueue to be used for input exchange between the source {@link Stream}
     * and the target 'matching' {@link Stream}s of the forks.
     * @param <T>
     * @return a ForkJoin instance
     */
    public static <T> ForkJoin<T, BlockingQueue<T>> of(Supplier<BlockingQueue<T>> exchangeProvider) {
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
        return ForkJoin.of(exchangeProvider, reader, writer);
    }

    private static <T, Q> ForkJoin<T, Q> of(Supplier<Q> exchangeProvider, Function<Q, Supplier<T>> exchangeReaderProvider,
                                            Function<Q, Consumer<T>> exchangeWriterProvider) {
        return new ForkJoin<>(exchangeProvider, exchangeReaderProvider, exchangeWriterProvider);
    }

    /**
     * Configure a match case or fork.
     *
     * @param predicate the matching condition or filter.
     * @param streamProcessor The processing on the {@link Stream} that will have the input value of the predicate matches.
     * @return this ForkJoin instance
     */
    public ForkJoin<T, Q> fork(Predicate <T> predicate, Consumer <Stream <T>> streamProcessor) {
        final Q messageExchange = exchangeProvider.get();
        final Stream<T> stream = StreamSource.of(exchangeReaderProvider.apply(messageExchange)).get();
        streams.add(stream);
        predicates.put(predicate, exchangeWriterProvider.apply(messageExchange));
        forks.add(executor.submit(() -> streamProcessor.accept(stream)));
        return this;
    }

    /**
     * <pre>
     * The default case, which matches fork no other 'fork' case does.
     * This should be configured after all 'fork' cases have been configured, in code.
     * </pre>
     * @param streamProcessor the consumer of the resulting stream
     * @return this ForkJoin instance
     */
    public ForkJoin<T, Q> fork(Consumer <Stream <T>> streamProcessor) {
        fork(t -> true, streamProcessor);
        return this;
    }

    @Override
    public void accept(T t) {
        acceptAction.accept(t);
    }

    @Override
    public void close() {
        streams.forEach(Stream::close);
        forks.forEach(f -> f.cancel(true));
    }
}