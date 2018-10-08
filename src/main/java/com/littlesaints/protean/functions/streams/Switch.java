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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <pre>
 * Functional substitute for a 'switch-case' construct as a lambda, especially when using java streams.
 * It allows a cleaner way to specify a mapper for each condition to be invoked when that condition is met, in a functional manner.
 * The order in which these cases are defined, is honored for matching the conditions. i.e. the mapper for the first condition matched, is invoked. 
 *
 * The function returns an {@link Optional} of the return Type, since there can be cases where there are no matching conditions provided to map a value.
 *
 * Usage:
 *
 * {@code
 *     IntStream.range(-10, 10).boxed()
 *      .map(i -> {
 *          switch (100 / i) {
 *              case 1:
 *                 return "one";
 *              case 2:
 *                 return "two";
 *              default:
 *                 return "other";
 *          }})
 *      .forEach(System.out::println);
 * }
 *
 * can be written as:
 *
 * {@code
 *     IntStream.range(-10, 10).boxed()
 *          .map(
 *              Switch.<Integer, Integer, String>evaluate(input -> 100 / input)
 *                  .when(quotient -> quotient == 1, (input, quotient) -> "one")
 *                  .when(quotient -> quotient == 2, (input, quotient) -> "two")
 *                  .orDefault((input, quotient) -> "other"))
 *          .forEach(System.out::println);
 * }
 *
 * Note:
 * - The use of 'default' case is also supported but it needs to be defined after all conditional cases have been defined,
 *   otherwise a runtime exception is raised during initialization.
 * </pre>
 *
 * @author Varun Anand
 * @since 1.0
 *
 * @param <T> The input type.
 * @param <U> The evaluated result type.
 * @param <R> The final result type.
 *
 * @see Try
 */
public class Switch<T, U, R> implements Function<T, Optional<R>> {

    private final Function<T, U> test;

    private Map<Predicate<U>, BiFunction<T, U, Optional<R>>> cases = new HashMap<>(0);

    private BiFunction<T, U, Optional<R>> defaultCase = (t, u) -> Optional.empty();

    private Switch(Function<T, U> test) {
        this.test = test;
    }

    public static <T, U, R> Switch<T, U, R> evaluate(Function<T, U> mapper) {
        return new Switch<>(mapper);
    }

    public Switch<T, U, R> when(Predicate<U> predicate, BiFunction<T, U, R> mapper) {
        return whenOptional(predicate, (t, u) -> Optional.ofNullable(mapper.apply(t, u)));
    }

    /**
     * Same as {@link #whenOptional(Predicate, BiFunction)}. It's named differently to make it easier for use.
     */
    public Switch<T, U, R> whenO(Predicate<U> predicate, BiFunction<T, U, Optional<R>> mapper) {
        return whenOptional(predicate, mapper);
    }

    public Switch<T, U, R> whenOptional(Predicate<U> predicate, BiFunction<T, U, Optional<R>> mapper) {
        cases.put(predicate, mapper);
        return this;
    }

    public Switch<T, U, R> orDefault(BiFunction<T, U, R> mapper) {
        return defaultOptional((t, u) -> Optional.ofNullable(mapper.apply(t, u)));
    }

    /**
     * Same as {@link #defaultOptional(BiFunction)}. It's named differently to make it easier for use.
     */
    public Switch<T, U, R> defaultO(BiFunction<T, U, Optional<R>> mapper) {
        return defaultOptional(mapper);
    }

    public Switch<T, U, R> defaultOptional(BiFunction<T, U, Optional<R>> mapper) {
        defaultCase = mapper;
        return this;
    }

    @Override
    public Optional<R> apply(T t) {
        final U u = test.apply(t);
        return cases.entrySet().stream().filter(e -> e.getKey().test(u)).findFirst()
            .map(Entry::getValue)
            .orElse(defaultCase)
            .apply(t, u);
    }
}
