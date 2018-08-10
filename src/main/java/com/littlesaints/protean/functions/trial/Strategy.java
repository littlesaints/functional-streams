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

import static com.littlesaints.protean.functions.trial.Defaults.DELAY_BETWEEN_TRIES_IN_MILLIS;
import static com.littlesaints.protean.functions.trial.Defaults.DELAY_THRESHOLD_IN_MILLIS;
import static com.littlesaints.protean.functions.trial.Defaults.TRIES_UNTIL_DELAY_INCREASE;
import static com.littlesaints.protean.functions.trial.Defaults.TRIES_WITH_DELAY;
import static com.littlesaints.protean.functions.trial.Defaults.TRIES_WITH_YIELD;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * <pre>
 * Define parameters for re-attempting a failed operation in a {@link Trial}
 *
 * Usage:
 * {@code
 *     Strategy strategy = Strategy.builder()
 *             .maxTriesWithYield(5)
 *             .maxTriesWithDelay(10)
 *             .triesUntilDelayIncrease(2)
 *             .delayBetweenTriesInMillis(500)
 *             .delayThresholdInMillis(2000)
 *             .build();
 * }
 * </pre>
 *
 * @author Varun Anand
 * @since 0.1
 * @see Trial
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Builder(toBuilder = true)
@AllArgsConstructor
public class Strategy {

    public static Strategy DEFAULT = Strategy.builder().build();

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

}
