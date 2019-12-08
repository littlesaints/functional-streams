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

import com.littlesaints.protean.functions.XFunction;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <pre>
 * This is a functional substitute for an 'try-catch' construct as a lambda, especially when using java streams.
 * It allows a cleaner way to specify a mapper to be invoked when an exception get raised, in a functional manner.
 *
 * The function {@link #wrapWithOptional(Try)} can be used to create a function, that returns an {@link Optional} of the return Type, since there can be cases where there are no matching conditions provided to map a value.
 * e.g. Configuring an 'try' condition without specifying the value mapper in an exception scenario, because one needs a return value.
 *
 * Usage:
 *
 * {@code
 *
 *    Arrays.stream(new String[]{"1", "2", "a", "3"})
 *        .map(i -> {
 *            try {
 *                return Integer.parseInt(i);
 *            } catch (Exception e) {
 *                return Integer.MIN_VALUE;
 *            }
 *        })
 *        .forEach(System.out::println);
 * }
 *
 * can be written as:
 *
 * {@code
 *     Arrays.stream(new String[]{"1", "2", "a", "3"})
 *         .map(
 *             Try.<String, Integer> evaluate(Integer::parseInt)
 *                 .onException((string, exception) -> Integer.MIN_VALUE))
 *         .forEach(System.out::println);
 * }
 *
 * Note:
 * - The use of 'finally' construct is also supported.
 * </pre>
 *
 * @param <T> The input type in the function.
 * @param <R> The return type.
 * @author Varun Anand
 * @see com.littlesaints.protean.functions.trial.Trial
 * @see If
 * @since 1.0
 */
public class Try<T, R> implements Function<T, R> {

    private final XFunction<T, R> mapper;

    private BiFunction<T, Exception, R> failureMapper = (t, e) -> null;

    private Consumer<T> finallyOp = t -> {};

    private BiFunction<T, R, R> successOp = (t, r) -> r;

    private Try(XFunction<T, R> mapper) {
        this.mapper = mapper;
    }

    public static <T, R> Try<T, R> evaluate(XFunction<T, R> mapper) {
        Objects.requireNonNull(mapper);
        return new Try<>(mapper);
    }

    /**
     * @see #wrapWithOptional()
     */
    public static <T, R> Try<T, Optional<R>> wrapWithOptional(Try<T, R> tryFn) {
        Objects.requireNonNull(tryFn);
        return tryFn.wrapWithOptional();
    }

    /**
     * <pre>
     * Create a Try function that returns an {@link Optional}.
     *
     * This is useful when the application doesn't want to handle {@code null} directly but the code can return {@code null}
     * or there's no code mapped to 'onSuccess' or 'onException' construct.
     * </pre>
     *
     * @return a copy of this 'Try' function.
     */
    public Try<T, Optional<R>> wrapWithOptional() {
        return Try.<T, Optional<R>>evaluate(t -> Optional.ofNullable(mapper.apply(t)))
                .onSuccess((t, or) -> successOp.apply(t, or.orElse(null)))
                .onException((t, x) -> Optional.ofNullable(failureMapper.apply(t, x)))
                .onFinally(finallyOp);
    }

    /**
     * @param successOp to be called when the Try succeeds
     * @return this Try instance
     */
    public Try<T, R> onSuccess(BiConsumer<T, R> successOp) {
        Objects.requireNonNull(successOp);
        this.successOp = (t, r) -> {
            successOp.accept(t, r);
            return r;
        };
        return this;
    }

    /**
     * @param failureMapper to be called when the Try results in an exception being thrown
     * @return this Try instance
     */
    public Try<T, R> onException(BiFunction<T, Exception, R> failureMapper) {
        Objects.requireNonNull(failureMapper);
        this.failureMapper = failureMapper;
        return this;
    }

    /**
     * @param failureOp to be called when the Try results in an exception being thrown
     * @return this Try instance
     */
    public Try<T, R> onFailure(BiConsumer<T, Exception> failureOp) {
        Objects.requireNonNull(failureMapper);
        this.failureMapper = (t, e) -> {
            failureOp.accept(t, e);
            return null;
        };
        return this;
    }

    /**
     * @param finallyOp to be called when Try completes, irrespective of the outcome
     * @return this Try instance
     */
    public Try<T, R> onFinally(Consumer<T> finallyOp) {
        Objects.requireNonNull(this.finallyOp);
        this.finallyOp = finallyOp;
        return this;
    }

    @Override
    public R apply(T t) {
        try {
            return successOp.apply(t, mapper.apply(t));
        } catch (Exception e) {
            return failureMapper.apply(t, e);
        } catch (Throwable r) {
            return failureMapper.apply(t, new Exception(r));
        } finally {
            finallyOp.accept(t);
        }
    }
}
