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
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * <pre>
 * A closeable stream generator.
 * It facilitates building a {@link Stream} from any source, even the ones that don't have a Streaming API.
 * e.g. streaming results from a database.
 * </pre>
 * @author Varun Anand
 * @since 0.1
 */
public class StreamHead<T> implements Supplier<Stream<T>>, AutoCloseable {

	private final Spliterator<T> spliterator;

    private final Collection<Stream<T>> streams = new ArrayList <>();

    private final AtomicBoolean isClosing = new AtomicBoolean(false);

    private final Runnable onClose;

    private class _Spliterator implements Spliterator<T> {

		private final Predicate<Consumer<? super T>> advanceAction;

		private _Spliterator(Supplier <T> reader) {
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
	}

    public static <T> StreamHead<T> of(Supplier<T> reader) {
        return new StreamHead<>(reader, () -> {});
    }

	public static <T> StreamHead<T> of(Supplier<T> reader, Runnable onClose) {
        return new StreamHead<>(reader, onClose);
    }

	private StreamHead(Supplier<T> reader, Runnable onClose) {
        this.spliterator = new _Spliterator(reader);
        this.onClose = onClose;
    }

	public Stream<T> get() {
        final Stream<T> s = StreamSupport.stream(spliterator, false);
        streams.add(s);
        return s;
    }

    @Override
	public void close() {
        isClosing.set(true);
        streams.forEach(Stream::close);
        onClose.run();
    }

}
