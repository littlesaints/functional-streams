/*
 *                     functional-streams
 *              Copyright (C) 2018 Varun Anand
 *
 * This file is part of functional-streams.
 *
 * functional-streams is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * functional-streams is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.littlesaints.protean.functions.streams;

import lombok.Builder;
import lombok.NonNull;

import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * <pre>
 * Function to generate a <b>finite</b> Stream from non-compatible sources. e.g. streaming results from a database.
 *
 * It turns the push based approach of retrieving inputs to a Streams like pull based approach.
 * This means the application can wire the logic to pull inputs from a Stream but they'll be pulled only upon execution of the terminal operation of the Java Stream.
 *
 * Moreover, the application can stop the stream in the following ways:
 *
 * 1. providing a {@link Predicate} {@link #doWhile}. This will be checked before every call to {@link #provider}, to get the next element for the stream.
 * 2. invoking {@link Stream#close()}. This feature is not available in streams created by {@link Stream#generate(Supplier)}.
 * </pre>
 *
 * @author Varun Anand
 * @since 1.0
 */
@Builder
public class StreamSource<T> implements Supplier<Stream<T>>, AutoCloseable {

    @NonNull
    private final Supplier<T> provider;

    @Builder.Default
    private final Supplier<Boolean> doWhile = () -> Boolean.TRUE;

    @Builder.Default
    private final int parallelism = ForkJoinPool.getCommonPoolParallelism();

    private final AtomicBoolean isClosing = new AtomicBoolean(false);

    @Builder.Default
    private final int characteristics = 0;

    private class _Spliterator implements Spliterator<T> {

        private final Predicate<Consumer<? super T>> advanceAction = action -> {
            if (doWhile.get()) {
                action.accept(provider.get());
                return !isClosing.get();
            }
            return false;
        };

        private int estimatedSize;

        private _Spliterator(int estimatedSize) {
            this.estimatedSize = estimatedSize;
        }

        public boolean tryAdvance(Consumer<? super T> action) {
            return advanceAction.test(action);
        }

        @Override
        public Spliterator<T> trySplit() {
            return new _Spliterator(--estimatedSize);
        }

        @Override
        public long estimateSize() {
            return estimatedSize;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }
    }

    @Override
    public void close() {
        isClosing.set(true);
    }

    public Stream<T> get() {
        final _Spliterator spliterator = new _Spliterator(parallelism);
        return StreamSupport.stream(spliterator, false).onClose(this::close);
    }

}
