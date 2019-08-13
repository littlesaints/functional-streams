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

import org.junit.Assert;
import org.junit.Test;

import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

public class StreamSourceTest {

    @Test(timeout = 5000)
    public void testStreamClose() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        final Stream<Integer> source = StreamSource.<Integer>builder()
                .provider(atomicInteger::incrementAndGet)
                .build().get();
        final int limit = 100;
        final If<Integer, Void> iff = If.<Integer, Void>test(n -> n.equals(limit)).then(n -> {
            source.close();
            return null;
        });
        source.forEach(iff::apply);
        Assert.assertEquals(limit, atomicInteger.get());
    }

    @Test(timeout = 5000)
    public void testStreamComplete() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        final int limit = 100;
        final LongAdder count = new LongAdder();
        final Stream<Integer> source = StreamSource.<Integer>builder()
                .provider(atomicInteger::incrementAndGet)
                .doWhile(() -> atomicInteger.get() < limit)
                .build().get();
        source.forEach(n -> count.increment());
        Assert.assertEquals(limit, atomicInteger.get());
        Assert.assertEquals(limit, count.intValue());
    }

    @Test(timeout = 5000)
    public void testParallelStreamComplete() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        final int limit = 100;
        final LongAdder count = new LongAdder();
        final StreamSource.StreamSourceBuilder<Integer> builder = StreamSource.<Integer>builder()
                .provider(atomicInteger::incrementAndGet)
                .doWhile(() -> atomicInteger.get() < limit)
                .parallelism(2);
        Assert.assertNotNull(builder.toString());
        final Stream<Integer> source = builder.build().get().parallel();
        source.forEach(n -> count.increment());
        Assert.assertEquals(limit, atomicInteger.get());
        Assert.assertEquals(limit, count.intValue());
    }

    @Test(timeout = 5000)
    public void testParallelCharacteristicsStreamComplete() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        final int limit = 100;
        final LongAdder count = new LongAdder();
        final Stream<Integer> source = StreamSource.<Integer>builder()
                .provider(atomicInteger::incrementAndGet)
                .doWhile(() -> atomicInteger.get() < limit)
                .characteristics(Spliterator.DISTINCT)
                .parallelism(2)
                .build().get().parallel();
        source.forEach(n -> count.increment());
        Assert.assertEquals(limit, atomicInteger.get());
        Assert.assertEquals(limit, count.intValue());
    }

    @Test(timeout = 5000)
    public void testCharacteristicsStreamComplete() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        final int limit = 100;
        final LongAdder count = new LongAdder();
        final Stream<Integer> source = StreamSource.<Integer>builder()
                .provider(atomicInteger::incrementAndGet)
                .doWhile(() -> atomicInteger.get() < limit)
                .characteristics(Spliterator.DISTINCT)
                .build().get().parallel();
        source.forEach(n -> count.increment());
        Assert.assertEquals(limit, atomicInteger.get());
        Assert.assertEquals(limit, count.intValue());
    }
}
