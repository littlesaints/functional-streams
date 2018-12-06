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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RunWith(Parameterized.class)
public class SwitchTest {

    private final int input;

    private final String expected;

    private static final Switch <Integer, Integer, String> SWITCH = Switch.<Integer, Integer, String>evaluate(Math::abs)
        .when(i -> i >= 0 && i < 10 , (ii, ll) -> "units")
        .when(i -> i >= 10 && i < 100 , (ii, ll) -> "tens")
        .orDefault((i, l) -> "hundreds or more");

    private static final Switch <Integer, Integer, Optional<String>> SWITCHOptional = Switch.wrapWithOptional(Switch.<Integer, Integer, String>evaluate(Math::abs)
        .when(i -> i >= 0 && i < 10 , (ii, ll) -> "units")
        .when(i -> i >= 10 && i < 100 , (ii, ll) -> "tens")
        .orDefault((i, l) -> "hundreds or more"));

    private static final Switch <Integer, Integer, String> SWITCHNoDefault = Switch.<Integer, Integer, String>evaluate(Math::abs)
        .when(i -> i >= 0 && i < 10 , (ii, ll) -> "units")
        .when(i -> i >= 10 && i < 100 , (ii, ll) -> "tens");

    public SwitchTest(int input, String expected) {
        this.input = input;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "{index}: input={0} | expected={1}")
    public static Collection<Object[]> data() {
        final List<Object[]> data = new ArrayList<>();
        IntStream.range(-20, -10).forEach(i -> data.add(new Object[]{i, "tens"}));
        IntStream.range(-9, 10).forEach(i -> data.add(new Object[]{i, "units"}));
        IntStream.range(10, 100).forEach(i -> data.add(new Object[]{i, "tens"}));
        IntStream.range(100, 110).forEach(i -> data.add(new Object[]{i, "hundreds or more"}));
        IntStream.range(1000, 1010).forEach(i -> data.add(new Object[]{i, "hundreds or more"}));
        Collections.shuffle(data);
        return data.stream().map(d -> new Object[]{d[0], d[1]}).collect(Collectors.toList());
    }

    @Test
    public void test() {
        Assert.assertEquals(expected,
            Stream.of(input)
                .map(SWITCH)
                .findAny()
                .orElse("unknown"));
    }

    @Test
    public void testOptional() {
        Assert.assertEquals(expected,
            Stream.of(input)
                .map(SWITCHOptional)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny()
                .orElse("unknown"));
    }

    @Test
    public void testNoDefault() {
        Assert.assertEquals(expected,
            Stream.of(input)
                .map(SWITCHNoDefault)
                .map(s -> s == null ? "hundreds or more" : s)
                .findAny()
                .orElse("unknown"));
    }
}
