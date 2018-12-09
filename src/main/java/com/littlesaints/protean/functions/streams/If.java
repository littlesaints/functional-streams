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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <pre>
 * This is a functional substitute for an 'if-else' construct as a lambda, especially when using java streams.
 *
 * The function {@link #wrapWithOptional(If)} can be used to create a function that returns an {@link Optional} of the return Type, since there can be cases where there are no matching conditions provided to map a value.
 * e.g. Configuring an 'if' condition without an else.
 *
 * Usage:
 *
 * {@code
 * IntStream.range(-10, 10).boxed()
 *      .map(i -> {
 *          if (i > 0) {
 *              return "positive";
 *          } else {
 *              return "negative";
 *          }})
 *      .forEach(System.out::println);
 * }
 *
 * can be written as:
 *
 * {@code
 * IntStream.range(-10, 10).boxed()
 *     .map(
 *          If.<Integer, String>test(i -> i > 0)
 *          .then(i -> "positive")
 *          .orElse(i -> "negative"))
 *     .forEach(System.out::println);
 * }
 *
 * Note:
 * - The use of 'elseif' construct is also supported.
 * </pre>
 *
 * @author Varun Anand
 * @since 1.0
 *
 * @param <T> The input type.
 * @param <R> The return type.
 *
 * @see Switch
 *
 */
public class If<T, R> implements Function<T, R> {

    private final Predicate<T> test;

    private Function<T, R> thenMapper = t -> null;

    private Map<Predicate<T>, Function<T, R>> elseIfs = new LinkedHashMap<>(0);

    private Function<T, R> elseMapper = t -> null;

    private If(Predicate <T> test) {
        this.test = test;
    }

    /**
     * @see #wrapWithOptional()
     */
    public static <T, R> If<T, Optional<R>> wrapWithOptional(If<T, R> fx) {
        return fx.wrapWithOptional();
    }

    public static <T, R> If<T, R> test(Predicate<T> predicate) {
        return new If <>(predicate);
    }

    /**
     * <pre>
     * Create an If function that returns an {@link Optional}.
     *
     * This is useful when the application doesn't want to handle {@code null} directly but the code can return {@code null}
     * or there's no code mapped to 'else' or 'then' construct.
     * </pre>
     * @return a copy of this 'If' function.
     */
    public If<T, Optional<R>> wrapWithOptional() {
        final If<T, Optional<R>> fx = If.<T, Optional<R>>test(test)
                .then(t -> Optional.ofNullable(thenMapper.apply(t)))
                .orElse(t -> Optional.ofNullable(elseMapper.apply(t)));
        elseIfs.forEach((k, v) -> fx.elseIfs.put(k, t -> Optional.ofNullable(v.apply(t))));
        return fx;
    }

    public If<T, R> then(Function<T, R> mapper) {
        thenMapper = mapper;
        return this;
    }

    public If<T, R> elseIf(Predicate<T> predicate, Function<T, R> mapper) {
        elseIfs.put(predicate, mapper);
        return this;
    }

    public If<T, R> orElse(Function<T, R> mapper) {
        elseMapper = mapper;
        return this;
    }

    @Override
    public R apply(T t) {
        final R result;
        if (test.test(t)) {
            result = thenMapper.apply(t);
        } else {
            result = elseIfs.entrySet().stream()
                .filter(e -> e.getKey().test(t))
                .findFirst()
                .map(Entry::getValue)
                .orElse(elseMapper)
                .apply(t);
        }
        return result;
    }
}
