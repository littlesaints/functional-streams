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

package com.littlesaints.protean.functions.trial;

import static com.littlesaints.protean.functions.trial.Constants.UNBOUNDED_TRIES;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 * Functional way to re-attempt failures. It supports configuring a re-attempt strategy and can perform a phased-backoff on continuous failures.
 * It's a {@link Supplier} that re-attempts failed attempts to generate/retrieve/supply a value, based on the defined {@link Strategy}.
 *
 * It does all the complex state management and tracking of failures, so the application can focus on it's business logic.
 *
 * Usage:
 * {@code
 *     Trial<Integer> trial = Trial.of(Strategy.DEFAULT, () -> {
 *         Integer result = 1; //retrieve / calculate value
 *         return result;
 *     });
 * }
 *
 * Instances of this class are NOT thread-safe. However, they can re-used after invoking the 'reset' method.
 *
 * Null value returned from an operation is considered an empty response and an {@link Optional#empty()} is returned.
 *
 * Note for interpreting the available statistics:
 *
 * If the very first trial succeeds, it doesn't count towards either {@link Trial#attemptedTriesWithYield} or {@link Trial#attemptedTriesWithDelay}
 * Hence, the total attempts made by a Trial instance will be one more than the sum of attemptedTriesWithXXX metrics.
 * </pre>
 *
 * @author Varun Anand
 * @since 1.0
 *
 * @see Strategy
 * @see com.littlesaints.protean.functions.streams.Try
 */
@Slf4j
public class Trial<T> implements Supplier<Optional<T>> {

    private final Strategy strategy;

    @Getter
    private final Supplier<Optional<T>> supplier;

    @Getter
    private long currentDelayBetweenTriesInMillis;

    @Getter
    private int attemptedTriesWithDelay;

    @Getter
    private int attemptedTriesWithYield;

    @Getter
    private long remainingTriesUntilDelayIncrease;

    /**
     * Creates a Trial with the given {@link Strategy} and an Operation that needs to executed.
     * The operation is considered success, if it resulted in a non-null value.
     */
    public static <T> Trial<T> ofNullable(Strategy strategy, Supplier<T> op) {
        return of(strategy, op, Objects::nonNull);
    }

    /**
     * Creates a Trial with the given {@link Strategy} and an Operation that needs to executed.
     * The operation is considered success, if it resulted in a non-empty {@link Optional}.
     */
    public static <T> Trial<Optional<T>> ofOptional(Strategy strategy, Supplier<T> op) {
        return of(strategy, () -> Optional.ofNullable(op.get()), Optional::isPresent);
    }

    /**
     * Creates a Trial with the given {@link Strategy} and an Operation that needs to executed.
     * The operation is considered success, if it resulted in a value that tests positive with the given predicate.
     */
    public static <T> Trial<T> of(Strategy strategy, Supplier<T> op, Predicate<T> successfulOpTest) {
        return new Trial<>(strategy, op, successfulOpTest);
    }

    private Trial(Strategy strategy, Supplier<T> op, Predicate<T> successfulOpTest) {
        this.strategy = strategy.toBuilder().build();
        this.supplier = () -> {
            reset();
            T result;
            do {
                if (successfulOpTest.test(result = op.get())){
                    return Optional.ofNullable(result);
                }
            } while (test());
            return Optional.empty();
        };
    }

    @Override
    public Optional <T> get() {
        return supplier.get();
    }

    private void reset() {
        attemptedTriesWithYield = 0;
        attemptedTriesWithDelay = 0;
        currentDelayBetweenTriesInMillis = strategy.getDelayBetweenTriesInMillis();
        remainingTriesUntilDelayIncrease = strategy.getTriesUntilDelayIncrease();
    }

    private boolean test() {

        if (attemptedTriesWithYield < strategy.getMaxTriesWithYield()) {
            Thread.yield();
            ++attemptedTriesWithYield;
            return true;
        }

        boolean retry = false;
        if (strategy.getMaxTriesWithDelay() == UNBOUNDED_TRIES) {
            //If unbounded retries, then don't increment attemptedTriesWithDelay or it'll eventually overflow.
            retry = true;
        }
        else if (attemptedTriesWithDelay < strategy.getMaxTriesWithDelay()) {
            ++attemptedTriesWithDelay;
            retry = true;
        }

        if (retry) {
            //can't do better atm.
            try {
                Thread.sleep(currentDelayBetweenTriesInMillis);
            } catch (InterruptedException e) {
                log.warn("Error during Thread.sleep.", e);
            }

            // iff delayThresholdInMillis isn't reached.
            if (currentDelayBetweenTriesInMillis < strategy.getDelayThresholdInMillis()) {
                if (remainingTriesUntilDelayIncrease > 0) {
                    --remainingTriesUntilDelayIncrease;
                    if (remainingTriesUntilDelayIncrease == 0) {
                        // reset
                        remainingTriesUntilDelayIncrease = strategy.getTriesUntilDelayIncrease();
                        // double the wait time bounded by the threshold
                        currentDelayBetweenTriesInMillis = Math.min(currentDelayBetweenTriesInMillis * 2, strategy.getDelayThresholdInMillis());
                    }
                }
            }
        }

        return retry;
    }

}
