/*
 *                     functional-streams
 *              Copyright (C) 2018  Varun Anand
 *
 * This file is part of  functional-streams.
 *
 *  functional-streams is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 *  functional-streams is distributed in the hope that it will be useful,
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RunWith(Parameterized.class)
public class ForkJoinTest {

    private final AtomicReferenceArray<AtomicInteger> output = new AtomicReferenceArray <>(3);

    private final int[] expectedOutput = new int[] {50, 50, 200};

    private final CountDownLatch latch = new CountDownLatch(300);

    private final ForkJoin<Integer, BlockingQueue<Integer>> forkJoin;

    public ForkJoinTest(String description) {
        forkJoin = ForkJoin.<Integer>newInstance()
            .fork(i -> i >= 0 && i < 50, s -> s.forEach(i -> {output.get(0).incrementAndGet(); latch.countDown();}))
            .fork(i -> i >= 50 && i < 100, s -> s.forEach(i -> {output.get(1).incrementAndGet(); latch.countDown();}))
            .fork(s -> s.forEach(i -> {output.get(2).incrementAndGet(); latch.countDown();}));
        IntStream.range(0, 3).forEach(i -> output.set(i, new AtomicInteger(0)));
    }

    @Parameterized.Parameters(name = "{index}: run={0}")
    public static Collection<Object[]> data() {
        final List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"1"});
        data.add(new Object[]{"2"});
        data.add(new Object[]{"3"});
        data.add(new Object[]{"4"});
        data.add(new Object[]{"3"});
        return data;
    }

    @Test(timeout = 5000)
    public void test() throws InterruptedException {
        Stream <Integer> stream;
        (stream = IntStream.range(-100, 100).boxed().parallel().onClose(forkJoin::close)).forEach(forkJoin);

        this.latch.await();

        stream.close();

        Assert.assertEquals(expectedOutput[0], output.get(0).get());
        Assert.assertEquals(expectedOutput[1], output.get(1).get());
        Assert.assertEquals(expectedOutput[2], output.get(2).get());
    }

}
