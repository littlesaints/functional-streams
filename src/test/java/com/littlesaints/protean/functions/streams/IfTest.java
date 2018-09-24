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
public class IfTest {

    private final int input;

    private final String expected;

    private static final If <Integer, String> IF = If. <Integer, String>test(i -> i >= 10)
        .then(i -> "i >= 10")
        .elseIf(i -> i >= 5 && i < 10, i -> "5 <= i < 10")
        .elseIf(i -> i >= 0 && i < 5, i -> "0 <= i < 5")
        .orElse(i -> "i < 0");

    private static final If <Integer, String> IFOptional = If. <Integer, String>test(i -> i >= 10)
            .thenO(i -> Optional.of("i >= 10"))
            .elseIfO(i -> i >= 5 && i < 10, i -> Optional.of("5 <= i < 10"))
            .elseIf(i -> i >= 0 && i < 5, i -> "0 <= i < 5")
            .elseO(i -> Optional.of("i < 0"));

    private static final If <Integer, String> IFWithoutThen = If. <Integer, String>test(i -> i >= 999999)
            .elseIf(i -> i >= 10, i -> "i >= 10")
            .elseIf(i -> i >= 5 && i < 10, i -> "5 <= i < 10")
            .elseIf(i -> i >= 0 && i < 5, i -> "0 <= i < 5")
            .elseIf(i -> i < 0, i -> "i < 0");

    public IfTest(int input, String expected) {
        this.input = input;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "{index}: input={0} | expected={1}")
    public static Collection <Object[]> data() {
        final List <Object[]> data = new ArrayList <>();
        IntStream.range(-10, 0).forEach(i -> data.add(new Object[]{i, "i < 0"}));
        IntStream.range(0, 5).forEach(i -> data.add(new Object[]{i, "0 <= i < 5"}));
        IntStream.range(5, 10).forEach(i -> data.add(new Object[]{i, "5 <= i < 10"}));
        IntStream.range(10, 15).forEach(i -> data.add(new Object[]{i, "i >= 10"}));
        Collections.shuffle(data);
        return data.stream().map(d -> new Object[]{d[0], d[1]}).collect(Collectors.toList());
    }

    @Test
    public void test() {
        Assert.assertEquals(expected,
            Stream.of(input)
                .map(IF)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny()
                .orElse("unknown"));
    }

    @Test
    public void testOptional() {
        Assert.assertEquals(expected,
                Stream.of(input)
                        .map(IFOptional)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findAny()
                        .orElse("unknown"));
    }

    @Test
    public void testNoThen() {
        Assert.assertEquals(expected,
                Stream.of(input)
                        .map(IFWithoutThen)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findAny()
                        .orElse("unknown"));
    }

}
