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

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <pre>
 * This is a functional substitute for an 'try-catch' construct as a lambda, especially when using java streams.
 * It allows a cleaner way to specify a mapper to be invoked when an exception get raised, in a functional manner.
 *
 * The function {@link #wrapWithOptional()} can be used to create a function, that returns an {@link Optional} of the return Type, since there can be cases where there are no matching conditions provided to map a value.
 * e.g. Configuring an 'try' condition without specifying the value mapper in an exception scenario, because one needs a return value.
 *
 * Usage:
 *
 * {@code
 *     IntStream.range(-10, 10).boxed()
 *      .map(i -> {
 *          try (100 / i) {
 *              return "positive";
 *          } catch (Exception e) {
 *              return "negative";
 *          }})
 *      .forEach(System.out::println);
 * }
 *
 * can be written as:
 *
 * {@code
 *     IntStream.range(-10, 10).boxed()
 *          .map(
 *              Try.<Integer, Integer, String>of(input -> 100 / input)
 *                  .onSuccess(quotient -> "positive")
 *                  .onFailure((input, ex) -> "negative"))
 *          .forEach(System.out::println);
 * }
 *
 * Note:
 * - The use of 'finally' construct is also supported.
 * </pre>
 *
 * @author Varun Anand
 * @since 1.0
 *
 * @param <T> The input type in the function.
 * @param <U> The value type evaluated by the try construct.
 * @param <R> The final return type.
 *
 * @see com.littlesaints.protean.functions.trial.Trial
 * @see If
 */
public class Try<T, U, R> implements Function<T, R> {

    private final XFunction<T, U> mapper;

    private Function<U, R> successMapper = u -> null;

    private BiFunction<T, Exception, R> failureMapper = (t, e) -> null;

    private Consumer<T> finallyMapper = t -> {};

    public Try(XFunction <T, U> mapper) {
        this.mapper = mapper;
    }

    public static <T, U, R> Try<T, U, R> of(XFunction<T, U> mapper) {
        return new Try <>(mapper);
    }

    /**
     * @see #wrapWithOptional()
     */
    public static <T, U, R> Try<T, U, Optional<R>> wrapWithOptional(Try<T, U, R> fx) {
        return fx.wrapWithOptional();
    }

    /**
     * <pre>
     * Create a Try function that returns an {@link Optional}.
     *
     * This is useful when the application doesn't want to handle {@code null} directly but the code can return {@code null}
     * or there's no code mapped to 'onSuccess' or 'onFailure' construct.
     * </pre>
     * @return a copy of this 'Try' function.
     */
    public Try<T, U, Optional<R>> wrapWithOptional() {
        return Try.<T, U, Optional<R>>of(mapper)
            .onSuccess(u -> Optional.ofNullable(successMapper.apply(u)))
            .onFailure((t, x) -> Optional.ofNullable(failureMapper.apply(t, x)))
            .onFinally(finallyMapper);
    }
    /**
     * @param successMapper to be called when the Try succeeds
     * @return this Try instance
     */
    public Try<T, U, R> onSuccess(Function <U, R> successMapper) {
        this.successMapper = successMapper;
        return this;
    }

    /**
     * @param failureMapper to be called when the Try results in an exception being thrown
     * @return this Try instance
     */
    public Try<T, U, R> onFailure(BiFunction<T, Exception, R> failureMapper) {
        this.failureMapper = failureMapper;
        return this;
    }

    /**
     * @param finallyMapper to be called when Try completes, irrespective of the outcome
     * @return this Try instance
     */
    public Try<T, U, R> onFinally(Consumer<T> finallyMapper) {
        this.finallyMapper = finallyMapper;
        return this;
    }

    @Override
    public R apply(T t) {
        try {
            return successMapper.apply(mapper.apply(t));
        } catch (Exception e) {
            return failureMapper.apply(t, e);
        } catch (Throwable r) {
            return failureMapper.apply(t, new Exception(r));
        } finally {
            finallyMapper.accept(t);
        }
    }

}

