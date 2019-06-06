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
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TryTest {

    private String successInput;

    private Integer successOutcome;

    private String finallyInput;

    private final BiConsumer<String, Integer> successOp = (i, b) -> {
        successInput = i;
        successOutcome = b;
    };

    private BiFunction<String, Exception, Integer> failureOp = (i, e) -> e instanceof NumberFormatException ? Integer.MIN_VALUE : Integer.MAX_VALUE;

    private final Consumer<String> finallyOp = i -> finallyInput = i;

    private final Try <String, Optional<Integer>> TRY = Try.wrapWithOptional(
        Try.<String, Integer>evaluate(s -> {
            if (s == null) {
                throw new Error();
            } else {
                return Integer.parseInt(s);
            }})
        .onSuccess(successOp)
        .onFailure(failureOp)
        .onFinally(finallyOp));

    private final Try <String, Optional<Integer>> TRY_NO_ON_SUCCESS = Try.wrapWithOptional(Try.<String, Integer>evaluate(Integer::parseInt)
            .onFailure(failureOp)
            .onFinally(finallyOp));

    private final Try <String, Optional<Integer>> TRY_NO_ON_FAILURE = Try.wrapWithOptional(Try.<String, Integer>evaluate(Integer::parseInt)
            .onSuccess(successOp)
            .onFinally(finallyOp));

    private final Try <String, Optional<Integer>> TRY_NO_ON_FINALLY = Try.wrapWithOptional(
            Try.<String, Integer>evaluate(Integer::parseInt)
            .onSuccess(successOp)
            .onFailure(failureOp));

    @Before
    public void init() {
        successInput = finallyInput = null;
        successOutcome = null;
    }

    @Test
    public void testTrySuccess() {
        final String input = "1";
        final Integer expected = 1;

        Integer actual = Optional.of(input).flatMap(TRY).get();
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(input, successInput);
        Assert.assertEquals(expected, successOutcome);
        Assert.assertEquals(input, finallyInput);

        init();
        actual = Optional.of(input).flatMap(TRY_NO_ON_SUCCESS).get();
        Assert.assertEquals(expected, actual);
        Assert.assertNull(successInput);
        Assert.assertNull(successOutcome);
        Assert.assertEquals(input, finallyInput);

        init();
        actual = Optional.of(input).flatMap(TRY_NO_ON_FAILURE).get();
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(input, successInput);
        Assert.assertEquals(expected, successOutcome);
        Assert.assertEquals(input, finallyInput);

        init();
        actual = Optional.of(input).flatMap(TRY_NO_ON_FINALLY).get();
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(input, successInput);
        Assert.assertEquals(expected, successOutcome);
        Assert.assertNull(finallyInput);

    }

    @Test
    public void testTryFailureExpected() {
        final String input = "X";
        final Integer expected = Integer.MIN_VALUE;

        Integer actual = Optional.of(input).flatMap(TRY).get();
        Assert.assertEquals(expected, actual);
        Assert.assertNull(successInput);
        Assert.assertNull(successOutcome);
        Assert.assertEquals(input, finallyInput);

        init();
        actual = Optional.of(input).flatMap(TRY_NO_ON_SUCCESS).get();
        Assert.assertEquals(expected, actual);
        Assert.assertNull(successInput);
        Assert.assertNull(successOutcome);
        Assert.assertEquals(input, finallyInput);

        init();
        actual = Optional.of(input).flatMap(TRY_NO_ON_FAILURE).orElse(null);
        Assert.assertNull(actual);
        Assert.assertNull(successInput);
        Assert.assertNull(successOutcome);
        Assert.assertEquals(input, finallyInput);

        init();
        actual = Optional.of(input).flatMap(TRY_NO_ON_FINALLY).get();
        Assert.assertEquals(expected, actual);
        Assert.assertNull(successInput);
        Assert.assertNull(successOutcome);
        Assert.assertNull(finallyInput);
    }

    @Test
    public void testTryFailureUnExpected() {
        final String input = null;
        final Integer expected = Integer.MAX_VALUE;
        final Integer actual = Stream.of(input).map(TRY).findFirst().get().get();
        Assert.assertEquals(expected, actual);
        Assert.assertNull(successInput);
        Assert.assertNull(successOutcome);
        Assert.assertEquals(input, finallyInput);
    }

}
