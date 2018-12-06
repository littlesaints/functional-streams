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

import javax.sql.rowset.Predicate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RunWith(Parameterized.class)
public class TryTest {

    private final String input;

    private final Boolean expected;

    private static final Try <String, Integer, Optional<Boolean>> TRY = Try.wrapWithOptional(Try.<String, Integer, Boolean>of(Integer::parseInt)
        .onSuccess(i -> true)
        .onFailure((i, e) -> e == null)
        .onFinally(i -> System.out.println("Finally :: ".concat(i))));

    private static final Try <String, Integer, Optional<Boolean>> TRY_NO_ON_SUCCESS = Try.wrapWithOptional(Try.<String, Integer, Boolean>of(Integer::parseInt)
            .onFailure((i, e) -> e == null)
            .onFinally(i -> System.out.println("Finally :: ".concat(i))));

    private static final Try <String, Integer, Optional<Boolean>> TRY_NO_ON_FAILURE = Try.wrapWithOptional(Try.<String, Integer, Boolean>of(Integer::parseInt)
            .onSuccess(i -> true)
            .onFinally(i -> System.out.println("Finally :: ".concat(i))));

    private static final Try <String, Integer, Optional<Boolean>> TRY_NO_ON_FINALLY = Try.wrapWithOptional(
            Try.<String, Integer, Boolean>of(s -> {
                if (s.startsWith("XX")) {
                    throw new Error();
                } else {
                    return Integer.parseInt(s);
                }})
            .onSuccess(i -> true)
            .onFailure((i, e) -> e == null));

    public TryTest(String input, Boolean expected) {
        this.input = input;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "{index}: input={0} | expected={1}")
    public static Collection<Object[]> data() {
        final List<Object[]> data = new ArrayList<>();
        IntStream.range(0, 100).boxed().map(Objects::toString).forEach(i -> data.add(new Object[]{i, true}));
        IntStream.range(0, 100).boxed().map(Objects::toString).map("XX"::concat).forEach(i -> data.add(new Object[]{i, false}));
        Collections.shuffle(data);
        return data.stream().map(d -> new Object[]{d[0], d[1]}).collect(Collectors.toList());
    }

    @Test
    public void test() {
        Assert.assertEquals(expected,
            Stream.of(input)
                .map(TRY)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny()
                .orElse(null));
    }

    @Test
    public void testNoSuccess() {
        Assert.assertFalse(
            Stream.of(input)
                .map(TRY_NO_ON_SUCCESS)
                .map(o -> o.orElse(false))
                .findAny()
                .orElse(null));
    }

    @Test
    public void testNoFailure() {
        Assert.assertTrue(
            Stream.of(input)
                .map(TRY_NO_ON_FAILURE)
                .map(o -> o.orElse(true))
                .findAny()
                .orElse(null));
    }

    @Test
    public void testNoFinally() {
        Assert.assertEquals(expected,
            Stream.of(input)
                .map(TRY_NO_ON_FINALLY)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny()
                .orElse(null));
    }

}
