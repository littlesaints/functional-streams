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

import lombok.*;
import lombok.Builder.Default;

import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static com.littlesaints.protean.functions.trial.Defaults.*;

/**
 * <pre>
 * Define parameters for re-attempting a failed operation in a {@link Trial}.
 *
 * Usage:
 * {@code
 *     Strategy strategy = Strategy.builder()
 *             .maxTriesWithYield(5) // maximum no. of re-attempts done with a {@link Thread#yield()} between trials.
 *             .maxTriesWithDelay(10) // maximum no. of re-attempts done with a {@link Thread#sleep(long)} of a configured delay millis.
 *             .triesUntilDelayIncrease(2) // maximum no. of re-attempts, after which the delay millis doubles. This is to implement a phased back-off.
 *             .delayBetweenTriesInMillis(500) // the configured delay in millis between re-attempts.
 *             .delayThresholdInMillis(2000) // the configured delay threshold in millis. The delay between re-attempts will never increase beyond this value.
 *             .build();
 * }
 *
 * See {@link Defaults} for default values of the above configurations.
 *
 * </pre>
 *
 * @author Varun Anand
 * @see Trial
 * @see Defaults
 * @since 1.0
 */
@Getter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Strategy {

    public static Supplier<Strategy> DEFAULT = Strategy::new;

    public static IntFunction<Strategy> CONSTANT_DELAY_UNBOUNDED_TRIES = delayInMillis -> Strategy.builder()
            .delayBetweenTriesInMillis(delayInMillis)
            .maxTriesWithDelay(Constants.UNBOUNDED_TRIES)
            .delayIncreaseMultiplier(1)
            .build();

    /**
     * The maximum number of times, consecutive tries will have a {@link Thread#yield()} operation in between.
     */
    @Default
    private int maxTriesWithYield = TRIES_WITH_YIELD;

    /**
     * The maximum number of re-attempts possible. If all attempts are exhausted, an {@link Optional#empty()} is returned.
     */
    @Default
    private int maxTriesWithDelay = TRIES_WITH_DELAY;

    /**
     * The initial {@link Thread#sleep(long)} duration between consecutive re-attempts, after all {@link #maxTriesWithYield} are exhausted.
     */
    @Default
    private long delayBetweenTriesInMillis = DELAY_BETWEEN_TRIES_IN_MILLIS;

    /**
     * The max value of {@link #maxTriesWithDelay} duration. {@link #triesUntilDelayIncrease} will have no affect once this value is reached.
     */
    @Default
    private long delayThresholdInMillis = DELAY_THRESHOLD_IN_MILLIS;

    /**
     * The number of times after which the {@link #maxTriesWithDelay} duration doubles. This implements a phased-backoff policy for continuously failing operations.
     */
    @Default
    private int triesUntilDelayIncrease = TRIES_UNTIL_DELAY_INCREASE;

    /**
     * The multiplier by which the delay increases, once {@link #triesUntilDelayIncrease} has exhausted.
     */
    @Default
    private int delayIncreaseMultiplier = DELAY_INCREASE_MULTIPLIER;

    public void validate() {
        if (maxTriesWithYield < 0) {
            throw new IllegalArgumentException("maxTriesWithYield must be >= 0 !!");
        }
        if (maxTriesWithDelay < 1 && maxTriesWithDelay != Constants.UNBOUNDED_TRIES) {
            throw new IllegalArgumentException("maxTriesWithDelay must be >= 1 !!");
        }
        if (delayBetweenTriesInMillis < 0) {
            throw new IllegalArgumentException("delayBetweenTriesInMillis must be >= 0 !!");
        }
        if (delayThresholdInMillis < 0) {
            throw new IllegalArgumentException("delayThresholdInMillis must be >= 0 !!");
        }
        if (delayBetweenTriesInMillis > delayThresholdInMillis) {
            throw new IllegalArgumentException("delayBetweenTriesInMillis must be <= delayThresholdInMillis !!");
        }
        if (triesUntilDelayIncrease < 1 && triesUntilDelayIncrease != Constants.NO_DELAY_INCREASE) {
            throw new IllegalArgumentException("triesUntilDelayIncrease must be >= 1 !!");
        }
        if (delayIncreaseMultiplier < 1) {
            throw new IllegalArgumentException("delayIncreaseMultiplier must be >= 1 !!");
        }
    }

}
