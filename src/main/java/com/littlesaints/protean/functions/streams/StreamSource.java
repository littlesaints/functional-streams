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

import java.util.Spliterator;
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
 * Moreover, the application can stop the stream by invoking {@link Stream#close()} unlike for streams created by {@link Stream#generate(Supplier)}.
 * </pre>
 *
 * @author Varun Anand
 * @since 1.0
 */
public class StreamSource<T> implements Supplier<Stream<T>> {

    private final Supplier<T> reader;

    private final Supplier<Boolean> doContinue;

    private class _Spliterator implements Spliterator<T>, AutoCloseable {

        private final Predicate<Consumer<? super T>> advanceAction;

        private final AtomicBoolean isClosing = new AtomicBoolean(false);

        private _Spliterator(Supplier<T> reader, Supplier<Boolean> doContinue) {
            advanceAction = action -> {
                T val = reader.get();
                if (doContinue.get()) {
                    action.accept(val);
                    return !isClosing.get();
                }
                return false;
            };
        }

        private _Spliterator(Supplier<T> reader) {
            advanceAction = action -> {
                action.accept(reader.get());
                return !isClosing.get();
            };
        }

        public boolean tryAdvance(Consumer<? super T> action) {
            return advanceAction.test(action);
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return 0;
        }

        @Override
        public int characteristics() {
            return 0;
        }

        @Override
        public void close() {
            isClosing.set(true);
        }
    }

    public static <T> StreamSource<T> of(Supplier<T> reader) {
        return new StreamSource<>(reader, null);
    }

    public static <T> StreamSource<T> of(Supplier<T> reader, Supplier<Boolean> doContinue) {
        return new StreamSource<>(reader, doContinue);
    }

    private StreamSource(Supplier<T> reader, Supplier<Boolean> doContinue) {
        this.reader = reader;
        this.doContinue = doContinue;
    }

    public Stream<T> get() {
        final _Spliterator spliterator;
        if (doContinue == null) {
            spliterator = new _Spliterator(reader);
        } else {
            spliterator = new _Spliterator(reader, doContinue);
        }
        return StreamSupport.stream(spliterator, false).onClose(spliterator::close);
    }

}
