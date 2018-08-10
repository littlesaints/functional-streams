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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ForkTest {

    private final Integer[] input;

    private final AtomicReferenceArray<AtomicInteger> output = new AtomicReferenceArray <>(3);

    private final int expectedFork;

    private final ForkJoinPool pool = new ForkJoinPool(3);

    private CountDownLatch latch;

    private final Fork <Integer, BlockingQueue<Integer>> FIRST_MATCH_FORK;

    private final Fork <Integer, BlockingQueue<Integer>> ALL_MATCH_FORK;

    public ForkTest(String description, Integer[] input, int expectedFork) {
        this.input = input;
        this.expectedFork = expectedFork;
        FIRST_MATCH_FORK = Fork.<Integer>of(pool, true)
            .when(i -> i >= 0 && i < 10, s -> s.forEach( i -> {output.get(0).incrementAndGet(); latch.countDown();}))
            .when(i -> i >= 10 && i < 100, s -> s.forEach( i -> {output.get(1).incrementAndGet(); latch.countDown();}))
            .orDefault(s -> s.forEach( i -> {output.get(2).incrementAndGet(); latch.countDown();}));
        ALL_MATCH_FORK = Fork.<Integer>of(pool, false)
            .when(i -> i >= 0 && i < 10, s -> s.forEach( i -> {output.get(0).incrementAndGet(); latch.countDown();}))
            .when(i -> i >= 10 && i < 100, s -> s.forEach( i -> {output.get(1).incrementAndGet(); latch.countDown();}))
            .orDefault(s -> s.forEach( i -> {output.get(2).incrementAndGet(); latch.countDown();}));
    }

    @Parameterized.Parameters(name = "{index}: input={0} | expectedFork={2}")
    public static Collection<Object[]> data() {
        final List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"[-10 to 0]", IntStream.range(-10, 0).boxed().toArray(Integer[]::new), 2});
        data.add(new Object[]{"[0 to 10]", IntStream.range(0, 10).boxed().toArray(Integer[]::new), 0});
        data.add(new Object[]{"[10 to 20]", IntStream.range(10, 20).boxed().toArray(Integer[]::new), 1});
        data.add(new Object[]{"[100 to 110]", IntStream.range(100, 110).boxed().toArray(Integer[]::new), 2});
        data.add(new Object[]{"[-10 to 0]", IntStream.range(-10, 0).boxed().toArray(Integer[]::new), 2});
        data.add(new Object[]{"[0 to 10]", IntStream.range(0, 10).boxed().toArray(Integer[]::new), 0});
        data.add(new Object[]{"[10 to 20]", IntStream.range(10, 20).boxed().toArray(Integer[]::new), 1});
        data.add(new Object[]{"[100 to 110]", IntStream.range(100, 110).boxed().toArray(Integer[]::new), 2});
        Collections.shuffle(data);
        return data.stream().map(d -> new Object[]{d[0], d[1], d[2]}).collect(Collectors.toList());
    }

    @Before
    public void beforeTest() {
        IntStream.range(0, 3).forEach(i -> output.set(i, new AtomicInteger(0)));
        latch = new CountDownLatch(input.length);
    }

    @Test
    public void testFirstMatch() throws InterruptedException {
        test(FIRST_MATCH_FORK, input.length);
    }

//    @Test
    public void testAllMatch() throws InterruptedException {
        test(ALL_MATCH_FORK, input.length * 2);
        Assert.assertEquals(input.length, output.get(2).get());
    }

    private void test(Fork<Integer, BlockingQueue<Integer>> fork, int expectedMatches) throws InterruptedException {

        Stream <Integer> stream;
        (stream = Arrays.stream(input).onClose(fork::close)).forEach(fork);

        this.latch.await();

        AtomicInteger count = new AtomicInteger();
        AtomicInteger sum = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(3);

        IntStream.range(0, 3)
            .boxed()
            .map(output::get)
            .forEach(i -> {
                if (i.get() != 0) {
                    count.incrementAndGet();
                }
                sum.addAndGet(i.get());
                latch.countDown();
            });
        latch.await();

        stream.close();

        Assert.assertEquals(expectedMatches, sum.get());
        Assert.assertEquals(input.length, output.get(expectedFork).get());
        Assert.assertEquals(1, count.get());

//        pool.shutdown();
    }

}
