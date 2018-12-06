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
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * <pre>
 * Functional way to re-attempt failures. It supports configuring a re-attempt strategy and can perform a phased-backoff on continuous failures.
 * It's a {@link Supplier} that re-attempts failed attempts to generate/retrieve/supply a value, based on the defined {@link Strategy}.
 *
 * It does all the complex state management and tracking of failures, so the application can focus on it's business logic.
 *
 * Usage:
 * {@code
 *     Trial<Integer> trial = Trial.ofNullable(Strategy.DEFAULT, () -> {
 *         Integer result = 1; // a calculation that can fail, which will then be re-attempted based on the configured strategy
 *         return result;
 *     });
 *
 *     Stream.generate(trial).limit(10).forEach(System.out::println);
 * }
 *
 * Instances of this class are NOT thread-safe. However, they can re-used.
 *
 * <b>Note:</b>
 *
 * If the very first trial succeeds, it doesn't count towards either attemptedTriesWithYield or attemptedTriesWithDelay.
 * Hence, the total attempts made by a Trial instance will be one more than the sum of attemptedTriesWithXXX metrics.
 *
 * Available statistics:
 * {@link #getCurrentDelayBetweenTriesInMillis()}, {@link #getAttemptedTriesWithYield()}, {@link #getAttemptedTriesWithDelay()} and {@link #getRemainingTriesUntilDelayIncrease()}
 * </pre>
 *
 * @author Varun Anand
 * @since 1.0
 *
 * @see Strategy
 * @see FnTrial
 * @see com.littlesaints.protean.functions.streams.Try
 */
@Slf4j
public class Trial<T> extends AbstractTrial<T> implements Supplier<T> {

    private final Supplier<T> supplier;

    /**
     * Creates a Trial with the given {@link Strategy} and an Operation that needs to executed.
     * The operation is considered success, if it results in a non-null value.
     * {@code null} is returned, is all trials are exhausted.
     */
    public static <T> Trial<T> ofNullable(Strategy strategy, Supplier<T> op) {
        return of(strategy, op, Objects::nonNull, t -> null);
    }

    /**
     * Creates a Trial with the given {@link Strategy} and an Operation that needs to executed.
     * The operation is considered success, if it results in a non-null value.
     * A non-empty {@link Optional} is returned in case of a success or an empty {@link Optional}, otherwise.
     */
    public static <T> Trial<Optional<T>> ofOptional(Strategy strategy, Supplier<T> op) {
        return of(strategy, () -> Optional.ofNullable(op.get()), Optional::isPresent, t -> Optional.empty());
    }

    /**
     * Creates a Trial with the given {@link Strategy} and an Operation that needs to executed.
     * The operation is considered success, if it results in a value that tests positive with the given predicate.
     */
    public static <T> Trial<T> of(Strategy strategy, Supplier<T> op, Predicate<T> successfulOpTest, UnaryOperator<T> onTrialsExhaustion) {
        return new Trial<>(strategy, op, successfulOpTest, onTrialsExhaustion);
    }

    private Trial(Strategy strategy, Supplier<T> op, Predicate<T> successfulOpTest, UnaryOperator<T> onTrialsExhaustion) {
        super(strategy, successfulOpTest, onTrialsExhaustion);
        this.supplier = op;
    }

    @Override
    public T get() {
        return get(supplier);
    }

}
