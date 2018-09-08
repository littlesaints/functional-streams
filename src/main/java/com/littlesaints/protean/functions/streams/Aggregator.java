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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.LongPredicate;
import java.util.function.ToIntFunction;

/**
 * <pre>
 * It can be used to aggregate or batch inputs based on a predicate.
 * A use-case can be of creating an archive, whose size is closest possible to a threshold.
 *
 * Usage:
 *
 * The following code batches integers with sum closest but less than 100.
 *
 * {@code
 * IntStream.range(0, 50).boxed()
 *   .map(
 *      Aggregator.of(Integer::intValue, l -> l < 100, Object::toString))
 *   .filter(Optional::isPresent)
 *   .map(Optional::get)
 *   .forEach(System.out::println);
 * }
 * </pre>
 * @param <T> the input type
 * @param <R> the aggregated output type
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Aggregator<T, R> implements Function<T, Optional<R>> {

    private final ToIntFunction<T> metricResolver;
    private final LongPredicate predicateToContinueAggregating;
    private final Function<Collection<T>, R> mapper;
    private final LongAdder adder = new LongAdder();
    private final Collection<T> collection = new ArrayList<>();

    public static <T, R> Aggregator<T, R> of (ToIntFunction<T> metricResolver, LongPredicate predicateToContinueAggregating,
                                              Function<Collection<T>, R> mapper) {
        return new Aggregator<>(metricResolver, predicateToContinueAggregating, mapper);
    }

    public static <T> Aggregator<T, Collection<T>> of (ToIntFunction<T> metricResolver, LongPredicate predicateToContinueAggregating) {
        return of(metricResolver, predicateToContinueAggregating, c -> c);
    }

        @Override
    public Optional<R> apply(T t) {
        int metric = metricResolver.applyAsInt(t);
        adder.add(metric);
        final Optional<R> result;
        if (predicateToContinueAggregating.test(adder.sum())) {
            result = Optional.empty();
        } else {
            result = Optional.ofNullable(mapper.apply(new ArrayList<>(collection)));
            adder.reset();
            adder.add(metric);
            collection.clear();
        }
        collection.add(t);
        return result;
    }
}
