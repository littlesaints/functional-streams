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

package com.littlesaints.protean.functions.trial;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * <pre>
 * Functional way to re-attempt failures upon executing a {@link Function}. It supports configuring a re-attempt strategy and can perform a phased-backoff on continuous failures.
 * It's a {@link Function} that re-attempts failed attempts to execute an operation on an input value, based on the defined {@link Strategy}.
 *
 * It does all the complex state management and tracking of failures, so the application can focus on it's business logic.
 *
 * It's similar to {@link Trial} but allows the application to configure a {@link Function} that operates on an input.
 *
 * Usage:
 * {@code
 *     FnTrial<Integer, Double> trial = FnTrial.ofNullable(Strategy.DEFAULT, i -> {
 *         Double result = i * 2; // any calculation that can fail. The method in this example (ofNullable), implies that a non-null return value is a success.
 *         // a failed trial will be re-attempted based on the configured strategy.
 *         return result;
 *     });
 *
 *     IntStream.range(0, 10).boxed().map(trial).forEach(System.out::println);
 * }
 *
 * Instances of this class are NOT thread-safe. However, they can re-used.
 *
 * <b>Note:</b>
 *
 * If the very first trial succeeds, it doesn't count towards either attemptedTriesWithYield or attemptedTriesWithDelay.
 * Hence, the total attempts made by a FnTrial instance will be one more than the sum of attemptedTriesWithXXX metrics.
 *
 * Available statistics:
 * {@link #getCurrentDelayBetweenTriesInMillis()}, {@link #getAttemptedTriesWithYield()}, {@link #getAttemptedTriesWithDelay()} and {@link #getRemainingTriesUntilDelayIncrease()}
 * </pre>
 *
 * @author Varun Anand
 * @see Strategy
 * @see Trial
 * @see com.littlesaints.protean.functions.streams.Try
 * @since 1.0.1
 */
@Slf4j
public class FnTrial<T, R> extends AbstractTrial<R> implements Function<T, R> {

    private final Function<T, R> supplier;

    /**
     * Creates a Trial with the given {@link Strategy} and a Function will be executed on an input value.
     * The operation is considered success, if it results in a non-null value.
     * {@code null} is returned, is all trials are exhausted.
     */
    public static <T, R> FnTrial<T, R> ofNullable(Strategy strategy, Function<T, R> op) {
        return of(strategy, op, Objects::nonNull, r -> null);
    }

    /**
     * Creates a Trial with the given {@link Strategy} and a Function will be executed on an input value.
     * The operation is considered success, if it results in a non-empty {@link Optional}.
     */
    public static <T, R> FnTrial<T, Optional<R>> ofOptional(Strategy strategy, Function<T, R> op) {
        return of(strategy, t -> Optional.ofNullable(op.apply(t)), Optional::isPresent, r -> Optional.empty());
    }

    /**
     * Creates a Trial with the given {@link Strategy} and a Function will be executed on an input value.
     * The operation is considered success, if it results in a value that tests positive with the given predicate.
     */
    public static <T, R> FnTrial<T, R> of(Strategy strategy, Function<T, R> op, Predicate<R> successfulOpTest, UnaryOperator<R> onTrialsExhaustion) {
        return new FnTrial<>(strategy, op, successfulOpTest, onTrialsExhaustion);
    }

    private FnTrial(Strategy strategy, Function<T, R> op, Predicate<R> successfulOpTest, UnaryOperator<R> onTrialsExhaustion) {
        super(strategy, successfulOpTest, onTrialsExhaustion);
        this.supplier = op;
    }

    @Override
    public R apply(T t) {
        return get(() -> supplier.apply(t));
    }

}
