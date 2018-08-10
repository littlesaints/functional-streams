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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <pre>
 * This is a function to substitute use of an 'if-else' construct as a lambda, especially when using java streams.
 *
 * The function returns an {@link Optional} of the return Type, since there can be cases where there are no matching conditions provided to map a value.
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
public class If<T, R> implements Function<T, Optional<R>> {

    private final Predicate<T> test;

    private Function<T, Optional<R>> thenMapper = t -> Optional.empty();

    private Map<Predicate<T>, Function<T, Optional<R>>> elseIfs = new HashMap <>(0);

    private Function<T, Optional<R>> elseMapper = t -> Optional.empty();

    private If(Predicate <T> test) {
        this.test = test;
    }

    public static <T, R> If<T, R> test(Predicate<T> predicate) {
        return new If <>(predicate);
    }

    public If<T, R> then(Function<T, R> mapper) {
        return thenOptional(t -> Optional.ofNullable(mapper.apply(t)));
    }

    /**
     * Same as {@link #thenOptional(Function)}. It's named differently to make it easier for use.
     */
    public If<T, R> thenO(Function<T, Optional<R>> mapper) {
        return thenOptional(mapper);
    }

    public If<T, R> thenOptional(Function<T, Optional<R>> mapper) {
        thenMapper = mapper;
        return this;
    }

    public If<T, R> elseIf(Predicate<T> predicate, Function<T, R> mapper) {
        return elseIfOptional(predicate, t -> Optional.ofNullable(mapper.apply(t)));
    }

    /**
     * Same as {@link #elseIfOptional(Predicate, Function)}. It's named differently to make it easier for use.
     */
    public If<T, R> elseIfO(Predicate<T> predicate, Function<T, Optional<R>> mapper) {
        return elseIfOptional(predicate, mapper);
    }

    public If<T, R> elseIfOptional(Predicate<T> predicate, Function<T, Optional<R>> mapper) {
        elseIfs.put(predicate, mapper);
        return this;
    }

    public If<T, R> orElse(Function<T, R> mapper) {
        return elseOptional(t -> Optional.ofNullable(mapper.apply(t)));
    }

    /**
     * Same as {@link #elseOptional(Function)}. It's named differently to make it easier for use.
     */
    public If<T, R> elseO(Function<T, Optional<R>> mapper) {
        return elseOptional(mapper);
    }

    public If<T, R> elseOptional(Function<T, Optional<R>> mapper) {
        elseMapper = mapper;
        return this;
    }

    @Override
    public Optional<R> apply(T t) {
        final Optional<R> result;
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
